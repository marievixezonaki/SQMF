/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.jgrapht.GraphPath;
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
 */
public class MonitorLinksTask extends TimerTask{

    private DataBroker db;
    private PacketProcessingService packetProcessingService;
    private RpcProviderRegistry rpcProviderRegistry;
    private List<Long> latencies = new ArrayList<>();
    private Integer ingressPackets = 0, egressPackets = 0;
    String sourceMac;
    volatile static boolean packetReceivedFromController = false;
    private static HashMap<String, String> nextNodeConnectors = new HashMap();
    public static boolean isFailover = false;
    private boolean linkFailure = false;

    public MonitorLinksTask(DataBroker db, RpcProviderRegistry rpcProviderRegistry, String srcMac){
        this.db = db;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.sourceMac = srcMac;
    }

    @Override
    public void run() {

        //monitor packet loss and delay
        QoSOperations qoSOperations = new QoSOperations(db);
        Long delay = monitorDelay(ExampleImpl.mainGraphWalk);
        double packetLoss = monitorPacketLoss();

        System.out.println("Total delay is " + delay + " ms");
        System.out.println("Total loss is " + packetLoss + "%");

        //compute path's QoE
        double pathMOS = qoSOperations.QoEEstimation(delay, packetLoss);
        System.out.println("MOS is " + pathMOS);
        if (pathMOS < ExampleImpl.QoEThreshold) {
            System.out.println("MOS is lower than the threshold.");
            //cancel previous timer task and monitor the new main path
            if (!isFailover) {
                if (!ExampleImpl.fastFailover) {
                    ExampleImpl.changePath();
                }
            }
            else{
                System.out.println("Cannot change path although QoE low.");
            }
        }

        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

    private Long monitorDelay(GraphPath<Integer, DomainLink> path){
        QoSOperations qoSOperations = new QoSOperations(db);

        if (rpcProviderRegistry != null) {
            packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);

            LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
            List<DomainLink> linkList = path.getEdgeList();

            //find next node connector where each packet should arrive at
            findNextNodeConnector(linkList);
            for (DomainLink link : linkList) {

                if (!NetworkGraph.getInstance().getGraphLinks().contains(link.getLink())){
                    System.out.println("A link in the path is down.");
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
            totalDelay = qoSOperations.computeTotalDelay(latencies);
        }
        latencies.clear();
        return totalDelay;
    }

  /*  private Long monitorDelay(GraphPath<Integer, DomainLink> pathh){
        QoSOperations qoSOperations = new QoSOperations(db);

        if (rpcProviderRegistry != null) {
            packetProcessingService = rpcProviderRegistry.getRpcService(PacketProcessingService.class);

            LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
            List<DomainLink> linkList = ExampleImpl.mainGraphWalk.getEdgeList();
            //find next node connector where each packet should arrive at
            findNextNodeConnector(linkList);
            for (DomainLink link : ExampleImpl.mainGraphWalk.getEdgeList()) {
                if (!NetworkGraph.getInstance().getGraphLinks().contains(link.getLink())){
                    System.out.println("Link failure!");
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
            totalDelay = qoSOperations.computeTotalDelay(latencies);
        }
        latencies.clear();
        return totalDelay;
    }*/

    private void findNextNodeConnector(List<DomainLink> linkList){

        int i = 0;
        for (DomainLink domainLink : linkList){
            if (i <= (linkList.size()-1)){
                nextNodeConnectors.put(domainLink.getLink().getSource().getSourceNode().getValue() ,domainLink.getLink().getDestination().getDestTp().getValue());
            }
        }
    }

    private double monitorPacketLoss(){
        Integer currentIngressPackets = PacketProcessing.ingressUdpPackets - ingressPackets;
        Integer currentEgressPackets = PacketProcessing.egressUdpPackets - egressPackets;
        Integer lostUdpPackets = currentIngressPackets - currentEgressPackets;

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
}

