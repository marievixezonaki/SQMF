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
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.Counter32;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.FlowCapableNodeConnectorStatisticsData;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxez on 28/8/2017.
 */
public class QoSOperations {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);
    private DataBroker db;

    public QoSOperations(DataBroker dataBroker){
        this.db = dataBroker;
    }

    public List<LinkWithQoS> getAllLinksWithQos() {

        try {

            List<LinkWithQoS> linksToReturn = new ArrayList<>();
            List<Link> links = getAllLinks(db);
            List<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> nodeList = getNodes(db);
            if (links != null) {
                for (Link link : links) {
                    String nodeToFind = link.getSource().getSourceNode().getValue();
                    String outputNodeConnector = link.getSource().getSourceTp().getValue();

                    for (org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node node : nodeList) {

                        if (node.getId().getValue().equals(nodeToFind)) {

                            List<NodeConnector> nodeConnectors = node.getNodeConnector();

                            for (NodeConnector nc : nodeConnectors) {

                                if (nc.getId().getValue().equals(outputNodeConnector)) {

                                    FlowCapableNodeConnectorStatisticsData statData = nc.getAugmentation(FlowCapableNodeConnectorStatisticsData.class);
                                    org.opendaylight.yang.gen.v1.urn.opendaylight.port.statistics.rev131214.flow.capable.node.connector.statistics.FlowCapableNodeConnectorStatistics statistics = statData.getFlowCapableNodeConnectorStatistics();
                                    BigInteger packetsTransmitted = statistics.getPackets().getTransmitted();
                                    BigInteger packetErrorsTransmitted = statistics.getTransmitErrors();
                                    Float packetLoss = (packetsTransmitted.floatValue() == 0) ? 0 : packetErrorsTransmitted.floatValue() / packetsTransmitted.floatValue();
                                    Counter32 duration = statistics.getDuration().getSecond();

                               //     System.out.println(packetErrorsTransmitted.floatValue() + " " + packetsTransmitted.floatValue() + " dur " + duration.getValue().toString());

                                    FlowCapableNodeConnector fcnc = nc.getAugmentation(FlowCapableNodeConnector.class);
                                    linksToReturn.add(new LinkWithQoS(fcnc.getCurrentSpeed(), packetLoss.longValue(), duration.getValue(), link));

                                    statistics.getPackets().getReceived();
                                    System.out.println(outputNodeConnector + " packets transmitted " + packetsTransmitted.floatValue() + " packets received " + statistics.getPackets().getReceived());
                                }
                            }
                        }
                    }
                }
                return linksToReturn;
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<Link> getAllLinks(DataBroker db) {
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

            LOG.info("Node Fetching Failed");

            return linkList;
        }

    }

    public static List<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> getNodes(DataBroker db) throws ReadFailedException {

        List<org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node> nodeList = new ArrayList<>();
        InstanceIdentifier<Nodes> nodesIid = InstanceIdentifier.builder(
                Nodes.class).build();
        ReadOnlyTransaction nodesTransaction = db.newReadOnlyTransaction();
        CheckedFuture<Optional<Nodes>, ReadFailedException> nodesFuture = nodesTransaction
                .read(LogicalDatastoreType.OPERATIONAL, nodesIid);
        Optional<Nodes> nodesOptional = nodesFuture.checkedGet();
        if (nodesOptional != null && nodesOptional.isPresent()) {
            nodeList = nodesOptional.get().getNode();
        }

        return nodeList;
    }

}
