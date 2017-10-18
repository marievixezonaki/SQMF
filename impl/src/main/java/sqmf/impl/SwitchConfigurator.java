/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.WriteTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.openflowplugin.api.OFConstants;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev130715.Uri;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.yang.types.rev130715.MacAddress;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.OutputActionCaseBuilder;
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
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.Instruction;
import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.types.rev131026.instruction.list.InstructionBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeConnectorId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.NodeId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.Node;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.nodes.NodeKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.l2.types.rev130827.EtherType;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetSourceBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.ethernet.match.fields.EthernetTypeBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.EthernetMatchBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.model.match.types.rev131026.match.IpMatchBuilder;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.BucketId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupId;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.GroupTypes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.Buckets;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.BucketsBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.Bucket;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.group.buckets.BucketKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.Group;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.group.types.rev131018.groups.GroupKey;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.GroupActionCaseBuilder;
import org.opendaylight.yang.gen.v1.urn.opendaylight.action.types.rev131112.action.action.group.action._case.GroupActionBuilder;

/**
 * The class containing the necessary methods for the switches configuration with OF rules.
 *
 * @author Marievi Xezonaki
 */
public class SwitchConfigurator {

    private DataBroker db;
    private AtomicLong flowCookieInc = new AtomicLong(0x2a00000000000000L);
    private Integer flowId = 1;
    private static HashMap<String, Integer> groupsHashMap = new HashMap();
    private static final Integer IP_ETHERTYPE = 0x0800;
    private Integer UDP_PROTOCOL = 17;
    private static Long groupId = 1L;
    private static Long bucketId = 1L;



    /**
     * The constructor method.
     *
     * @param db    The databroker to assist in accessing the MD-SAL.
     * @return
     */
    public SwitchConfigurator(DataBroker db) {
        this.db = db;
    }



    /**
     * The method which configures the ingress and the egress node of the main path for QoE Monitoring.
     *
     * @param ingressSwitch     The main path's ingress switch.
     * @param egressSwitch      The main path's egress switch.
     * @param inputPorts        The input ports for all the main path's nodes.
     * @param outputPorts       The output ports for all the main path's nodes.
     * @return
     */
    public void configureIngressAndEgressForMonitoring(String ingressSwitch, String egressSwitch, HashMap<String, Integer> inputPorts, HashMap<String, Integer> outputPorts){

        // Line used only for QoE monitoring-only
        //  manuallyConfigureIngressAndEgressForTests(ingressSwitch, egressSwitch, inputPorts, outputPorts);
        WriteTransaction transaction = db.newWriteOnlyTransaction();

        //create flow for ingress node
        Flow flowForIngressMainPath = createIngressAndEgressFlowForMonitoring(inputPorts.get(ingressSwitch), outputPorts.get(ingressSwitch));
        InstanceIdentifier<Flow> instanceIdentifierIngressMainPath = createInstanceIdentifierForFlow(ingressSwitch, flowForIngressMainPath);

        //create flow for egress node
        Flow flowForEgressMainPath = createIngressAndEgressFlowForMonitoring(inputPorts.get(egressSwitch), outputPorts.get(egressSwitch));
        InstanceIdentifier<Flow> instanceIdentifierEgressMainPath = createInstanceIdentifierForFlow(egressSwitch, flowForEgressMainPath);

        //put flows into transaction
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngressMainPath, flowForIngressMainPath, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierEgressMainPath, flowForEgressMainPath, true);

