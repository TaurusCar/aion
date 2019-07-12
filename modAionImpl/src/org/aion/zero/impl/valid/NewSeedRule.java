package org.aion.zero.impl.valid;

import java.util.Arrays;
import java.util.List;
import org.aion.crypto.ed25519.ECKeyEd25519;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.util.bytes.ByteUtil;
import org.aion.zero.types.StakedBlockHeader;

/** Checks proof value against its boundary for the block header */
public class NewSeedRule implements BlockHeaderRuleInterface {

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        if (extraArgs == null || extraArgs.length < 1) {
            return false;
        }

        BlockHeader parentHeader = (BlockHeader) extraArgs[0];
        if (parentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        byte[] oldSeed = ((StakedBlockHeader) parentHeader).getSeed();
        byte[] newSeed = ((StakedBlockHeader) header).getSeed();
        byte[] pk = ((StakedBlockHeader) header).getPubKey();

        if (!ECKeyEd25519.verify(oldSeed, newSeed, pk)) {
            addError(formatError(oldSeed, newSeed, pk), errors);
            return false;
        }

        return true;
    }

    private static String formatError(byte[] seed, byte[] parentSeed, byte[] pubkey) {
        return "block seed output ("
                + ByteUtil.toHexString(seed)
                + ") violates seed ( parentSeed:"
                + ByteUtil.toHexString(parentSeed)
                + ") and public key condition ( publicKey:"
                + ByteUtil.toHexString(pubkey)
                + ")";
    }
}
