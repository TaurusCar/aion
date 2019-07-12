package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

/**
 * Rule for checking that energyConsumed does not exceed energyLimit:
 * assert(blockHeader.energyConsumed <= blockHeader.energyLimit)
 */
public class EnergyConsumedRuleNew implements BlockHeaderRuleInterface {

    private static String formatError(long energyConsumed, long energyLimit) {
        return "energyConsumed (" + energyConsumed + ") > energyLimit(" + energyLimit + ")";
    }

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {
        if (header.getEnergyConsumed() > header.getEnergyLimit()) {
            addError(formatError(header.getEnergyConsumed(), header.getEnergyLimit()), errors);
            return false;
        }
        return true;
    }
}
