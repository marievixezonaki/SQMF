/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.common.RpcResult;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Future;

/**
 * The class implementing a task which monitors the topology's links.
 *
 * @author Marievi Xezonaki
 */
public class MonitorLinksTask extends TimerTask{

    private DataBroker db;
    private String pathInputPort, pathOutputPort;
    private PacketProcessingService packetProcessingService;

    public MonitorLinksTask(DataBroker db, String pathInputPort, String pathOutputPort){
        this.db = db;
        this.pathInputPort = pathInputPort;
        this.pathOutputPort = pathOutputPort;
    }

    @Override
    public void run() {

        //monitor packet loss and delay
        QoSOperations qoSOperations = new QoSOperations(db, "openflow:1:2", "openflow:8:2");
        qoSOperations.getAllLinksWithQos();

     /*   LatencyMonitor latencyMonitor = new LatencyMonitor(db, this.packetProcessingService);
        List<Link> linkList = latencyMonitor.getAllLinks();
        for (Link link : linkList) {
            Long latency = latencyMonitor.MeasureNextLink(link);
            System.out.println("Latency for " + link.getSource().getSourceNode().getValue() + "-->" + link.getDestination().getDestNode().getValue() + " is " + latency);
        }*/

        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

}

