package org.aion.mcf.valid;

import java.util.LinkedList;
import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRule;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface.RuleError;
import org.aion.mcf.blockchain.valid.IValidRule;
import org.aion.mcf.types.AbstractBlockHeader;
import org.slf4j.Logger;

public class BlockHeaderValidatorNew {

    private List<BlockHeaderRuleInterface> rules;

    public BlockHeaderValidatorNew(List<BlockHeaderRuleInterface> rules) {
        this.rules = rules;
    }

    public boolean validate(BlockHeader header, Logger logger) {
        List<RuleError> errors = new LinkedList<>();
        for (BlockHeaderRuleInterface rule : rules) {
            if (!rule.validate(header, errors)) {
                if (logger != null) logErrors(logger, errors);
                return false;
            }
        }
        return true;
    }

    public boolean validate(BlockHeader header, Logger logger, Object... extraValidationArg) {
        List<RuleError> errors = new LinkedList<>();
        for (BlockHeaderRuleInterface rule : rules) {
            if (rule.extraValidateArg()) {
                if (!rule.validate(header, errors, extraValidationArg)) {
                    if (logger != null) logErrors(logger, errors);
                    return false;
                }

            } else {
                if (!rule.validate(header, errors)) {
                    if (logger != null) logErrors(logger, errors);
                    return false;
                }
            }
        }
        return true;
    }

    private void logErrors(final Logger logger, final List<RuleError> errors) {
        if (errors.isEmpty()) return;

        if (logger.isErrorEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append(this.getClass().getSimpleName());
            builder.append(" raised errors: \n");
            for (RuleError error : errors) {
                builder.append(error.errorClass.getSimpleName());
                builder.append("\t\t\t\t");
                builder.append(error.error);
                builder.append("\n");
            }
            logger.error(builder.toString());
        }
    }
}
