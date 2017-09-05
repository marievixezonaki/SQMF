/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev100924.MacAddress;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public abstract class PacketParsingUtils {

    private static final int IP_SRC_START_POSITION = 26;
    private static final int IP_SRC_END_POSITION = 30;
    private static final int IP_DST_START_POSITION = 30;
    private static final int IP_DST_END_POSITION = 34;

    private static final int IP_PROTOCOL_START_POSITION = 23;


    private static final int DEST_PORT_START_POSITION = 36;
    private static final int DEST_PORT_END_POSITION = 38;

    /**
     * size of MAC address in octets (6*8 = 48 bits)
     */
    private static final int MAC_ADDRESS_SIZE = 6;

    /**
     * start position of destination MAC address in array
     */
    private static final int DST_MAC_START_POSITION = 0;

    /**
     * end position of destination MAC address in array
     */
    private static final int DST_MAC_END_POSITION = 6;

    /**
     * start position of source MAC address in array
     */
    private static final int SRC_MAC_START_POSITION = 6;

    /**
     * end position of source MAC address in array
     */
    private static final int SRC_MAC_END_POSITION = 12;

    /**
     * start position of ethernet type in array
     */
    private static final int ETHER_TYPE_START_POSITION = 12;

    /**
     * end position of ethernet type in array
     */
    private static final int ETHER_TYPE_END_POSITION = 14;

    private PacketParsingUtils() {
        //prohibite to instantiate this class
    }

    /**
     * @param payload
     * @return destination MAC address
     */
    public static byte[] extractDstMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, DST_MAC_START_POSITION, DST_MAC_END_POSITION);
    }

    /**
     * @param payload
     * @return source MAC address
     */
    public static byte[] extractSrcMac(final byte[] payload) {
        return Arrays.copyOfRange(payload, SRC_MAC_START_POSITION, SRC_MAC_END_POSITION);
    }

    /**
     * @param payload
     * @return source MAC address
     */
    public static byte[] extractEtherType(final byte[] payload) {
        return Arrays.copyOfRange(payload, ETHER_TYPE_START_POSITION, ETHER_TYPE_END_POSITION);
    }

    /**
     * @param rawMac
     * @return {@link MacAddress} wrapping string value, baked upon binary MAC
     *         address
     */
    public static MacAddress rawMacToMac(final byte[] rawMac) {
        MacAddress mac = null;
        if (rawMac != null && rawMac.length == MAC_ADDRESS_SIZE) {
            StringBuilder sb = new StringBuilder();
            for (byte octet : rawMac) {
                sb.append(String.format(":%02X", octet));
            }
            mac = new MacAddress(sb.substring(1));
        }
        return mac;
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

    public static byte[] stringMacToRawMac(String address) {
        String[] elements = address.split(":");
        if (elements.length != MAC_ADDRESS_SIZE) {
            throw new IllegalArgumentException(
                    "Specified MAC Address must contain 12 hex digits" +
                            " separated pairwise by :'s.");
        }

        byte[] addressInBytes = new byte[MAC_ADDRESS_SIZE];
        for (int i = 0; i < MAC_ADDRESS_SIZE; i++) {
            String element = elements[i];
            addressInBytes[i] = (byte)Integer.parseInt(element, 16);
        }
        return addressInBytes;
    }


    public static int extractDestPort(final byte[] payload) {
        return 0xffff & ByteBuffer.wrap(Arrays.copyOfRange(payload,  DEST_PORT_START_POSITION  , DEST_PORT_END_POSITION )).getShort();
    }

    public static byte extractIPprotocol(final byte[] payload) {
        return payload[IP_PROTOCOL_START_POSITION];
    }

    public static byte[] extractSrcIP(final byte[] payload) {
        return Arrays.copyOfRange(payload,  IP_SRC_START_POSITION , IP_SRC_END_POSITION);
    }

    public static byte[] extractDstIP(final byte[] payload) {
        return Arrays.copyOfRange(payload, IP_DST_START_POSITION, IP_DST_END_POSITION);
    }

    public static String rawIpToString(final byte[] rawIp) {
        if (rawIp.length != 4)
            return "";
        StringBuilder sb = new StringBuilder();
        for (byte octet : rawIp) {
            sb.append(String.format("%d.", octet));
        }
        return sb.substring(0, sb.length()-1);
    }
}