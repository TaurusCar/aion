package org.aion.mcf.stake;

public interface IStakeRunner {

    void startStaking();

    void stopStaking();

    void delayedStartStaking(int sec);

    boolean isStaking();

    void shutdown();
}
