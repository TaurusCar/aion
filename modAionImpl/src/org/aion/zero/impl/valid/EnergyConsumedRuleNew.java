package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

/**
 * Rule for checking that energyConsumed does not exceed energyLimit:
 * assert(blockHeader.energyConsumed <= blockHeader.energyLimit)
 */
public class EnergyConsumedRuleNew implements BlockHeaderRuleInterface {

    @Override
    public boolean validate(BlockHeader blockHeader, List<RuleError> error) {
        if (blockHeader.getEnergyConsumed() > blockHeader.getEnergyLimit()) {
            addError(
                    formatError(blockHeader.getEnergyConsumed(), blockHeader.getEnergyLimit()),
                    error);
            return false;
        }
        return true;
    }

    private static String formatError(long energyConsumed, long energyLimit) {
        return "energyConsumed (" + energyConsumed + ") > energyLimit(" + energyLimit + ")";
    }
}