        //submit transaction
        transaction.submit();
    }



    /**
     * The method which creates the flows for the ingress and the egress node of the main path for QoE Monitoring.
     *
     * @param inPort        The input port of the node, on the main path.
     * @param outPort       The output port of the node, on the main path.
     * @return              The created flow.
     */
    private Flow createIngressAndEgressFlowForMonitoring(Integer inPort, Integer outPort){

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

        //output to controller
        Action outputToControllerAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(65535)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputToControllerAction);

        Action outputNodeConnectorAction = actionBuilder
                .setOrder(1).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(outPort.toString()))
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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }



    /**
     * The method which configures the main path's links for delay monitoring (for VoIP applications).
     *
     * @param links         The main path's links.
     * @param srcMac        The source MAC address to be set to the packet which will be sent from each node.
     * @return
     */
    public void configureNodesForDelayMonitoring(List<DomainLink> links, String srcMac){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        for (DomainLink link : links){
            String switchToConfigure = link.getLink().getDestination().getDestNode().getValue();
            Flow flow = createFlowForDelayMonitoring(srcMac);
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);
        }

        transaction.submit();
    }



    /**
     * The method which creates the flows for the main path's nodes for delay monitoring (for VoIP applications).
     *
     * @param srcMac        The source MAC address to be set to the packet which will be sent from each node.
     * @return              The created flow.
     */
    public Flow createFlowForDelayMonitoring(String srcMac){

        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        Match match = new MatchBuilder()
                .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetSource(new EthernetSourceBuilder()
                                .setAddress(new MacAddress(srcMac))
                        .build())
                        .build())
                .build();

        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<Action>();

        //output to controller
        Action outputToControllerAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(65535)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputToControllerAction);

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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }



    /**
     * The method which establishes the main path for packets transmission.
     *
     * @param links             The main path's links.
     * @param inputPorts        The input ports for all the main path's nodes.
     * @param outputPorts       The output ports for all the main path's nodes.
     * @return
     */
    public void configureNodesForUDPTrafficForwarding(List<DomainLink> links, HashMap<String, Integer> inputPorts, HashMap<String, Integer> outputPorts){

        WriteTransaction transaction = db.newWriteOnlyTransaction();
        List<DomainLink> domainLinks = links;
        domainLinks.remove(domainLinks.get(domainLinks.size()-1));
        for (DomainLink link : domainLinks){
            Flow flowForUDPForwarding = createFlowForUDPForwarding(inputPorts.get(link.getLink().getDestination().getDestNode().getValue()), outputPorts.get(link.getLink().getDestination().getDestNode().getValue()));
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(link.getLink().getDestination().getDestNode().getValue(), flowForUDPForwarding);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flowForUDPForwarding, true);
        }

        transaction.submit();
    }



    /**
     * The method which creates the flows for the main path's nodes for path establishment.
     *
     * @param inPort        The input port of the node, on the main path.
     * @param outPort       The output port of the node, on the main path.
     * @return              The created flow.
     */
    public Flow createFlowForUDPForwarding(Integer inPort, Integer outPort){
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

        //output to port
        Action outputAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(outPort.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputAction);

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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }



    /**
     * The method which deletes a flow from a switch.
     *
     * @param switchToRemoveFlow        The switch to remove a flow from.
     * @param flowId                    The ID of the flow to be removed.
     * @return
     */
    public void deleteFlow(String switchToRemoveFlow, String flowId) {

        InstanceIdentifier<Flow> flowPath = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(switchToRemoveFlow)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey((short) 0))
                .child(Flow.class, new FlowKey(new FlowId(flowId))).build();

        WriteTransaction transaction = db.newWriteOnlyTransaction();
        transaction.delete(LogicalDatastoreType.CONFIGURATION, flowPath);
        transaction.submit();
    }



    /**
     * The method which creates an instance identifier for a flow.
     *
     * @param switchToConfigure   The switch were the flow will be added
     * @param flow                The flow to be added
     * @return                    The created instance identifier
     */
    private InstanceIdentifier<Flow> createInstanceIdentifierForFlow(String switchToConfigure, Flow flow){
        InstanceIdentifier<Flow> flowPath = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(switchToConfigure)))
                .augmentation(FlowCapableNode.class)
                .child(Table.class, new TableKey(flow.getTableId()))
                .child(Flow.class, new FlowKey(flow.getId())).build();
        return flowPath;
    }



    // --------------------------------------  TESTING METHODS TO SUPPORT QoE MONITORING ONLY ---------------------------------------


    /**
     * The method which replicates the default rules in the ingress and egress switches, but extends them so that statistics
     * are also sent to the controller for packet loss monitoring.
     *
     * @param ingressSwitch     The main path's ingress switch.
     * @param egressSwitch      The main path's egress switch.
     * @param inputPorts        The input ports for all the main path's nodes.
     * @param outputPorts       The output ports for all the main path's nodes.
     * @return
     */
    public void manuallyConfigureIngressAndEgressForTests(String ingressSwitch, String egressSwitch, HashMap<String, Integer> inputPorts, HashMap<String, Integer> outputPorts){
        WriteTransaction transaction = db.newWriteOnlyTransaction();

        //create flows for ingress node
        Flow flowForIngressMainPath = createManualIngressAndEgressFlowForMonitoring(inputPorts.get(ingressSwitch));
        InstanceIdentifier<Flow> instanceIdentifierIngressMainPath = createInstanceIdentifierForFlow(ingressSwitch, flowForIngressMainPath);

        //create flows for egress node
        Flow flowForEgressMainPath = createManualIngressAndEgressFlowForMonitoring(inputPorts.get(egressSwitch));
        InstanceIdentifier<Flow> instanceIdentifierEgressMainPath = createInstanceIdentifierForFlow(egressSwitch, flowForEgressMainPath);
        Flow extraForEgress = extraFlowEgress(1);
        InstanceIdentifier<Flow> instanceIdentifierExtraEgress = createInstanceIdentifierForFlow(egressSwitch, extraForEgress);

        //put flows into transaction
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngressMainPath, flowForIngressMainPath, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierEgressMainPath, flowForEgressMainPath, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierExtraEgress, extraForEgress, true);

        transaction.submit();
    }




    /**
     * The method which also takes into account the packets which will arrive to the egress switch from the backup path
     * and inserts rule to gather statistics from there as well.
     *
     * @param inPort    The egress switch's input port, on the main path.
     * @return          The created flow.
     */
    private Flow extraFlowEgress(Integer inPort){
        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        NodeConnectorId inputPort = new NodeConnectorId(inPort.toString());

        Match match = new MatchBuilder()
             /*   .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetType(new EthernetTypeBuilder()
                                .setType(new EtherType(IP_ETHERTYPE.longValue()))
                                .build())
                        .build())*/
                .setInPort(inputPort)
                .build();

        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<Action>();

        //output to controller
        Action outputToControllerAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(65535)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputToControllerAction);

        Integer a = 3, b = 2;
        Action outputNodeConnectorAction1 = actionBuilder
                .setOrder(1).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(a.toString()))
                                .build())
                        .build())
                .build();
        Action outputNodeConnectorAction2 = actionBuilder
                .setOrder(2).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setOutputNodeConnector(new Uri(b.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputNodeConnectorAction1);
        actions.add(outputNodeConnectorAction2);

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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }



    /**
     * The method which inserts rules in the ingress and egress switches of the main path to replicate and extend the
     * default rules for packet loss monitoring.
     *
     * @param inPort    The ingress and egress switches' input port, on the main path.
     * @return          The created flow.
     */
    private Flow createManualIngressAndEgressFlowForMonitoring(Integer inPort){

        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        NodeConnectorId inputPort = new NodeConnectorId(inPort.toString());

        Match match = new MatchBuilder()
             /*   .setEthernetMatch(new EthernetMatchBuilder()
                        .setEthernetType(new EthernetTypeBuilder()
                                .setType(new EtherType(IP_ETHERTYPE.longValue()))
                                .build())
                        .build())*/
                .setInPort(inputPort)
                .build();

        ActionBuilder actionBuilder = new ActionBuilder();
        List<Action> actions = new ArrayList<Action>();

        //output to controller
        Action outputToControllerAction = actionBuilder
                .setOrder(0).setAction(new OutputActionCaseBuilder()
                        .setOutputAction(new OutputActionBuilder()
                                .setMaxLength(65535)
                                .setOutputNodeConnector(new Uri(OutputPortValues.CONTROLLER.toString()))
                                .build())
                        .build())
                .build();
        actions.add(outputToControllerAction);

        if (inPort == 2){
            Integer a = 3, b = 1;
            Action outputNodeConnectorAction1 = actionBuilder
                    .setOrder(1).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(a.toString()))
                                    .build())
                            .build())
                    .build();
            Action outputNodeConnectorAction2 = actionBuilder
                    .setOrder(2).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(b.toString()))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction1);
            actions.add(outputNodeConnectorAction2);
        }
        else if (inPort == 3){
            Integer a = 2, b = 1;
            Action outputNodeConnectorAction1 = actionBuilder
                    .setOrder(1).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(a.toString()))
                                    .build())
                            .build())
                    .build();
            Action outputNodeConnectorAction2 = actionBuilder
                    .setOrder(2).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(b.toString()))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction1);
            actions.add(outputNodeConnectorAction2);
        }
        else if (inPort == 1){
            Integer a = 3, b = 2;
            Action outputNodeConnectorAction1 = actionBuilder
                    .setOrder(1).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(a.toString()))
                                    .build())
                            .build())
                    .build();
            Action outputNodeConnectorAction2 = actionBuilder
                    .setOrder(2).setAction(new OutputActionCaseBuilder()
                            .setOutputAction(new OutputActionBuilder()
                                    .setOutputNodeConnector(new Uri(b.toString()))
                                    .build())
                            .build())
                    .build();
            actions.add(outputNodeConnectorAction1);
            actions.add(outputNodeConnectorAction2);
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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }



    // --------------------------------------   FAST FAILOVER METHODS ---------------------------------------

    /**
     * The method which configures the ingress (source) switch of the main path - used for Fast Failover.
     *
     * @param sourceNode    The source node of the main path
     * @param inPort        The input port of the source node
     * @param outputPort    The output port of the source node
     * @param failoverPort  The failover port of the source node (where the paths will be redirected in case of link failure)
     * @return
     */
 /*   public void configureIngress(DomainNode sourceNode, Integer inPort, Integer outputPort, Integer failoverPort){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        String standardOutputPort = sourceNode.getODLNodeID().concat(":").concat(outputPort.toString());
        String backupPort = sourceNode.getODLNodeID().concat(":").concat(failoverPort.toString());

        //create flows and groups
        Group groupForIngress = createGroupForIngressSwitch(standardOutputPort, backupPort);
        InstanceIdentifier<Group> instanceIdentifierGroupIngress = createInstanceIdentifierForGroup(sourceNode.getODLNodeID(), groupForIngress);
        Flow flowForIngress = createFlow(standardOutputPort, inPort);
        InstanceIdentifier<Flow> instanceIdentifierIngress = createInstanceIdentifierForFlow(sourceNode.getODLNodeID(), flowForIngress);
        Flow flowForIngressFailover = createFlow(backupPort, outputPort);
        InstanceIdentifier<Flow> instanceIdentifierIngressFailover = createInstanceIdentifierForFlow(sourceNode.getODLNodeID(), flowForIngressFailover);

        //put flows and groups into transaction
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierGroupIngress, groupForIngress, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngress, flowForIngress, true);
        transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierIngressFailover, flowForIngressFailover, true);

        transaction.submit();
    }*/

    /**
     * The method which configures the core and the egress (destination) switches of the main path - used for Fast Failover.
     *
     * @param mainPathLinks  The links of the main path
     * @param inputPorts     The input ports of the main path's nodes
     * @param outputPorts    The output ports of the main path's nodes
     * @param failoverPorts  The failover ports of the main path's nodes (where the paths will be redirected in case of link failure)
     * @return
     */
  /*  public void configureCoreAndEgress(List<DomainLink> mainPathLinks, HashMap<String, Integer> inputPorts, HashMap<String, Integer> outputPorts, HashMap<String, Integer> failoverPorts){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        for (DomainLink link : mainPathLinks){
            String switchToConfigure = link.getLink().getDestination().getDestNode().getValue();
            String standardOutputPort = switchToConfigure.concat(":").concat(outputPorts.get(switchToConfigure).toString());
            String backupPort = switchToConfigure.concat(":").concat(failoverPorts.get(switchToConfigure).toString());

            //create flows and groups
            Group group = createGroupForCoreAndEgressSwitch(standardOutputPort, backupPort);
            InstanceIdentifier<Group> instanceIdentifierGroup = createInstanceIdentifierForGroup(switchToConfigure, group);
            Flow flow = createFlow(standardOutputPort, inputPorts.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);
            Flow flowFailover = createFlow(backupPort, outputPorts.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifierFailover = createInstanceIdentifierForFlow(switchToConfigure, flowFailover);

            //put flows and groups into transaction
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierGroup, group, true);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifierFailover, flowFailover, true);

        }

        transaction.submit();

    }*/

    /**
     * The method which configures the core and the egress (destination) switch of the failover path - used for Fast Failover.
     *
     * @param failoverPathLinks     The links of the failover path
     * @param inputPortsFailover    The input ports of the failover path's nodes
     * @param outputPortsFailover   The output ports of the failover path's nodes
     * @return
     */
 /*   public void configureFailoverPath(List<DomainLink> failoverPathLinks, HashMap<String, Integer> inputPortsFailover, HashMap<String, Integer> outputPortsFailover){

        WriteTransaction transaction = db.newWriteOnlyTransaction();

        for (DomainLink link : failoverPathLinks){
            String switchToConfigure = link.getLink().getDestination().getDestNode().getValue();
            String standardOutputPort = switchToConfigure.concat(":").concat(outputPortsFailover.get(switchToConfigure).toString());

            //create flow
            Flow flow = createFlowFailover(standardOutputPort, inputPortsFailover.get(switchToConfigure));
            InstanceIdentifier<Flow> instanceIdentifier = createInstanceIdentifierForFlow(switchToConfigure, flow);

            //put flow into transaction
            transaction.put(LogicalDatastoreType.CONFIGURATION, instanceIdentifier, flow, true);

        }

        transaction.submit();

    }*/

    /**
     * The method which creates a flow for a node - used for Fast Failover.
     *
     * @param standardOutputPort    The output port of the node
     * @param inPort                The input port of the node
     * @return                      The created flow
     */
  /*  private Flow createFlow(String standardOutputPort, Integer inPort){

        FlowBuilder flowBuilder = new FlowBuilder()
                .setTableId((short) 0)
                .setFlowName("flow" + flowId)
                .setId(new FlowId(flowId.toString()));
        flowId++;

        NodeConnectorId inputPort = new NodeConnectorId(inPort.toString());

        //match udp and input port
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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }*/

    /**
     * The method which creates a flow for a failover node - used for Fast Failover.
     *
     * @param standardOutputPort    The output port of the node
     * @param inPort                The input port of the node
     * @return                      The created flow
     */
 /*   private Flow createFlowFailover(String standardOutputPort, Integer inPort){

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

        //output to port
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
                .setCookie(new FlowCookie(BigInteger.valueOf(flowCookieInc.getAndIncrement())))
                .setFlags(new FlowModFlags(false, false, false, false, false));

        return flowBuilder.build();
    }*/

    /**
     * The method which creates a group for the main path's nodes, in proactive failover - used for Fast Failover.
     *
     * @param standardOutputPort    The output port of the node
     * @param backupPort            The failover port of the node
     * @return                      The created flow
     */
  /*  private Group createGroupForIngressSwitch(String standardOutputPort, String backupPort){

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
                                    .setOutputNodeConnector(new Uri(backupPort))
//                                    .setOutputNodeConnector(new Uri(OutputPortValues.INPORT.toString()))
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
    }*/

    /**
     * The method which creates a flow for the main path's ingress node - used for Fast Failover.
     *
     * @param standardOutputPort    The output port of the node
     * @param backupPort            The backup port of the node
     * @return                      The created flow
     */
  /*  private Group createGroupForCoreAndEgressSwitch(String standardOutputPort, String backupPort){

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
    }*/



    /**
     * The method which creates an instance identifier for a group - used for Fast Failover.
     * @param switchToConfigure     The switch where the group will be inserted.
     * @param group                 The group to be inserted.
     * @return                      The created instance identifier
     */
 /*   private InstanceIdentifier<Group> createInstanceIdentifierForGroup(String switchToConfigure, Group group){
        InstanceIdentifier<Group> groupPath = InstanceIdentifier
                .builder(Nodes.class)
                .child(Node.class, new NodeKey(new NodeId(switchToConfigure)))
                .augmentation(FlowCapableNode.class)
                .child(Group.class, new GroupKey(group.getGroupId())).build();
        return groupPath;
    }*/

}



