/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import java.util.HashMap;
import java.util.List;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.PortNumber;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.output.action._case.OutputActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.Action;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.list.ActionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowCapableNode;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.FlowId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.Table;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.TableKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.Flow;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.inventory.rev130819.tables.table.FlowKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowCookie;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.FlowModFlags;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.OutputPortValues;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Instructions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.InstructionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.Match;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.flow.MatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.ApplyActionsCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActions;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.instruction.apply.actions._case.ApplyActionsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.node.NodeConnector;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.ProtocolMatchFieldsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.layer._4.match.UdpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;

public class SwitchConfigurator {

    private DataBroker db;
    private AtomicLong flowCookieInc = new AtomicLong(0x2a00000000000000L);
    private static final Logger LOG = LoggerFactory.getLogger(SwitchConfigurator.class);
    private Integer flowId = 1;
    private static Long groupId = 1L;
    private static Long bucketId = 1L;
    private static HashMap<String, Integer> groupsHashMap = new HashMap();
    private static final Integer IP_ETHERTYPE = 0x0800;
    private Integer UDP_PROTOCOL = 17;
    private static List<String> connectorsWithFlowsToRemove = new ArrayList<>();

    public SwitchConfigurator(DataBroker db) {
        this.db = db;
    }

    public void configureIngress(DomainNode sourceNode, Integer inPort, Integer outputPort, Integer failoverPort){
        System.out.println("Output from " + sourceNode.getODLNodeID() + " will be at port " + outputPort + " and failover at " + failoverPort + " ,input is at " + inPort);

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        String standardOutputPort = sourceNode.getODLNodeID().concat(":").concat(outputPort.toString());
        String backupPort = sourceNode.getODLNodeID().concat(":").concat(failoverPort.toString());

        Group groupForIngress = createGroupForIngressSwitch(standardOutputPort, backupPort);
        InstanceIdentifier<Group> instanceIdentifierGroupIngress = createInstanceIdentifierForGroup(sourceNode.getODLNodeID(), groupForIngress);

        Flow flowForIngress = createFlow(standardOutputPort, inPort);
        InstanceIdentifier<Flow> instanceIdentifierIngress = createInstanceIdentifierForFlow(sourceNode.getODLNodeID(), flowForIngress);

        Flow flowForIngressFailover = createFlow(backupPort, outputPort);
        InstanceIdentifier<Flow> instanceIdentifierIngressFailover = createInstanceIdentifierForFlow(sourceNode.getODLNodeID(), flowForIngressFailover);

        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierGroupIngress, groupForIngress, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngress, flowForIngress, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngressFailover, flowForIngressFailover, true);

