package org.aion.zero.impl.valid;

import java.util.List;
import org.aion.mcf.blockchain.valid.BlockHeaderRule;
import org.aion.mcf.types.AbstractBlockHeader;
import org.aion.zero.types.BlockHeaderSealType;

public class AionHeaderVersionRule<H extends AbstractBlockHeader> extends BlockHeaderRule<H> {

    @Override
    public boolean validate(H header, List<RuleError> errors) {
        if (!BlockHeaderSealType.isActive(header.getSealType())) {
            addError(
                    "Invalid version, found version "
                            + header.getSealType()
                            + " expected one of "
                            + BlockHeaderSealType.activeTypes(),
                    errors);
            return false;
        }
        return true;
    }
}
