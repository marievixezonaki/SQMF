/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

/**
 * The class which monitors the latency.
 *
 * @author Marievi Xezonaki
 */
public class LatencyMonitor {

    private static DataBroker db;
    public volatile static Long latency = -1L;
    private static PacketProcessingService packetProcessingService;
    private static PacketSender packetSender;

    /**
     * TThe constructor method.
     *
     * @param dataBroker                The databroker to assist in accessing the MD-SAL.
     * @param packetProcessingService   The service which will enable to send packets for delay monitoring.
     */
    public LatencyMonitor(DataBroker dataBroker, PacketProcessingService packetProcessingService){
        this.packetProcessingService = packetProcessingService;
        db = dataBroker;
        packetSender = new PacketSender(packetProcessingService);
    }



    /**
     * The method which measures the delay for a particular link.
     *
     * @param link                  The link for which delay will be measured.
     * @param srcMac                The source MAC which will be assigned to the packet to be sent to next node.
     * @param nextNodeConnector     The interface which will receive the packet.
     */
    public Long MeasureNextLink(Link link, String srcMac, String nextNodeConnector) {
        MonitorLinksTask.packetReceivedFromController = false;
        latency = -1L;
        String nodeConnectorId = link.getSource().getSourceTp().getValue();
        String nodeId = link.getSource().getSourceNode().getValue();
        packetSender.sendPacket(nodeConnectorId, nodeId, srcMac, nextNodeConnector);
        while(latency == -1) {
        }
        return latency;
    }

}
