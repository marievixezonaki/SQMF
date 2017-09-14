/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
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
    private String pathInputPort, pathOutputPort;
    private PacketProcessingService packetProcessingService;
    private RpcProviderRegistry rpcProviderRegistry;
    private List<Long> latencies = new ArrayList<>();
    private Integer ingressPackets = 0, egressPackets = 0;
    String sourceMac;
    volatile static boolean packetReceivedFromController = false;

    private static HashMap<String, String> nextNodeConnectors = new HashMap();

    public MonitorLinksTask(DataBroker db, String pathInputPort, String pathOutputPort, RpcProviderRegistry rpcProviderRegistry, String srcMac){
        this.db = db;
        this.pathInputPort = pathInputPort;
        this.pathOutputPort = pathOutputPort;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.sourceMac = srcMac;
    }

    @Override
    public void run() {

        //monitor packet loss and delay
        QoSOperations qoSOperations = new QoSOperations(db);
   //     qoSOperations.getAllLinksWithQos();

        //monitor packet loss
   //     PacketLossMonitor packetLossMonitor = new PacketLossMonitor();
  //      double totalPacketLoss = packetLossMonitor.monitorPacketLoss();
   /*     Integer currentIngressPackets = PacketProcessing.ingressUdpPackets - ingressPackets;
        Integer currentEgressPackets = PacketProcessing.egressUdpPackets - egressPackets;
        Integer lostUdpPackets = currentIngressPackets - currentEgressPackets;

        ingressPackets = PacketProcessing.ingressUdpPackets;
        egressPackets = PacketProcessing.egressUdpPackets;

        double packetLoss;
        if (lostUdpPackets > 0){
            System.out.println("Lost " + lostUdpPackets + " of total sent " + currentIngressPackets);
            packetLoss = (double)lostUdpPackets/currentIngressPackets;
        }
        else{
            packetLoss = 0;
        }
     //   System.out.println("Ingress node has sent " + PacketProcessing.ingressUdpPackets + " " + currentIngressPackets);
     //   System.out.println("Egress node has received " + PacketProcessing.egressUdpPackets + " " + currentEgressPackets);
        System.out.println("Packet loss is " + packetLoss);*/

        double packetLoss = monitorPacketLoss();
        //monitor delay
 /*       if (rpcProviderRegistry != null) {
            packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);

            LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
            List<DomainLink> linkList = ExampleImpl.mainGraphWalk.getEdgeList();
            //find next node connector where each packet should arrive at
            findNextNodeConnector(linkList);
            for (DomainLink link : linkList) {
                if (!link.getLink().getLinkId().getValue().contains("host")) {
                    Long latency = latencyMonitor.MeasureNextLink(link.getLink(), sourceMac, nextNodeConnectors.get(link.getLink().getSource().getSourceNode().getValue()));
                    System.out.println("Latency for " + link.getLink().getSource().getSourceNode().getValue() + " --> " + link.getLink().getDestination().getDestNode().getValue()  + " is " + latency);
                    while (packetReceivedFromController == false){

                    }
                    latencies.add(latency);
                }
                else{
                    System.out.println("Latency not computed for " + link.getLink().getLinkId().getValue());
                }
            }
        }

        //TODO : check if there are latencies for all links
        Long totalDelay = 0L;

        //compute path's total delay
        if (latencies.size() > 0){
            totalDelay = qoSOperations.computeTotalDelay(latencies);
        }
        System.out.println("Total delay is " + totalDelay);
        latencies.clear();*/
        Long delay = monitorDelay();
        System.out.println("Total delay is " + delay);
        System.out.println("Total loss is " + packetLoss);

        //compute path's QoE
        double pathMOS = qoSOperations.QoEEstimation(delay, packetLoss);
        System.out.println("MOS is " + pathMOS);

        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

    private void findNextNodeConnector(List<DomainLink> linkList){

        int i = 0;
        for (DomainLink domainLink : linkList){
            if (i <= (linkList.size()-1)){
                nextNodeConnectors.put(domainLink.getLink().getSource().getSourceNode().getValue() ,domainLink.getLink().getDestination().getDestTp().getValue());
            }
        }
    }

    private Long monitorDelay(){
        QoSOperations qoSOperations = new QoSOperations(db);

        if (rpcProviderRegistry != null) {
            packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);

            LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
            List<DomainLink> linkList = ExampleImpl.mainGraphWalk.getEdgeList();
            //find next node connector where each packet should arrive at
            findNextNodeConnector(linkList);
            for (DomainLink link : linkList) {
                if (!link.getLink().getLinkId().getValue().contains("host")) {
                    Long latency = latencyMonitor.MeasureNextLink(link.getLink(), sourceMac, nextNodeConnectors.get(link.getLink().getSource().getSourceNode().getValue()));
                    System.out.println("Latency for " + link.getLink().getSource().getSourceNode().getValue() + " --> " + link.getLink().getDestination().getDestNode().getValue()  + " is " + latency);
                    while (packetReceivedFromController == false){

                    }
                    latencies.add(latency);
                }
                else{
                    System.out.println("Latency not computed for " + link.getLink().getLinkId().getValue());
                }
            }
        }

        //TODO : check if there are latencies for all links
        Long totalDelay = 0L;

        //compute path's total delay
        if (latencies.size() > 0){
            totalDelay = qoSOperations.computeTotalDelay(latencies);
        }
        latencies.clear();
        return totalDelay;
    }

    private double monitorPacketLoss(){
        Integer currentIngressPackets = PacketProcessing.ingressUdpPackets - ingressPackets;
        Integer currentEgressPackets = PacketProcessing.egressUdpPackets - egressPackets;
        Integer lostUdpPackets = currentIngressPackets - currentEgressPackets;

        ingressPackets = PacketProcessing.ingressUdpPackets;
        egressPackets = PacketProcessing.egressUdpPackets;

        double packetLoss;
        if (lostUdpPackets > 0){
     //       System.out.println("Lost " + lostUdpPackets + " of total sent " + currentIngressPackets);
            packetLoss = (double)lostUdpPackets/currentIngressPackets;
        }
        else{
            packetLoss = 0;
        }
        return packetLoss;
    }
}

