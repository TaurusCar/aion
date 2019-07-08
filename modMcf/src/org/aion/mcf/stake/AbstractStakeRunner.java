package org.aion.mcf.stake;

import org.aion.interfaces.block.Block;
import org.aion.log.AionLoggerFactory;
import org.aion.log.LogEnum;
import org.aion.mcf.mine.IMineRunner;
import org.slf4j.Logger;

/** Abstract Miner. */
public abstract class AbstractStakeRunner<BLK extends Block<?, ?>> implements IStakeRunner {

    protected static final Logger LOG = AionLoggerFactory.getLogger(LogEnum.CONS.name());

    protected boolean isStaking;

    protected volatile BLK proposingBlock;

    public boolean isStaking() {
        return isStaking;
    }

    protected abstract void fireRunnerStarted();

    protected abstract void fireRunnerStopped();
}
