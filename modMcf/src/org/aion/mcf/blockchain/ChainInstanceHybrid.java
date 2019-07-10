package org.aion.mcf.blockchain;

import org.aion.mcf.mine.IMineRunner;
import org.aion.mcf.stake.IStakeRunner;

/** Chain instance pow interface. */
public interface ChainInstanceHybrid extends IChainInstanceBase {

    PosChain<?> getBlockchain();

    IMineRunner getPowMiner();

    IStakeRunner getPosStaker();
}
