package org.aion.mcf.blockchain;

import java.math.BigInteger;
import org.aion.interfaces.block.Block;
import org.aion.mcf.core.AbstractTxInfo;
import org.aion.mcf.db.IBlockStoreBase;
import org.aion.vm.api.types.Hash256;

/**
 * unity protocol blockchain interface.
 *
 * @param <BLK>
 */
@SuppressWarnings("rawtypes")
public interface PosChain<BLK extends Block> {

    BigInteger getTotalDifficulty();

    void setTotalDifficulty(BigInteger totalDifficulty);

    BigInteger getTotalDifficultyByHash(Hash256 hash);

    //BLK getPowBlockByNumber(long number);

    //BLK getPowBlockByHash(byte[] hash);

    BLK getBlockByNumber(long number);

    BLK getBlockByHash(byte[] hash);

    IBlockStoreBase<?, ?> getBlockStore();

    BLK getBestBlock();

    AbstractTxInfo getTransactionInfo(byte[] hash);

    void flush();
}
