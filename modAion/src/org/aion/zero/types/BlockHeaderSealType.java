package org.aion.zero.types;

import java.util.HashSet;
import java.util.Set;

public class BlockHeaderSealType {

    private static Set<Byte> active =
            new HashSet<>() {
                {
                    this.add((byte) 0x01);
                    this.add((byte) 0x02);
                }
            };

    public static boolean isActive(byte version) {
        return active.contains(version);
    }

    public static String activeTypes() {
        StringBuilder toReturn = new StringBuilder("{");

        for (Byte aByte : active) {
            toReturn.append(aByte);
        }

        return toReturn + "}";
    }
}
