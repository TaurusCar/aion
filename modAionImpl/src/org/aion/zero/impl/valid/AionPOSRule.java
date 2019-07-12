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

        if (extraArgs == null || extraArgs.length < 3) {
            return false;
        }

        BlockHeader parentHeader = (BlockHeader) extraArgs[0];
        if (parentHeader == null) {
            throw new IllegalStateException("Invalid parent header input");
        }

        long parentTimeStamp = parentHeader.getTimestamp();

        BigInteger stake = (BigInteger) extraArgs[2];
        if (stake == null) {
            throw new IllegalStateException("Invalid stake input");
        }

        long timeStamp = header.getTimestamp();
        BigInteger blockDifficulty = header.getDifficultyBI();

        BigInteger dividend =
                new BigInteger(1, HashUtil.h256(((StakedBlockHeader) header).getSeed()));

        double delta =
                blockDifficulty.doubleValue()
                        * Math.log(boundry.divide(dividend).doubleValue())
                        / stake.doubleValue();

        if (timeStamp < (parentTimeStamp + delta)) {
            addError(formatError(timeStamp, parentTimeStamp, delta), errors);
            return false;
        }

        return true;
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
}
