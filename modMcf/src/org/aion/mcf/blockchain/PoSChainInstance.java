package org.aion.mcf.blockchain;

import org.aion.mcf.stake.IStakeRunner;

/** Chain instance pow interface. */
public interface PoSChainInstance {

    PosChain<?> getBlockchain();

    IStakeRunner getBlockMiner();
}