        transaction.submit();
    }

    public void configureCoreAndEgress(List<DomainLink> mainPathLinks, HashMap<String, Integer> inputPorts, HashMap<String, Integer> outputPorts, HashMap<String, Integer> failoverPorts){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        for (DomainLink link : mainPathLinks){
            String switchToConfigure = link.getLink().getDestination().getDestNode().getValue();
            String standardOutputPort = switchToConfigure.concat(":").concat(outputPorts.get(switchToConfigure).toString());
            String backupPort = switchToConfigure.concat(":").concat(failoverPorts.get(switchToConfigure).toString());
            Group group = createGroupForCoreAndEgressSwitch(standardOutputPort, backupPort);
            InstanceIdentifier<Group> instanceIdentifierGroup = createInstanceIdentifierForGroup(switchToConfigure, group);

            Flow flow = createFlow(standardOutputPort, inputPorts.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);

            Flow flowFailover = createFlow(backupPort, outputPorts.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifierFailover = createInstanceIdentifierForFlow(switchToConfigure, flowFailover);

            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierGroup, group, true);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierFailover, flowFailover, true);

        }

        transaction.submit();

    }

    public void configureFailoverPath(List<DomainLink> failoverPathLinks, HashMap<String, Integer> inputPortsFailover, HashMap<String, Integer> outputPortsFailover){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        for (DomainLink link : failoverPathLinks){
            String switchToConfigure = link.getLink().getDestination().getDestNode().getValue();
            String standardOutputPort = switchToConfigure.concat(":").concat(outputPortsFailover.get(switchToConfigure).toString());

            Flow flow = createFlowFailover(standardOutputPort, inputPortsFailover.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);

            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);

        }

        transaction.submit();

    }

    public void configureFailoverIngress(DomainNode sourceNode, Integer inPort, Integer outputPort){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        String switchToConfigure = sourceNode.getODLNodeID();
        String standardOutputPort = switchToConfigure.concat(":").concat(outputPort.toString());

        Flow flow = createFlow(standardOutputPort, inPort);
        InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);

        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);
        transaction.submit();

    }

    private Flow createFlow(String standardOutputPort, Integer inPort){

        connectorsWithFlowsToRemove.add(standardOutputPort);

        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        NodeConnectorId inputPort = new NodeConnectorId(inPort.toString());

        Match match = new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                    .setEthernetType(new EthernetTypeBuilder()
                        .setType(new EtherType(IP_ETHERTYPE.longValue()))
                        .build())
                    .build())
                    .setIpMatch(new IpMatchBuilder()
                        .setIpProtocol(UDP_PROTOCOL.shortValue())
                    .build())
                .setInPort(inputPort)
                .build();

        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<Action>();

        Integer group = groupsHashMap.get(standardOutputPort);
        if (group != null){
            Action groupAction = actionBuilder
                    .setOrder(0).setAction(new GroupActionCaseBuilder()
                            .setGroupAction(new GroupActionBuilder()
                                    .setGroupId(group.longValue())
                                    .build())
                            .build())
                    .build();
            actions.add(groupAction);
        }
        else{
            Action outputNodeConnectorAction = actionBuilder
                    .setOrder(0).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(standardOutputPort.split(":")[2]))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction);
        }

        //ApplyActions
        ApplyActions applyActions = new ApplyActionsBuilder().setAction(actions).build();
        List<Instruction> instructions = new ArrayList<>();

        //Instruction for Actions
        Instruction applyActionsInstruction = new InstructionBuilder()
                .setOrder(0).setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();

        instructions.add(applyActionsInstruction);

        //Build all the instructions together, based on the Instructions list
        Instructions allInstructions = new InstructionsBuilder()
                .setInstruction(instructions)
                .build();

        flowBuilder
                .setMatch(match)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setInstructions(allInstructions)
                .setPriority(1000)
                .setHardTimeout(30000)
                .setIdleTimeout(30000)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }

    private Flow createFlowFailover(String standardOutputPort, Integer inPort){

        connectorsWithFlowsToRemove.add(standardOutputPort);

        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        NodeConnectorId inputPort = new NodeConnectorId(inPort.toString());

        Match match = new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetType(new EthernetTypeBuilder()
                                .setType(new EtherType(IP_ETHERTYPE.longValue()))
                                .build())
                        .build())
                .setIpMatch(new IpMatchBuilder()
                        .setIpProtocol(UDP_PROTOCOL.shortValue())
                        .build())
                .setInPort(inputPort)
                .build();

        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<Action>();

        Action outputNodeConnectorAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(standardOutputPort.split(":")[2]))
                                .build())
                        .build())
                .build();
        actions.add(outputNodeConnectorAction);

        //ApplyActions
        ApplyActions applyActions = new ApplyActionsBuilder().setAction(actions).build();
        List<Instruction> instructions = new ArrayList<>();

        //Instruction for Actions
        Instruction applyActionsInstruction = new InstructionBuilder()
                .setOrder(0).setInstruction(new ApplyActionsCaseBuilder()
                        .setApplyActions(applyActions)
                        .build())
                .build();

        instructions.add(applyActionsInstruction);

        //Build all the instructions together, based on the Instructions list
        Instructions allInstructions = new InstructionsBuilder()
                .setInstruction(instructions)
                .build();

        flowBuilder
                .setMatch(match)
                .setBufferId(OFConstants.OFP_NO_BUFFER)
                .setInstructions(allInstructions)
                .setPriority(1000)
                .setHardTimeout(30000)
                .setIdleTimeout(30000)
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }

    private Group createGroupForIngressSwitch(String standardOutputPort, String backupPort){

        if (!groupsHashMap.containsKey(standardOutputPort)) {
            GroupId id = new GroupId(groupId);
            String groupName = "group" + groupId;

            BucketId bId = new BucketId(bucketId);
            BucketKey bucketKey = new BucketKey(bId);
            List<Bucket> bucketList = new ArrayList<>();

            //Actions for Bucket
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            Action outputNodeConnectorAction = actionBuilder
                    .setOrder(0).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(standardOutputPort))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction);

            //Bucket creation for current port
            Bucket bucket = new BucketBuilder()
                    .setBucketId(bId)
                    .setWatchPort(Long.parseLong(standardOutputPort.split(":")[2]))
                    .setKey(bucketKey)
                    .setAction(actions)
                    .build();
            bucketList.add(bucket);
            bucketId++;

            BucketId bIdFailover = new BucketId(bucketId);
            BucketKey bucketKeyFailover = new BucketKey(bIdFailover);

            //Actions for Failover Bucket
            ActionBuilder actionBuilderFailover = new ActionBuilder();
            List<Action> actionsFailover = new ArrayList<>();

            Action outputNodeConnectorActionFailover = actionBuilderFailover
                    .setOrder(1).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                     //               .setOutputNodeConnector(new Uri(OutputPortValues.INPORT.toString()))
                                    .setOutputNodeConnector(new Uri(backupPort))
                                    .build())
                            .build())
                    .build();
            actionsFailover.add(outputNodeConnectorActionFailover);

            //Bucket creation for failover port
            Long failoverPort = Long.parseLong(backupPort.split(":")[2].toString());
            Bucket bucketFailover = new BucketBuilder()
                    .setBucketId(bIdFailover)
                    .setWatchPort(failoverPort)
                    .setKey(bucketKeyFailover)
                    .setAction(actionsFailover)
                    .build();
            bucketList.add(bucketFailover);
            bucketId++;

            Buckets buckets = new BucketsBuilder().setBucket(bucketList).build();

            Group group = new GroupBuilder().setGroupName(groupName)
                    .setBarrier(false)
                    .setGroupId(id)
                    .setGroupType(GroupTypes.GroupFf)
                    .setBuckets(buckets)
                    .build();

            //Write group created to Group hash map
            groupsHashMap.put(standardOutputPort, groupId.intValue());

            groupId++;
            return group;
        }
        else {
            return null;
        }
    }

    private Group createGroupForCoreAndEgressSwitch(String standardOutputPort, String backupPort){

        if (!groupsHashMap.containsKey(standardOutputPort)) {
            GroupId id = new GroupId(groupId);
            String groupName = "group" + groupId;

            BucketId bId = new BucketId(bucketId);
            BucketKey bucketKey = new BucketKey(bId);
            List<Bucket> bucketList = new ArrayList<>();

            //Actions for Bucket
            ActionBuilder actionBuilder = new ActionBuilder();
            List<Action> actions = new ArrayList<>();
            Action outputNodeConnectorAction = actionBuilder
                    .setOrder(0).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(standardOutputPort))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction);

            //Bucket creation for current port
            Bucket bucket = new BucketBuilder()
                    .setBucketId(bId)
                    .setWatchPort(Long.parseLong(standardOutputPort.split(":")[2]))
                    .setKey(bucketKey)
                    .setAction(actions)
                    .build();
            bucketList.add(bucket);
            bucketId++;

            BucketId bIdFailover = new BucketId(bucketId);
            BucketKey bucketKeyFailover = new BucketKey(bIdFailover);

            //Actions for Failover Bucket
            ActionBuilder actionBuilderFailover = new ActionBuilder();
            List<Action> actionsFailover = new ArrayList<>();

            Action outputNodeConnectorActionFailover = actionBuilderFailover
                    .setOrder(1).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(OutputPortValues.INPORT.toString()))
                                 //   .setOutputNodeConnector(new Uri(backupPort))
                                    .build())
                            .build())
                    .build();
            actionsFailover.add(outputNodeConnectorActionFailover);

            //Bucket creation for failover port
            Long failoverPort = Long.parseLong(backupPort.split(":")[2].toString());
            Bucket bucketFailover = new BucketBuilder()
                    .setBucketId(bIdFailover)
                    .setWatchPort(failoverPort)
                    .setKey(bucketKeyFailover)
                    .setAction(actionsFailover)
                    .build();
            bucketList.add(bucketFailover);
            bucketId++;

            Buckets buckets = new BucketsBuilder().setBucket(bucketList).build();

            Group group = new GroupBuilder().setGroupName(groupName)
                    .setBarrier(false)
                    .setGroupId(id)
                    .setGroupType(GroupTypes.GroupFf)
                    .setBuckets(buckets)
                    .build();

            //Write group created to Group hash map
            groupsHashMap.put(standardOutputPort, groupId.intValue());

            groupId++;
            return group;
        }
        else {
            return null;
        }
    }

    private InstanceIdentifier<Flow> createInstanceIdentifierForFlow(String switchToConfigure, Flow flow){
        InstanceIdentifier<Flow> flowPath = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(switchToConfigure)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId()))
                .child(Flow.class, new FlowKey(flow.getId())).build();
        return flowPath;
    }

    private InstanceIdentifier<Group> createInstanceIdentifierForGroup(String switchToConfigure, Group group){
        InstanceIdentifier<Group> groupPath = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(switchToConfigure)))
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId())).build();
        return groupPath;
    }

    public void removeReactiveFlows(){
        System.out.println("We have " + connectorsWithFlowsToRemove.size() + " flows to remove.");

        WriteTransaction transaction = db.newWriteOnlyTransaction();
        for (String connector : connectorsWithFlowsToRemove) {
            String[] switchParts = connector.split(":");
            String switchName = switchParts[0].concat(":").concat(switchParts[1]);

            // build instance identifier for flow
            InstanceIdentifier<Flow> flowPath = InstanceIdentifier
                    .builder(Nodes.class)
                    .child(Node.class, new NodeKey(new NodeId(switchName)))
                    .augmentation(FlowCapableNode.class)
                    .child(Table.class, new TableKey((short) 0))
                    .child(Flow.class, new FlowKey(new FlowId(connector))).build();

            transaction.delete(LogicalDatastoreType.CONFIGURATION, flowPath);
        }
        transaction.submit();
    }
}




