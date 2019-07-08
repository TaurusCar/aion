package org.aion.zero.types;

import java.math.BigInteger;
import java.util.List;
import org.aion.interfaces.block.Block;
import org.aion.types.AionAddress;

/** pos block interface. */
public interface PoSBlockInterface extends Block<AionTransaction, StakedBlockHeader> {

    AionAddress getCoinbase();

    long getTimestamp();

    byte[] getDifficulty();

    byte[] getStateRoot();

    void setStateRoot(byte[] stateRoot);

    BigInteger getCumulativeDifficulty();

    byte[] getReceiptsRoot();

    byte[] getTxTrieRoot();

    byte[] getLogBloom();

    void setSeed(byte[] seed);

    List<AionTransaction> getTransactionsList();

    long getNrgConsumed();

    long getNrgLimit();
}
