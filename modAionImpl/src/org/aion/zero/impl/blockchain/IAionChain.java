package org.aion.zero.impl.blockchain;

import java.util.List;
import org.aion.base.AionTransaction;
import org.aion.mcf.blockchain.IChainInstancePOW;
import org.aion.mcf.blockchain.IPowChain;
import org.aion.mcf.db.Repository;
import org.aion.zero.impl.AionHub;
import org.aion.zero.impl.query.QueryInterface;
import org.aion.zero.impl.types.AionBlock;
import org.aion.zero.types.A0BlockHeader;
import org.aion.zero.types.AionTxReceipt;
import org.aion.zero.types.IAionBlock;

/** Aion chain interface. */
public interface IAionChain extends IChainInstancePOW, QueryInterface {

    IPowChain<AionBlock, A0BlockHeader> getBlockchain();

    void close();

    void broadcastTransaction(AionTransaction transaction);

    AionTxReceipt callConstant(AionTransaction tx, IAionBlock block);

    Repository<?, ?> getRepository();

    Repository<?, ?> getPendingState();

    Repository<?, ?> getSnapshotTo(byte[] root);

    List<AionTransaction> getWireTransactions();

    List<AionTransaction> getPendingStateTransactions();

    AionHub getAionHub();

    void exitOn(long number);

    long estimateTxNrg(AionTransaction tx, IAionBlock block);
}
