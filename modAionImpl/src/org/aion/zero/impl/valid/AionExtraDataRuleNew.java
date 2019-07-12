package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

public class AionExtraDataRuleNew implements BlockHeaderRuleInterface {

    private final int maximumExtraDataSize;

    public AionExtraDataRuleNew(int _maximumExtraDataSize) {
        if (_maximumExtraDataSize < 0)
            throw new IllegalArgumentException("extra data size must be >= 0");

        maximumExtraDataSize = _maximumExtraDataSize;
    }

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {
        if (header.getExtraData() != null && header.getExtraData().length > maximumExtraDataSize) {
            addError(
                    String.format(
                            "extraData (%d) > MAXIMUM_EXTRA_DATA_SIZE (%d)",
                            header.getExtraData().length, maximumExtraDataSize),
                    errors);
            return false;
        }
        return true;
    }

    @Override
    public void addError(String error, List<RuleError> errors) {
        errors.add(new RuleError(this.getClass(), error));
    }
}
