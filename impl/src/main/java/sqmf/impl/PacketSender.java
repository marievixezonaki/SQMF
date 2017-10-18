/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnectorKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.PacketProcessingService;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.packet.service.rev130709.TransmitPacketInputBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;

/**
 * The class containing the methods for packet sending, for delay monitoring.
 *
 * @author Marievi Xezonaki
 */
public class PacketSender {

    private PacketProcessingService packetProcessingService;
    public static HashMap<String, Long> sentTimes = new HashMap();

    /**
     * The constructor method.
     *
     * @param packetProcessingService   The service which will enable to send packets for delay monitoring.
     */
    public PacketSender(PacketProcessingService packetProcessingService){
        this.packetProcessingService = packetProcessingService;
    }

    /**
     *
     *
     * @param outputNodeConnector       The interface where the packet will be sent.
     * @param nodeId                    The node which will send the packet.
     * @param srcMac                    The source MAC address which the packets to be sent will have.
     * @param nextNodeConnector         The interface which will receive the sent packet.
     *
     * @return                          True or false, depending on the method's success or not.
     */
    public boolean sendPacket(String outputNodeConnector, String nodeId, String srcMac, String nextNodeConnector) {

        MacAddress srcMacAddress = new MacAddress(srcMac);
        String nodeConnectorId = outputNodeConnector.split(":")[2];

        NodeRef ref = createNodeRef(nodeId);
        NodeConnectorId ncId = new NodeConnectorId(outputNodeConnector);
        NodeConnectorKey nodeConnectorKey = new NodeConnectorKey(ncId);
        NodeConnectorRef nEgressConfRef = new NodeConnectorRef(createNodeConnRef(nodeId, nodeConnectorKey));

        byte[] lldpFrame = LLDPUtils.buildLldpFrame(new NodeId(nodeId),
                new NodeConnectorId(outputNodeConnector), srcMacAddress, Long.parseLong(nodeConnectorId));

        ActionBuilder actionBuilder = new ActionBuilder();
        ArrayList<Action> actions = new ArrayList<>();

        Action outputNodeConnectorAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(nodeConnectorId))
                                .build())
                        .build())
                .build();
        actions.add(outputNodeConnectorAction);

        TransmitPacketInput packet = new TransmitPacketInputBuilder()
                .setEgress(nEgressConfRef)
                .setNode(ref)
                .setPayload(lldpFrame)
                .setAction(actions)
                .build();
        sentTimes.put(nextNodeConnector, System.currentTimeMillis());

        Future<RpcResult<Void>> future = packetProcessingService.transmitPacket(packet);
        try {
            if (future.get().isSuccessful()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    /**
     * The method which creates a reference for a node connector.
     *
     * @param node_id       The node where the node connector which the reference will be created for belongs.
     * @param nodeConnKey   The node connector for which the reference will be created.
     * @return              The reference.
     */
    private NodeConnectorRef createNodeConnRef(String node_id, NodeConnectorKey nodeConnKey) {
        InstanceIdentifier<NodeConnector> instanceIdent= InstanceIdentifier.builder(Nodes.class)
                .child(Node.class, new NodeKey(NodeId.getDefaultInstance(node_id)))
                .child(NodeConnector.class, new NodeConnectorKey(nodeConnKey)).toInstance();
        return new NodeConnectorRef(instanceIdent);
    }



    /**
     * The method which creates a reference for a node.
     *
     * @param node_id       The node for which the reference will be created.
     *
     * @return              The reference.
     */
    private NodeRef createNodeRef(String node_id) {
        NodeKey key = new NodeKey(new NodeId(node_id));
        InstanceIdentifier<Node> path = InstanceIdentifier.builder(Nodes.class).child(Node.class, key).build();
        return new NodeRef(path);
    }

}
