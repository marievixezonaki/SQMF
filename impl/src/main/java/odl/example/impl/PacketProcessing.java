/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.LinkedList;
import java.util.List;

public class PacketProcessing implements PacketProcessingListener {

    private final Logger LOG = LoggerFactory.getLogger(PacketProcessing.class);
    private List<String> dstMacs;
    public static Integer ingressUdpPackets = 0;
    public static Integer egressUdpPackets = 0;
    public String srcNode;
    public String dstNode;
    private String sourceMac;

    public PacketProcessing(String srcNode, String dstNode, String srcMac) {
        LOG.info("PacketProcessing loaded successfully");
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.sourceMac = srcMac;
        dstMacs = new LinkedList<>();
    }

    @Override
    public void onPacketReceived(PacketReceived packetReceived) {

        byte[] payload = packetReceived.getPayload();
        byte protocol = PacketParsingUtils.extractIPprotocol(payload);
        if (protocol == 0x11) {
            Match match = packetReceived.getMatch();
            String[] matchParts = match.getInPort().getValue().split(":");
            String switchWhichReceivedPacket = matchParts[0].concat(":").concat(matchParts[1]);
            if (switchWhichReceivedPacket.equals(srcNode)){
                ingressUdpPackets++;
            }
            else if (switchWhichReceivedPacket.equals(dstNode)){
                egressUdpPackets++;
            }
        }

        byte[] srcMacRaw = PacketParsingUtils.extractSrcMac(payload);
        String srcMac = PacketParsingUtils.rawMacToString(srcMacRaw);

        if(srcMac.equals(sourceMac)) {
            Long timeNow = System.currentTimeMillis();
            Long sentTime = PacketSender.sentTimes.get(packetReceived.getMatch().getInPort().getValue());
      //      System.out.println("Packet received from " + packetReceived.getMatch().getInPort().getValue());
      //      System.out.println("Time now is: " + timeNow);
     //       System.out.println("Sent time is : " + sentTime);
            Long latency = timeNow - sentTime;
            LatencyMonitor.latency = latency;
            MonitorLinksTask.packetReceivedFromController = true;
        }
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