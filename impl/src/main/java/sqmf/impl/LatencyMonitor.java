/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package sqmf.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

public class LatencyMonitor {

    private static DataBroker db;

    public volatile static Long latency = -1L;

    private static PacketProcessingService packetProcessingService;
    private static PacketSender packetSender;

    public LatencyMonitor(DataBroker dataBroker, PacketProcessingService packetProcessingService){
        this.packetProcessingService = packetProcessingService;
        db = dataBroker;
        packetSender = new PacketSender(packetProcessingService);
    }

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
