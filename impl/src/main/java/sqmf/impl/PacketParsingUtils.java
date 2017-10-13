/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package sqmf.impl;

import java.util.Arrays;

/**
 * THIS CLASS HAS BEEN FOUND ON :
 * https://github.com/sdnhub/SDNHub_Opendaylight_Tutorial/blob/master/commons/utils/src/main/java/org/
 * sdnhub/odl/tutorial/utils/PacketParsingUtils.java
 */
public abstract class PacketParsingUtils {

    private static final int IP_PROTOCOL_START_POSITION = 23;

    /**
     * start position of source MAC address in array
     */
    private static final int SRC_MAC_START_POSITION = 6;

    /**
     * end position of source MAC address in array
     */
    private static final int SRC_MAC_END_POSITION = 12;


    private PacketParsingUtils() {
        //prohibite to instantiate this class
    }

    /**
     * @param payload
     * @return source MAC address
     */
    public static byte[] extractSrcMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_MAC_START_POSITION, SRC_MAC_END_POSITION);
    }


    public static String rawMacToString(byte[] rawMac) {
        if (rawMac != null && rawMac.length == 6) {
            StringBuffer sb = new StringBuffer();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            return sb.substring(1);
        }
        return null;
    }

    public static byte extractIPprotocol(final byte[] payload) {
        return payload[IP_PROTOCOL_START_POSITION];
    }

}