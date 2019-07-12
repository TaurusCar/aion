package org.aion.mcf.blockchain.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;

/** Block header rules. */
public interface BlockHeaderRuleInterface {
    class RuleError {
        public final Class<?> errorClass;
        public final String error;

        public RuleError(Class<?> errorClass, String error) {
            this.errorClass = errorClass;
            this.error = error;
        }
    }

    boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs);

    default void addError(String error, List<RuleError> errors) {
        errors.add(new RuleError(this.getClass(), error));
    }
}
