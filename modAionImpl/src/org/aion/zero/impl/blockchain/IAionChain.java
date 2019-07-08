package org.aion.zero.impl.blockchain;

import java.math.BigInteger;
import java.util.List;
import org.aion.mcf.blockchain.ChainInstanceHybrid;
import org.aion.mcf.blockchain.PosChain;
import org.aion.types.AionAddress;
import org.aion.interfaces.db.Repository;
import org.aion.zero.impl.AionHub;
import org.aion.zero.impl.query.QueryInterface;
import org.aion.zero.impl.types.AionBlock;
import org.aion.zero.impl.types.AionPoSBlock;
import org.aion.zero.types.A0BlockHeader;
import org.aion.zero.types.AionTransaction;
import org.aion.zero.types.AionTxReceipt;
import org.aion.zero.types.IAionBlock;
import org.aion.zero.types.StakedBlockHeader;

/** Aion chain interface. */
public interface IAionChain extends ChainInstanceHybrid, QueryInterface {

    PosChain<AionBlock, A0BlockHeader> getBlockchain();

    PosChain<AionPoSBlock, StakedBlockHeader> getPosChain();


    void close();

    AionTransaction createTransaction(BigInteger nonce, AionAddress to, BigInteger value, byte[] data);

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
