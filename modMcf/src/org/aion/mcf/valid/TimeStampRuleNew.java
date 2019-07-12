package org.aion.mcf.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

/** Validates whether the timestamp of the current block is > the timestamp of the parent block */
public class TimeStampRuleNew implements BlockHeaderRuleInterface {

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        if (extraArgs == null || extraArgs.length < 1) {
            throw new IllegalStateException("Invalid validation args input");
        }

        BlockHeader parentHeader = (BlockHeader) extraArgs[0];
        if (parentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        if (header.getTimestamp() <= parentHeader.getTimestamp()) {
            addError(
                    "timestamp ("
                            + header.getTimestamp()
                            + ") is not greater than parent timestamp ("
                            + parentHeader.getTimestamp()
                            + ")",
                    errors);
            return false;
        }
        return true;
    }
}
