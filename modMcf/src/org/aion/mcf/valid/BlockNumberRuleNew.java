package org.aion.mcf.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

public class BlockNumberRuleNew implements BlockHeaderRuleInterface {
    private static String formatError(long headerNumber, long parentNumber) {
        return "blockNumber ("
                + headerNumber
                + ") is not equal to parentBlock number + 1 ("
                + parentNumber
                + ")";
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

        if (header.getNumber() != (parentHeader.getNumber() + 1)) {
            addError(formatError(header.getNumber(), parentHeader.getNumber()), errors);
            return false;
        }
        return true;
    }
}
