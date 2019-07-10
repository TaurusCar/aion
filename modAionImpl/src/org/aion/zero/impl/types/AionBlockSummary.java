package org.aion.zero.impl.types;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import org.aion.interfaces.block.Block;
import org.aion.types.AionAddress;
import org.aion.interfaces.block.BlockSummary;
import org.aion.mcf.types.AbstractBlockSummary;
import org.aion.zero.types.AionTransaction;
import org.aion.zero.types.AionTxExecSummary;
import org.aion.zero.types.AionTxReceipt;

/**
 * Modified to add transactions
 *
 * @author yao
 */
public class AionBlockSummary
        extends AbstractBlockSummary<AionTransaction, AionTxReceipt, AionTxExecSummary>
        implements BlockSummary {

    public AionBlockSummary(
            Block block,
            Map<AionAddress, BigInteger> rewards,
            List<AionTxReceipt> receipts,
            List<AionTxExecSummary> summaries) {
        this.block = block;
        this.rewards = rewards;
        this.receipts = receipts;
        this.summaries = summaries;
    }
}
