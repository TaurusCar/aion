package org.aion.zero.impl.pos;

import static org.aion.mcf.core.ImportResult.IMPORTED_BEST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.aion.crypto.ECKey;
import org.aion.crypto.ECKeyFac;
import org.aion.evtmgr.IEvent;
import org.aion.evtmgr.IEventMgr;
import org.aion.evtmgr.IHandler;
import org.aion.evtmgr.impl.callback.EventCallback;
import org.aion.evtmgr.impl.es.EventExecuteService;
import org.aion.evtmgr.impl.evt.EventConsensus;
import org.aion.evtmgr.impl.evt.EventConsensus.CALLBACK;
import org.aion.evtmgr.impl.evt.EventTx;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.blockchain.IPendingState;
import org.aion.mcf.core.ImportResult;
import org.aion.util.bytes.ByteUtil;
import org.aion.util.conversions.Hex;
import org.aion.zero.impl.blockchain.AionImpl;
import org.aion.zero.impl.config.CfgAion;
import org.aion.zero.impl.core.IAionBlockchain;
import org.aion.zero.impl.sync.SyncMgr;
import org.aion.zero.impl.types.AionPoSBlock;
import org.aion.zero.types.AionTransaction;
import org.slf4j.Logger;

/**
 * {@link AionPoS} contains the logic to process new mined blocks and dispatch new mining task to
 * miners when needed.
 */
