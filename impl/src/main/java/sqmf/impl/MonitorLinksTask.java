/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.jgrapht.GraphPath;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.impl.rev141210.Sqmf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TimerTask;

/**
 * The class implementing a task which monitors the topology's links.
 *
 * @author Marievi Xezonaki
 */
public class MonitorLinksTask extends TimerTask{

    private DataBroker db;
    private PacketProcessingService packetProcessingService;
    private RpcProviderRegistry rpcProviderRegistry;
    private List<Long> latencies = new ArrayList<>();
    private Integer ingressPackets = 0, egressPackets = 0, ingressBits = 0, egressBits = 0;
    String sourceMac;
    volatile static boolean packetReceivedFromController = false;
    private static HashMap<String, String> nextNodeConnectors = new HashMap();
    public static boolean isFailover = false;
    public static boolean linkFailure = false;
    private String videoAbsolutePath;
    private float videoFPS;
    private static Long lastQoEEstimationTime = 0L;
    private int videoCase;
    public static boolean senderLinkDown = false;
    public static boolean receiverLinkDown = false;
    public static int numberOfStallings = 0;

    /**
     * The constructor method.
     *
     * @param db                        The data broker which gives access to the MD-SAL.
     * @param rpcProviderRegistry       Will enable to register the PacketProcessingService, so that it can operate.
     * @param srcMac                    The source MAC address which the packets sent for delay monitoring will have.
     * @param videoAbsolutePath         The video's absolute path in the file system.
     * @param videoFPS                  The video's frame rate.
     * @param videoCase                 The case (1-5) where the video belongs to.
     *                                  * Case 1 : keyFrame = 1, codec = mpeg4, format = 320x240 (QQVGA)
                                        * Case 2 : keyFrame = 1, codec = mpeg4, format = 160x120 (QVGA)
                                        * Case 3 : keyFrame = 1, codec = mpeg2, format = 640x480 (VGA)
                                        * Case 4 : keyFrame = 1, codec = mpeg4, format = 640x480 (VGA)
                                        * Case 5 : keyFrame = 1, codec = h264, format = 640x480 (VGA)
     */
    public MonitorLinksTask(DataBroker db, RpcProviderRegistry rpcProviderRegistry, String srcMac, String videoAbsolutePath, float videoFPS, int videoCase){
        this.db = db;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.sourceMac = srcMac;
        this.videoAbsolutePath = videoAbsolutePath;
        this.videoFPS = videoFPS;
        this.videoCase = videoCase;
    }


    /**
     * The core method of the task, executing the link monitoring.
     */
    @Override
    public void run() {

        double pathQoE = -1;

        if (senderLinkDown){
            System.out.println("No QoE computation : sender cannot send any traffic.");
            return;
        }
        if (receiverLinkDown){
            System.out.println("No QoE computation : receiver cannot receive any traffic.");
            return;
        }
        if (linkFailure){
            System.out.println("No QoE computation : Changing path as a link failure has occurred.");
            SqmfImplementation.changePath();
            linkFailure = false;
            System.out.println("-----------------------------------------------------------------------------------------------------");
            return;
        }

        // if application streamed is VoIP
        if (SqmfImplementation.applicationType.equals(VoIP.getName())){
            Long delay = monitorDelay(SqmfImplementation.mainGraphWalk);
            double packetLoss;
            if (receiverLinkDown){
                packetLoss = 1;
            }
            else {
                packetLoss = monitorPacketLoss();
            }
            System.out.println("Total delay is " + delay + " ms");
            System.out.println("Total loss is " + packetLoss + "%");
            pathQoE = VoIP.estimateQoE(delay, packetLoss);
        }
        // if application streamed is Video
        else if (SqmfImplementation.applicationType.equals(UDPVideo.getName())){
            UDPVideo udpVideo = new UDPVideo();
            double packetLoss;
            if (receiverLinkDown){
                packetLoss = 1;
            }
            else {
                packetLoss = monitorPacketLoss();
            }
            int bitsReceivedCount = findBits();
            //float frameRate = computeVideoFPS(videoAbsolutePath);
            float frameRate = videoFPS;
            float N = computeN(frameRate);
            float BR = udpVideo.computeVideoBitRate(videoAbsolutePath);

            float bitRate;
         /*   if (frameRate != -1 && N != -1) {
                bitRate = frameRate * bitsReceivedCount / N;
            }
            else {
                bitRate = -1L;
            }*/
         /*   if (bitRate != -1){
                pathQ = Video.estimateQoE(frameRate, BR, packetLoss);
            }*/
            if (bitsReceivedCount == 0){
                BR = 0;
            }
            if (frameRate != -1){
                pathQoE = UDPVideo.estimateUDPVideoQoE(frameRate, BR, packetLoss, videoCase);
            }

            System.out.println("FPS is " + frameRate);
            System.out.println("BR is " + BR);
            System.out.println("PLR is " + packetLoss);

        }
        // if application streamed is TCP Video
        else if (SqmfImplementation.applicationType.equals(WebBasedVideo.getName())){
        //    numberOfStallings = 0;
            numberOfStallings = WebBasedVideo.computeNumberOfStallings();
            System.out.println("Number of stallings : " + numberOfStallings);
            int durationOfStallings = WebBasedVideo.computeDurationOfStallings();
       //     pathQoE = WebBasedVideo.estimateQoE(numberOfStallings, durationOfStallings);
        }

        System.out.println("QoE is " + pathQoE);
        if ( /*linkFailure ||*/ ((pathQoE >= 0) && (pathQoE < SqmfImplementation.QoEThreshold)) ) {
            System.out.println("MOS is lower than the threshold.");
            if (!isFailover && PacketProcessing.videoHasStarted) {
                if (!SqmfImplementation.fastFailover) {
                    /* CRUCIAL LINE : comment in order to just monitor without making corrective
                    *  actions, uncomment in order to implement QoE-based forwarding
                    */
                    SqmfImplementation.changePath();
                }
            }
            else{
                System.out.println("Cannot change path although QoE low.");
            }
        }
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }



