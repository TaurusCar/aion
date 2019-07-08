package org.aion.stake;


import static org.aion.util.conversions.Hex.toHexString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;
import org.aion.evtmgr.IEvent;
import org.aion.evtmgr.IEventMgr;
import org.aion.evtmgr.IHandler;
import org.aion.evtmgr.impl.callback.EventCallback;
import org.aion.evtmgr.impl.es.EventExecuteService;
import org.aion.evtmgr.impl.evt.EventConsensus;
import org.aion.evtmgr.impl.evt.EventMiner;
import org.aion.interfaces.block.Solution;
import org.aion.mcf.stake.AbstractStakeRunner;
import org.aion.zero.impl.blockchain.AionImpl;
import org.aion.zero.impl.blockchain.IAionChain;
import org.aion.zero.impl.config.CfgAion;
import org.aion.zero.impl.types.AionBlock;
import org.aion.zero.impl.types.AionPoSBlock;
import org.aion.zero.types.PoSBlockInterface;

public class StakeRunner extends AbstractStakeRunner<AionPoSBlock> {

    public static final String VERSION = "1.0.0";

    private final CfgAion cfg;

    private final IEventMgr evtMgr;

    private final EventExecuteService ees;

    private Thread thread = null;

    private final class EpMiner implements Runnable {
        boolean go = true;

        @Override
        public void run() {
            while (go) {
                IEvent e = ees.take();
                if (e.getEventType() == IHandler.TYPE.CONSENSUS.getValue()
                        && e.getCallbackType()
                                == EventConsensus.CALLBACK.ON_BLOCK_TEMPLATE.getValue()) {
                    StakeRunner.this.onBlockTemplate((AionBlock) e.getFuncArgs().get(0));
                } else if (e.getEventType() == IHandler.TYPE.POISONPILL.getValue()) {
                    go = false;
                }
            }
        }
    }

    private static class Holder {
        static final StakeRunner INSTANCE = new StakeRunner();
    }

    /**
     * Singleton instance
     *
     * @return Equihash miner instance
     */
    public static StakeRunner inst() {
        return Holder.INSTANCE;
    }

    /** Private constructor; called by singleton instance once */
    private StakeRunner() {
        this.cfg = CfgAion.inst();

        IAionChain a0Chain = AionImpl.inst();

        ees = new EventExecuteService(1000, "EpMiner", Thread.NORM_PRIORITY, LOG);
        ees.setFilter(setEvtFilter());

        this.evtMgr = a0Chain.getAionHub().getEventMgr();
        registerMinerEvents();
        registerCallback();

        ees.start(new EpMiner());
    }

    private Set<Integer> setEvtFilter() {
        Set<Integer> eventSN = new HashSet<>();

        int sn = IHandler.TYPE.CONSENSUS.getValue() << 8;
        eventSN.add(sn + EventConsensus.CALLBACK.ON_BLOCK_TEMPLATE.getValue());

        return eventSN;
    }

    @Override
    public void startStaking() {
        if (!isStaking) {
            isStaking = true;
            fireRunnerStarted();

//            scheduledWorkers.scheduleWithFixedDelay(
//                    new ShowMiningStatusTask(),
//                    STATUS_INTERVAL * 2,
//                    STATUS_INTERVAL,
//                    TimeUnit.SECONDS);

            thread = new Thread(this::staking, "staker");

            thread.start();
                LOG.info("Pos staker starting.");
        }
    }

    @Override
    public void stopStaking() {
        if (isStaking) {
            isStaking = false;
            fireRunnerStopped();
            LOG.info("Pos staker stopping.");

//            scheduledWorkers.shutdownNow();

            thread.interrupt();
            LOG.info("Interrupt staker.");

            try {
                thread.join();
                LOG.info("Stopped staker.");
            } catch (InterruptedException e) {
                LOG.error("Failed to stop staker thread");
            }
        }
    }

    /** Keeps staking until the thread is interrupted */
    private void staking() {
        PoSBlockInterface block;

        byte[] nonce;

        while (!Thread.currentThread().isInterrupted()) {
            if ((block = proposingBlock) == null) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    break;
                }
            } else {

                // A new array must be created each loop
                // If reference is reused the array contents may be changed
                // before block sealed causing validation to fail
                nonce = new byte[32];
                ThreadLocalRandom.current().nextBytes(nonce);

                Solution s = miner.mine(block, nonce);
                if (s != null) {
                    IEvent ev = new EventConsensus(EventConsensus.CALLBACK.ON_SOLUTION);
                    ev.setFuncArgs(Collections.singletonList(s));
                    evtMgr.newEvent(ev);
                }
            }
        }
    }

    /** Restart the mining process when a new block template is received. */
    private void onBlockTemplate(AionBlock block) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("onBlockTemplate(): {}", toHexString(block.getHash()));
        }

        // Do not change reference if the event passes a null reference
        if (isStaking() && block != null) {
            proposingBlock = block;
        }
    }

    /**
     * Start block mining after sec seconds
     *
     * @param sec The number of seconds to wait until beginning to mine blocks
     */
    @Override
    public void delayedStartStaking(int sec) {
        if (cfg.getConsensus().getStaking()) {
            LOG.info("<delayed-start-staking>");
            Timer t = new Timer();
            t.schedule(
                    new TimerTask() {
                        @Override
                        public void run() {
                            if (cfg.getConsensus().getStaking()) {
                                startStaking();
                            }
                        }
                    },
                    sec * 1000);
        } else {
            LOG.info("<staking-disabled>");
        }
    }

    /** This miner will listen to the ON_BLOCK_TEMPLATE event from the consensus handler. */
    private void registerCallback() {
        // Only register events if actual mining
        if (cfg.getConsensus().getMining()) {
            if (this.evtMgr != null) {
                IHandler hdrCons = this.evtMgr.getHandler(4);
                if (hdrCons != null) {
                    hdrCons.eventCallback(new EventCallback(ees, LOG));
                }
            } else {
                LOG.error("event manager is null");
            }
        }
    }

    /** Register miner events. */
    private void registerMinerEvents() {
        List<IEvent> evts = new ArrayList<>();
        evts.add(new EventMiner(EventMiner.CALLBACK.MININGSTARTED));
        evts.add(new EventMiner(EventMiner.CALLBACK.MININGSTOPPED));
        this.evtMgr.registerEvent(evts);
    }

    @Override
    protected void fireRunnerStarted() {
        if (this.evtMgr != null) {
            this.evtMgr.newEvent(new EventMiner(EventMiner.CALLBACK.MININGSTARTED));
        }
    }

    @Override
    protected void fireRunnerStopped() {
        if (this.evtMgr != null) {
            this.evtMgr.newEvent(new EventMiner(EventMiner.CALLBACK.MININGSTOPPED));
        }
    }

    public void shutdown() {
        ees.shutdown();
    }
}
