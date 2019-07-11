package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;

public class AionExtraDataRuleNew implements BlockHeaderRuleInterface {

    private final int maximumExtraDataSize;

    public AionExtraDataRuleNew(int maximumExtraDataSize) {
        if (maximumExtraDataSize < 0)
            throw new IllegalArgumentException("extra data size must be >= 0");

        this.maximumExtraDataSize = maximumExtraDataSize;
    }

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors) {
        if (header.getExtraData() != null
                && header.getExtraData().length > this.maximumExtraDataSize) {
            addError(
                    String.format(
                            "extraData (%d) > MAXIMUM_EXTRA_DATA_SIZE (%d)",
                            header.getExtraData().length, this.maximumExtraDataSize),
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
