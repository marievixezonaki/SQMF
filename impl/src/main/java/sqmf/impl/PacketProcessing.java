/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.controller.liblldp.BitBufferHelper;
import org.opendaylight.controller.liblldp.BufferException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingListener;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The class which listens for incoming packets and processes them.
 *
 * @author Marievi Xezonaki
 */
public class PacketProcessing implements PacketProcessingListener {

    private final Logger LOG = LoggerFactory.getLogger(PacketProcessing.class);
    public static Integer ingressUdpPackets = 0;
    public static Integer egressUdpPackets = 0;
    public static Integer totalInPackets = 0;
    public static Integer totalOutPackets = 0;
    public static Integer ingressBits = 0;
    public static Integer egressBits = 0;
    public String srcNode;
    public String dstNode;
    private String sourceMac;
    public static final Integer ETHERTYPE_8021Q = 0x8100;
    public static final Integer ETHERTYPE_QINQ = 0x9100;
    private int udpPacketSize;
    public static boolean videoHasStarted = false;
    public static Long videoStartTime = 0L;
    public static volatile int buffer = 0;

    /**
     * The constructor method.
     *
     * @param srcNode       The ingress switch of the path.
     * @param dstNode       The egress switch of the path.
     * @param srcMac        The source MAC address which the packets to be sent will have.
     *
     * @return              The reference.
     */
    public PacketProcessing(String srcNode, String dstNode, String srcMac) {
        LOG.info("PacketProcessing loaded successfully");
        this.srcNode = srcNode;
        this.dstNode = dstNode;
        this.sourceMac = srcMac;
    }



    /**
     * The method which filters the received packets by the controller, keeps the UDP packets and computes
     * this way the delay and the packet loss of the path.
     *
     * @param packetReceived    A packet received by the controller.
     */
    @Override
    public void onPacketReceived(PacketReceived packetReceived) {

        byte[] payload = packetReceived.getPayload();
        byte protocol = PacketParsingUtils.extractIPprotocol(payload);
        if (protocol == 0x11) {
            if (!videoHasStarted && videoStartTime == 0L){
                videoStartTime = System.currentTimeMillis();
                videoHasStarted = true;
            }
            Match match = packetReceived.getMatch();
            String[] matchParts = match.getInPort().getValue().split(":");
            String switchWhichReceivedPacket = matchParts[0].concat(":").concat(matchParts[1]);
            if (switchWhichReceivedPacket.equals(srcNode) /*&& (Integer.parseInt(matchParts[2]) == 2)*/){
                try {
                    udpPacketSize = decode(packetReceived);
                } catch (BufferException e) {
                    e.printStackTrace();
                }
                ingressUdpPackets++;
                totalInPackets++;
                ingressBits += udpPacketSize;
            }
            else if (switchWhichReceivedPacket.equals(dstNode) /*&& ((Integer.parseInt(matchParts[2]) == 3)) *//*|| (Integer.parseInt(matchParts[2]) == 1)*/){
                try {
                    udpPacketSize = decode(packetReceived);
                } catch (BufferException e) {
                    e.printStackTrace();
                }
                egressUdpPackets++;
                totalOutPackets++;
                egressBits += udpPacketSize;
            }
            else if (switchWhichReceivedPacket.equals(SqmfImplementation.mainGraphWalk.getEdgeList().get(SqmfImplementation.mainGraphWalk.getEdgeList().size() - 2).getLink().getDestination().getDestNode().getValue())){
                //this is the egress switch
                System.out.println("This is the previous from the egress switch, " + switchWhichReceivedPacket);
                buffer++;

            }
            else if (switchWhichReceivedPacket.equals(SqmfImplementation.mainGraphWalk.getEdgeList().get(SqmfImplementation.mainGraphWalk.getEdgeList().size() - 1).getLink().getDestination().getDestNode().getValue())){
                //this is the egress switch
                System.out.println("This is the egress switch, " + switchWhichReceivedPacket);
                buffer--;

            }
        }

        byte[] srcMacRaw = PacketParsingUtils.extractSrcMac(payload);
        String srcMac = PacketParsingUtils.rawMacToString(srcMacRaw);

        if(srcMac.equals(sourceMac)) {
            Long timeNow = System.currentTimeMillis();
            Long sentTime = PacketSender.sentTimes.get(packetReceived.getMatch().getInPort().getValue());
            Long latency = timeNow - sentTime;
            LatencyMonitor.latency = latency;
            MonitorLinksTask.packetReceivedFromController = true;
        }
    }



    /**
     * The method which decodes a received packet in order to extract its size.
     *
     * @param packetReceived    A packet received by the controller.
     * @return                  The packet size.
     */
    public int decode(PacketReceived packetReceived) throws BufferException {
        byte[] data = packetReceived.getPayload();
        try {
            Integer nextField = BitBufferHelper.getInt(BitBufferHelper.getBits(data, 96, 16));
            int extraHeaderBits = 0;
            while (nextField.equals(ETHERTYPE_8021Q) || nextField.equals(ETHERTYPE_QINQ)) {
                nextField = BitBufferHelper.getInt(BitBufferHelper.getBits(data, 128 + extraHeaderBits, 16));
                extraHeaderBits += 32;
            }
         //   int headerSize = (112 + extraHeaderBits);
            int packetSize = packetReceived.getPayload().length*8;
          //  int packetSize = headerSize + payloadSize;
            return packetSize;
        } catch (BufferException e) {
            e.printStackTrace();
        }
        return -1;
    }
}