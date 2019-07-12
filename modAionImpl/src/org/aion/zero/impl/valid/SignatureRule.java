package org.aion.zero.impl.valid;

import java.util.Arrays;
import java.util.List;
import org.aion.crypto.ed25519.ECKeyEd25519;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.util.bytes.ByteUtil;
import org.aion.zero.types.StakedBlockHeader;

/** Checks proof value against its boundary for the block header */
public class SignatureRule implements BlockHeaderRuleInterface {

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        byte[] mineHash = ((StakedBlockHeader) header).getMineHash();
        byte[] pk = ((StakedBlockHeader) header).getPubKey();
        byte[] sig = ((StakedBlockHeader) header).getSignature();


        if (!ECKeyEd25519.verify(mineHash, sig, pk)) {
            addError(formatError(mineHash, ByteUtil.merge(pk, sig)), errors);
            return false;
        }

        return true;
    }

    private static String formatError(byte[] hash, byte[] sig) {
        return "block hash output ("
                + ByteUtil.toHexString(hash)
                + ") violates signature condition ( signature:"
                + ByteUtil.toHexString(sig)
                + ")";
    }
}
