package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.zero.types.BlockHeaderSealType;
import org.aion.zero.types.StakedBlockHeader;

public class HeaderSealTypeRule implements BlockHeaderRuleInterface {

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors) {
        if (!BlockHeaderSealType.isActive(((StakedBlockHeader)header).getSealType())) {
            addError(
                    "Invalid header sealType, found sealType "
                            + ((StakedBlockHeader)header).getSealType()
                            + " expected one of "
                            + BlockHeaderSealType.activeTypes(),
                    errors);
            return false;
        }
        return true;
    }
}
