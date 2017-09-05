/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import com.google.common.base.Optional;
import com.google.common.util.concurrent.CheckedFuture;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class LatencyMonitor {

    private static DataBroker db;

    public static Long latency = -1L;

    private static HashMap<Link, Long> latencies = new HashMap<>();
    private static  HashMap<Link, Long> jitters = new HashMap<>();
    private static ArrayList<Link> links = new ArrayList<>();
    private static PacketProcessingService packetProcessingService;
    private static PacketSender packetSender;

    public LatencyMonitor(DataBroker dataBroker, PacketProcessingService packetProcessingService){
        this.packetProcessingService = packetProcessingService;
        db = dataBroker;
        packetSender = new PacketSender(packetProcessingService);
    }

    public Long MeasureNextLink(Link link) {

        latency = -1L;
        String node_id = link.getSource().getSourceNode().getValue();
        String node_connector_id = link.getSource().getSourceTp().getValue();
        packetSender.sendPacket(0, node_connector_id, node_id );
        while(latency == -1) {
        }
        return latency;
    }

    public List<Link> getAllLinks() {
        List<Link> linkList = new ArrayList<>();

        try {
            TopologyId topoId = new TopologyId("flow:1");
            InstanceIdentifier<Topology> nodesIid = InstanceIdentifier.builder(NetworkTopology.class).child(Topology.class, new TopologyKey(topoId)).toInstance();
            ReadOnlyTransaction nodesTransaction = db.newReadOnlyTransaction();
            CheckedFuture<Optional<Topology>, ReadFailedException> nodesFuture = nodesTransaction
                    .read(LogicalDatastoreType.OPERATIONAL, nodesIid);
            Optional<Topology> nodesOptional = nodesFuture.checkedGet();

            if (nodesOptional != null && nodesOptional.isPresent())
                linkList = nodesOptional.get().getLink();

            return linkList;
        } catch (Exception e) {
            e.printStackTrace();
            return linkList;
        }

    }
}
