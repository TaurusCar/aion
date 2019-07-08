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
public interface PosChain<BLK extends Block, BH extends AbstractBlockHeader> {

    BigInteger getPowTotalDifficulty();

    void setPowTotalDifficulty(BigInteger totalDifficulty);

    BigInteger getPowTotalDifficultyByHash(Hash256 hash);

    BLK getPowBlockByNumber(long number);

    BLK getPowBlockByHash(byte[] hash);

    BLK getBestPowBlock();
}
