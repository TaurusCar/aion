package org.aion.mcf.blockchain;

import java.math.BigInteger;
import org.aion.interfaces.block.Block;
import org.aion.mcf.types.AbstractBlockHeader;
import org.aion.vm.api.types.Hash256;

/**
 * unity protocol blockchain interface.
 *
 * @param <BLK>
 * @param <BH>
 */
@SuppressWarnings("rawtypes")
public interface PowChain<BLK extends Block, BH extends AbstractBlockHeader> {

    BigInteger getPosTotalDifficulty();

    void setPosTotalDifficulty(BigInteger totalDifficulty);

    BigInteger getPosTotalDifficultyByHash(Hash256 hash);

    BLK getPosBlockByNumber(long number);

    BLK getPosBlockByHash(byte[] hash);

    BLK getBestPosBlock();
}
