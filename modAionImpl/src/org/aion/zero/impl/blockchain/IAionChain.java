package org.aion.zero.impl.blockchain;

import java.math.BigInteger;
import java.util.List;
import org.aion.mcf.blockchain.PoSChainInstance;
import org.aion.mcf.blockchain.PosChain;
import org.aion.types.AionAddress;
import org.aion.interfaces.db.Repository;
import org.aion.zero.impl.AionHub;
import org.aion.zero.impl.query.QueryInterface;
import org.aion.zero.impl.types.AionPoSBlock;
import org.aion.zero.types.AionTransaction;
import org.aion.zero.types.AionTxReceipt;
import org.aion.zero.types.PoSBlockInterface;

/** Aion chain interface. */
public interface IAionChain extends PoSChainInstance, QueryInterface {

    //IPowChain<AionPoSBlock, StakedBlockHeader> getBlockchain();

    PosChain<AionPoSBlock> getBlockchain();


    void close();

    AionTransaction createTransaction(BigInteger nonce, AionAddress to, BigInteger value, byte[] data);

    void broadcastTransaction(AionTransaction transaction);

    AionTxReceipt callConstant(AionTransaction tx, PoSBlockInterface block);

    Repository<?, ?> getRepository();

    Repository<?, ?> getPendingState();

    Repository<?, ?> getSnapshotTo(byte[] root);

    List<AionTransaction> getWireTransactions();

    List<AionTransaction> getPendingStateTransactions();

    AionHub getAionHub();

    void exitOn(long number);

    long estimateTxNrg(AionTransaction tx, PoSBlockInterface block);
}