public class AionPoS {
    protected static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.CONS.name());

    private static final int syncLimit = 128;

    protected IAionBlockchain blockchain;
    protected IPendingState<AionTransaction> pendingState;
    protected IEventMgr eventMgr;

    protected AtomicBoolean initialized = new AtomicBoolean(false);
    protected AtomicBoolean newPendingTxReceived = new AtomicBoolean(false);
    protected AtomicLong lastUpdate = new AtomicLong(0);

    private AtomicBoolean shutDown = new AtomicBoolean();
    private SyncMgr syncMgr;

    private EventExecuteService ees;

    private byte[] seed = new byte[96];
    private byte[] privateKey =
            ByteUtil.hexStringToBytes(
                    "0xcc76648ce8798bc18130bc9d637995e5c42a922ebeab78795fac58081b9cf9d4069346ca77152d3e42b1630826feef365683038c3b00ff20b0ea42d7c121fa9f");

    private ECKey key = ECKeyFac.inst().fromPrivate(privateKey);

    private final class EpPOS implements Runnable {
        boolean go = true;

        @Override
        public void run() {
            while (go) {
                IEvent e = ees.take();

                if (e.getEventType() == IHandler.TYPE.TX0.getValue()
                        && e.getCallbackType() == EventTx.CALLBACK.PENDINGTXRECEIVED0.getValue()) {
                    newPendingTxReceived.set(true);
                } else if (e.getEventType() == IHandler.TYPE.CONSENSUS.getValue()
                        && e.getCallbackType() == CALLBACK.ON_STAKE_SIG.getValue()) {
                    finalizeBlock((AionPoSBlock) e.getFuncArgs().get(0));
                } else if (e.getEventType() == IHandler.TYPE.POISONPILL.getValue()) {
                    go = false;
                }
            }
        }
    }

    private final CfgAion config = CfgAion.inst();

    /**
     * Creates an {@link AionPoS} instance. Be sure to call {@link #init(IAionBlockchain,
     * IPendingState, IEventMgr)} to initialize the instance.
     */
    public AionPoS() {}

    /**
     * Initializes this instance.
     *
     * @param blockchain Aion blockchain instance
     * @param pendingState List of Aion transactions
     * @param eventMgr Event manager
     */
    public void init(
            IAionBlockchain blockchain,
            IPendingState<AionTransaction> pendingState,
            IEventMgr eventMgr) {
        if (initialized.compareAndSet(false, true)) {
            this.blockchain = blockchain;
            this.pendingState = pendingState;
            this.eventMgr = eventMgr;
            this.syncMgr = SyncMgr.inst();

            // return early if staking is disabled, otherwise we are doing needless
            // work by generating new block templates on IMPORT_BEST
            if (!config.getConsensus().getStaking()) {
                return;
            }

            setupHandler();
            ees = new EventExecuteService(100_000, "EpPos", Thread.NORM_PRIORITY, LOG);
            ees.setFilter(setEvtFilter());

            registerCallback();
            ees.start(new EpPOS());

            new Thread(
                            () -> {
                                while (!shutDown.get()) {
                                    try {
                                        Thread.sleep(1);

                                        long now = System.currentTimeMillis();
                                        if (now - lastUpdate.get() > 19999
                                                        && newPendingTxReceived.compareAndSet(
                                                                true, false)
                                                || now - lastUpdate.get()
                                                        > 19999) { // fallback, when
                                            // we never
                                            // received any
                                            // events

                                            seed =
                                                    key.sign(blockchain.getBestBlock().getSeed())
                                                            .toBytes();

                                            createNewBlockTemplate(seed);
                                        }
                                    } catch (InterruptedException e) {
                                        break;
                                    }
                                }
                            },
                            "pos")
                    .start();
        }
    }

    /** Sets up the consensus event handler. */
    private void setupHandler() {
        List<IEvent> txEvts = new ArrayList<>();
        txEvts.add(new EventTx(EventTx.CALLBACK.PENDINGTXRECEIVED0));
        txEvts.add(new EventTx(EventTx.CALLBACK.PENDINGTXUPDATE0));
        txEvts.add(new EventTx(EventTx.CALLBACK.PENDINGTXSTATECHANGE0));
        eventMgr.registerEvent(txEvts);

        List<IEvent> events = new ArrayList<>();
        events.add(new EventConsensus(EventConsensus.CALLBACK.ON_BLOCK_TEMPLATE));
        events.add(new EventConsensus(CALLBACK.ON_STAKE_SIG));
        eventMgr.registerEvent(events);
    }

    private Set<Integer> setEvtFilter() {
        Set<Integer> eventSN = new HashSet<>();
        int sn = IHandler.TYPE.TX0.getValue() << 8;
        eventSN.add(sn + EventTx.CALLBACK.PENDINGTXRECEIVED0.getValue());

        sn = IHandler.TYPE.CONSENSUS.getValue() << 8;
        eventSN.add(sn + EventConsensus.CALLBACK.ON_STAKE_SIG.getValue());

//        sn = IHandler.TYPE.BLOCK0.getValue() << 8;
//        eventSN.add(sn + EventBlock.CALLBACK.ONBEST0.getValue());

        return eventSN;
    }

    /**
     * Registers callback for the {@link
     * org.aion.evtmgr.impl.evt.EventConsensus.CALLBACK#ON_SOLUTION} event.
     */
    public void registerCallback() {
        IHandler consensusHandler = eventMgr.getHandler(IHandler.TYPE.CONSENSUS.getValue());
        consensusHandler.eventCallback(new EventCallback(ees, LOG));

        IHandler blockHandler = eventMgr.getHandler(IHandler.TYPE.BLOCK0.getValue());
        blockHandler.eventCallback(new EventCallback(ees, LOG));

        IHandler transactionHandler = eventMgr.getHandler(IHandler.TYPE.TX0.getValue());
        transactionHandler.eventCallback(new EventCallback(ees, LOG));
    }

    /**
     * Processes a received solution.
     *
     * @param signedBlock the block has been signed.
     */
    protected synchronized void finalizeBlock(AionPoSBlock signedBlock) {
        if (!shutDown.get()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Best block num [{}]", blockchain.getBestBlock().getNumber());
                LOG.debug(
                        "Best block hash [{}]",
                        Hex.toHexString(blockchain.getBestBlock().getHash()));
            }

            //            if (!Arrays.equals(signedBlock.getHeader().getSignature(), new byte[64])
            //                    && !(signedBlock.getHeader().getSignature().length == 0)) {
            //                // block has been processed
            //                return;
            //            }

            // This can be improved
            ImportResult importResult = AionImpl.inst().addNewMinedBlock(signedBlock);

            // Check that the new block was successfully added
            if (importResult.isSuccessful()) {
                if (importResult == IMPORTED_BEST) {
                    LOG.info(
                            "block sealed <num={}, hash={}, diff={}, tx={}>",
                            signedBlock.getNumber(),
                            signedBlock.getShortHash(),
                            signedBlock.getHeader().getDifficultyBI().toString(),
                            signedBlock.getTransactionsList().size());
                } else {
                    LOG.debug(
                            "block sealed <num={}, hash={}, diff={}, td={}, tx={}, result={}>",
                            signedBlock.getNumber(),
                            signedBlock.getShortHash(),
                            signedBlock.getHeader().getDifficultyBI().toString(),
                            blockchain.getTotalDifficulty(),
                            signedBlock.getTransactionsList().size(),
                            importResult);
                }
                // TODO: fire block mined event
            } else {
                LOG.info(
                        "Unable to import a new mined block; restarting mining.\n"
                                + "Mined block import result is "
                                + importResult
                                + " : "
                                + signedBlock.getShortHash());
            }
        }
    }

    /** Creates a new block template. */
    protected synchronized void createNewBlockTemplate(byte[] seed) {

        if (!shutDown.get()) {
            // TODO: Validate the trustworthiness of getNetworkBestBlock - can
            // it be used in DDOS?
            if (this.syncMgr.getNetworkBestBlockNumber() - blockchain.getBestBlock().getNumber()
                    > syncLimit) {
                return;
            }

            if (seed == null || seed.length != 96) {
                LOG.error("Invalid seed info.");
                return;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Creating a new block template");
            }

            AionPoSBlock bestBlock =
                    blockchain.getBlockByNumber(blockchain.getBestBlock().getNumber());

            List<AionTransaction> txs = pendingState.getPendingTransactions();

            AionPoSBlock newBlock = blockchain.createNewBlock(bestBlock, txs, seed);

            EventConsensus ev = new EventConsensus(EventConsensus.CALLBACK.ON_BLOCK_TEMPLATE);
            ev.setFuncArgs(Collections.singletonList(newBlock));
            eventMgr.newEvent(ev);

            // update last timestamp
            lastUpdate.set(System.currentTimeMillis());
        }
    }

    public synchronized void shutdown() {
        if (ees != null) {
            ees.shutdown();
        }
        shutDown.set(true);
    }
}
