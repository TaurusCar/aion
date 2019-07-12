package org.aion.zero.impl.valid;

import static org.aion.util.biginteger.BIUtil.isEqual;

import java.math.BigInteger;
import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.IChainCfg;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.mcf.core.IDifficultyCalculator;
import org.aion.mcf.valid.GrandParentDependantBlockHeaderRule;
import org.aion.zero.types.AionTransaction;
import org.aion.zero.types.StakedBlockHeader;

/** Checks block's difficulty against calculated difficulty value */
public class PoSDifficultyRule implements BlockHeaderRuleInterface {

    private IDifficultyCalculator diffCalc;

    public PoSDifficultyRule(IChainCfg<AionTransaction> configuration) {
        diffCalc = configuration.getDifficultyCalculator();
    }

    private static String formatError(BigInteger expectedDifficulty, BigInteger actualDifficulty) {
        return "difficulty ("
                + actualDifficulty
                + ") != expected difficulty ("
                + expectedDifficulty
                + ")";
    }

    /**
     * @inheritDoc
     * @implNote There is a special case in block 1 where we do not have a grandparent, to get
     *     around this we must apply a different rule.
     *     <p>Currently that rule will be defined to "pass on" the difficulty of the parent block
     *     {@code block 0} to the current block {@code block 1} If the current Header has invalid
     *     difficulty length, will return {BigInteger.ZERO}.
     */
    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        if (extraArgs == null || extraArgs.length < 2) {
            throw new IllegalStateException("Invalid validation args input");
        }

        BigInteger currDiff = header.getDifficultyBI();
        if (currDiff.equals(BigInteger.ZERO)) {
            return false;
        }

        BlockHeader parentHeader = (BlockHeader) extraArgs[0];
        if (parentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        if (parentHeader.getNumber() == 0L) {
            if (!isEqual(parentHeader.getDifficultyBI(), currDiff)) {
                addError(formatError(parentHeader.getDifficultyBI(), currDiff), errors);
                return false;
            }
            return true;
        }

        BlockHeader grandParentHeader = (BlockHeader) extraArgs[1];
        if (grandParentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        BigInteger calcDifficulty =
                this.diffCalc.calculateDifficulty(parentHeader, grandParentHeader);

        if (!isEqual(calcDifficulty, currDiff)) {
            addError(formatError(calcDifficulty, currDiff), errors);
            return false;
        }
        return true;
    }
}
