package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.mcf.types.AbstractBlockHeader;
import org.aion.mcf.valid.DependentBlockHeaderRule;

/**
 * Energy limit rule is defined as the following (no documentation yet)
 *
 * <p>if EnergyLimit(n) > MIN_ENERGY EnergyLimit(n-1) - EnergyLimit(n-1)/1024 <= EnergyLimit(n) <=
 * EnergyLimit(n-1) + EnergyLimit(n-1)/1024
 *
 * <p>This rule depends on the parent to implement
 */
public class EnergyLimitRuleNew implements BlockHeaderRuleInterface {

    private final long energyLimitDivisor;
    private final long energyLimitLowerBounds;

    public EnergyLimitRuleNew(long _energyLimitDivisor, long _energyLimitLowerBounds) {
        energyLimitDivisor = _energyLimitDivisor;
        energyLimitLowerBounds = _energyLimitLowerBounds;
    }

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        if (extraArgs == null || extraArgs.length < 1) {
            throw new IllegalStateException("Invalid validation args input");
        }

        BlockHeader parentHeader = (BlockHeader) extraArgs[0];
        if (parentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        long energyLimit = header.getEnergyLimit();
        long parentEnergyLimit = parentHeader.getEnergyLimit();
        long parentEnergyQuotient = parentEnergyLimit / energyLimitDivisor;

        // check that energy is atleast equal to lower bounds, otherwise block is invalid
        if (energyLimit < energyLimitLowerBounds) {
            addError(
                    "energyLimit ("
                            + energyLimit
                            + ") lower than lower bound ("
                            + energyLimitLowerBounds
                            + ")",
                    errors);
            return false;
        }

        // magnitude of distance between parent energy and current energy
        long energyDeltaMag = Math.abs(energyLimit - parentEnergyLimit);
        if (energyDeltaMag > parentEnergyQuotient) {
            addError(
                    "energyLimit ("
                            + energyLimit
                            + ") of current block has delta ("
                            + energyDeltaMag
                            + ") greater than bounds ("
                            + parentEnergyQuotient
                            + ")",
                    errors);
            return false;
        }
        return true;
    }
}
