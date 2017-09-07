/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.opendaylight.yangtools.concepts.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;

public class PacketProcessing implements PacketProcessingListener {

    private final Logger LOG = LoggerFactory.getLogger(PacketProcessing.class);
    private List<String> dstMacs;

    public PacketProcessing() {
        LOG.info("PacketProcessing loaded successfully");
        dstMacs = new LinkedList<>();
    }

    @Override
    public void onPacketReceived(PacketReceived packetReceived) {

        byte[] payload = packetReceived.getPayload();
        String protocol;
        byte p = PacketParsingUtils.extractIPprotocol(payload);
        if (p == 0x11) {
            protocol = "UDP";
            System.out.print("Received UDP packet");
        }

     //   System.out.println("Packet received");

/*
        byte[] srcMacRaw = PacketParsingUtils.extractSrcMac(payload);
        String srcMac = PacketParsingUtils.rawMacToString(srcMacRaw);
        LOG.info(srcMac);
        byte[] dstMacRaw = PacketParsingUtils.extractDstMac(payload);
        String dstMac = PacketParsingUtils.rawMacToString(dstMacRaw);
        LOG.info(dstMac);

        LOG.info("\n\n\n\n --------------------------------------");

        byte[] srcIpRaw = PacketParsingUtils.extractSrcIP(payload);
        String srcIp = PacketParsingUtils.rawIpToString(srcIpRaw);
        LOG.info("Source IP: " + srcIp);
        byte[] dstIpRaw = PacketParsingUtils.extractDstIP(payload);
        String dstIp = PacketParsingUtils.rawIpToString(dstIpRaw);
*/
//        LOG.info("Destination IP: " + dstIp);

        byte[] srcMacRaw = PacketParsingUtils.extractSrcMac(payload);
        String srcMac = PacketParsingUtils.rawMacToString(srcMacRaw);

        if(srcMac.equals("00:00:00:00:00:09")) {
            System.out.println("pacekt addrwess matched");
            Long timeNow = System.currentTimeMillis();
            Long latency = timeNow - PacketSender.sentTime;
            LatencyMonitor.latency = latency;

          /*  System.out.println("Got the packet    " + System.currentTimeMillis());
            System.out.println("latency is " + (System.currentTimeMillis() - PacketSender.sentTime ) );
*/
        }

        byte[] dstMacRaw = PacketParsingUtils.extractDstMac(payload);
        String dstMac = PacketParsingUtils.rawMacToString(dstMacRaw);
/*        String protocol;
        byte p = PacketParsingUtils.extractIPprotocol(payload);
        if (p == 0x11)
            protocol = "UDP";
        else
            protocol = "TCP";
*/
   //     int port = PacketParsingUtils.extractDestPort(payload);

  /*      if (isDestination(dstMac)) {
            forwardPacket(srcMac, dstMac, srcIp, dstIp);
        }
*/

    }

    public void addDestMac(String address) {
        dstMacs.add(address);
    }

    public boolean isDestination(String address) {
        for (String mac : dstMacs) {
            if (mac.equals(address)) {
                return true;
            }
        }
        return false;
    }

}