    /**
     * The method which monitors the delay for a path.
     *
     * @param path      The path whose links will be monitored.
     * @return          The computed delay.
     */
    private Long monitorDelay(GraphPath<Integer, DomainLink> path){

        if (rpcProviderRegistry != null) {
            packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);

            LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
            List<DomainLink> linkList = path.getEdgeList();

            //find next node connector where each packet should arrive at
            findNextNodeConnector(linkList);
            for (DomainLink link : linkList) {
                if (!NetworkGraph.getInstance().getGraphLinks().contains(link.getLink())){
                 //   System.out.println("A link in the path is down.");
                    linkFailure = true;
                }
                if (!link.getLink().getLinkId().getValue().contains("host") && NetworkGraph.getInstance().getGraphLinks().contains(link.getLink())) {
                    Long latency = latencyMonitor.MeasureNextLink(link.getLink(), sourceMac, nextNodeConnectors.get(link.getLink().getSource().getSourceNode().getValue()));
                    while (packetReceivedFromController == false){

                    }
                    latencies.add(latency);
                }
            }
        }
        Long totalDelay = 0L;
        //compute path's total delay
        if (latencies.size() > 0){
            totalDelay = computeTotalDelay(latencies);
        }
        latencies.clear();
        return totalDelay;
    }



    /**
     * The method which finds the interfaces which will receive the packet sent from each interface.
     *
     * @param linkList          The path's links.
     */
    private void findNextNodeConnector(List<DomainLink> linkList){
        int i = 0;
        for (DomainLink domainLink : linkList){
            if (i <= (linkList.size()-1)){
                nextNodeConnectors.put(domainLink.getLink().getSource().getSourceNode().getValue() ,domainLink.getLink().getDestination().getDestTp().getValue());
            }
        }
    }



    /**
     * The method which monitors the packet loss for a path.
     *
     * @return      The computed packet loss.
     */
    private double monitorPacketLoss(){
        Integer currentIngressPackets = PacketProcessing.ingressUdpPackets - ingressPackets;
        Integer currentEgressPackets = PacketProcessing.egressUdpPackets - egressPackets;
        Integer lostUdpPackets = currentIngressPackets - currentEgressPackets;
        System.out.println("Packets " + PacketProcessing.totalInPackets + " " + PacketProcessing.totalOutPackets);

        ingressPackets = PacketProcessing.ingressUdpPackets;
        egressPackets = PacketProcessing.egressUdpPackets;

        double packetLoss;
        if (lostUdpPackets > 0){
            packetLoss = (double)lostUdpPackets/currentIngressPackets;
        }
        else{
            packetLoss = 0;
        }
        return packetLoss;
    }



    /**
     * The method which computes the bits received by the destination.
     *
     * @return      The bits received by the destination.
     */
    private int findBits(){
        Integer currentIngressBits = PacketProcessing.ingressBits - ingressBits;
        Integer currentEgressBits = PacketProcessing.egressBits - egressBits;
 //       System.out.println("Bits " + currentIngressBits + " " + currentEgressBits);
        ingressBits = PacketProcessing.ingressBits;
        egressBits = PacketProcessing.egressBits;
        return currentEgressBits;
    }



    /**
     * The method which computes N factor for video QoE estimation.
     *
     * @param frameRate     The video's frame rate.
     * @return              The bits received by the destination.
     */
    public float computeN(float frameRate){
        float N = -1;
        Long timeNow = System.currentTimeMillis();
        if (lastQoEEstimationTime == 0L){
            if (PacketProcessing.videoStartTime != 0L && PacketProcessing.videoHasStarted) {
                float timeElapsed = (timeNow - PacketProcessing.videoStartTime)/(float)1000;
                N = frameRate*timeElapsed;

            }
        }
        else {
            float timeElapsed = (timeNow - lastQoEEstimationTime)/(float)1000;
            N  = frameRate*timeElapsed;
        }
        lastQoEEstimationTime = timeNow;

        return N;
    }



    /**
     * The method which computes the total delay of a path.
     *
     * @param delays    A list containing the delay of each link in the path.
     * @return          It returns the path's total delay.
     */
    public Long computeTotalDelay(List<Long> delays){
        Long totalDelay = 0L;
        for (Long delay : delays){
            totalDelay += delay;
        }
        return totalDelay;
    }

}


