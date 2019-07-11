package org.aion.zero.impl.valid;

import java.math.BigInteger;
import java.util.List;
import org.aion.crypto.HashUtil;
import org.aion.interfaces.block.BlockHeader;
import org.aion.mcf.blockchain.valid.BlockHeaderRuleInterface;
import org.aion.zero.types.StakedBlockHeader;

/** Checks proof value against its boundary for the block header */
public class AionPOSRule implements BlockHeaderRuleInterface {

    private static BigInteger boundry = BigInteger.TWO.pow(256);

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors, Object... extraArgs) {

        if (extraArgs == null || extraArgs.length != 2) {
            return false;
        }

        long parentTimeStamp = (long) extraArgs[0];
        BigInteger stake = (BigInteger) extraArgs[1];

        long timeStamp = header.getTimestamp();
        BigInteger blockDifficulty = header.getDifficultyBI();

        BigInteger divide =
                new BigInteger(1, HashUtil.h256(((StakedBlockHeader) header).getSeed()));

        double delta =
                blockDifficulty.doubleValue()
                        * Math.log(boundry.divide(divide).doubleValue())
                        / stake.doubleValue();

        if (timeStamp < (parentTimeStamp + delta)) {
            addError(formatError(timeStamp, parentTimeStamp, delta), errors);
            return false;
        }

        return true;
    }

    @Override
    public boolean validate(BlockHeader header, List<RuleError> errors) {
        throw new IllegalStateException("Un-expect method call!");
    }

    private static String formatError(long timeStamp, long parantTimeStamp, double delta) {
        return "block timestamp output ("
                + timeStamp
                + ") violates boundary condition ( paraentTimeStamp:"
                + parantTimeStamp
                + " delta:"
                + delta
                + ")";
    }

    @Override
    public boolean extraValidateArg() {
        return true;
    }
}
