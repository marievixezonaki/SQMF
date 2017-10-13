/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package sqmf.impl;

import com.google.common.util.concurrent.CheckedFuture;
import com.google.common.util.concurrent.Futures;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.ReadOnlyTransaction;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.md.sal.common.api.data.ReadFailedException;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.inventory.rev130819.Nodes;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev141210.SqmfService;
//import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev141210.StartFailoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev141210.StartFailoverInput;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev141210.StartMonitoringLinksInput;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.nio.ch.Net;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.Future;

/**
 * The core class of the implementation.
 *
 */
public class SqmfImplementation implements SqmfService {

    private static final Logger LOG = LoggerFactory.getLogger(SqmfImplementation.class);
    private DijkstraShortestPath<NodeId, Link> shortestPath = null;
    private BindingAwareBroker.ProviderContext session;
    private static HashMap<String, Integer> inputPorts = new HashMap<>();
    private static HashMap<String, Integer> outputPorts = new HashMap<>();
    private static HashMap<String, Integer> failoverPorts = new HashMap<>();
    private static HashMap<String, Integer> inputPortsFailover = new HashMap<>();
    private static HashMap<String, Integer> outputPortsFailover = new HashMap<>();
    private static DataBroker db;
    private static String srcMacForDelayMeasuring = "00:00:00:00:00:09";
    public static String srcNode = null;
    public static String dstNode = null;
    private static RpcProviderRegistry rpcProviderRegistry;
    private NotificationProviderService notificationService;
    public static GraphPath<Integer, DomainLink> mainGraphWalk = null, failoverGraphWalk = null;
    public static double QoEThreshold;
    private static Timer timer;
    private static MonitorLinksTask monitorLinksTask;
    public static boolean fastFailover = false;
    public static String applicationType = "";
    private String videoAbsolutePath;
    private int videoCase = 0;

    public SqmfImplementation(BindingAwareBroker.ProviderContext session, DataBroker db, RpcProviderRegistry rpcProviderRegistry, NotificationProviderService notificationService) {
        this.db = db;
        this.session = session;
        this.rpcProviderRegistry = rpcProviderRegistry;
        this.notificationService = notificationService;
    }

    /**
     * The method which starts monitoring the packet loss and delay of links, when the user asks it.
     *
     * @param input     The input of the RPC, containing the source node, the destination node, the QoE threshold and the application type (VoIP or video).
     */
    @Override
    public Future<RpcResult<Void>> startMonitoringLinks(StartMonitoringLinksInput input) {

        //read user input
        if (input != null){
            if (input.getSrcNode() != null && input.getDstNode() != null && input.getQoEThreshold() != null && input.getApplication() != null){
                srcNode = input.getSrcNode();
                dstNode = input.getDstNode();
                try {
                    Float QoE = Float.parseFloat(input.getQoEThreshold());
                    QoEThreshold = QoE.doubleValue();
                }
                catch (NumberFormatException e){
                    LOG.info("Wrong number format for QoE threshold, try again.");
                    return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
                }
                applicationType = input.getApplication().getName();
            }
        }
        else{
            LOG.info("A field of the input is empty, try again.");
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }

        // if the application type is video, launch a file chooser to select a video file to be streamed
        if (applicationType.equals(Video.getName())){
            FileDialog dialog = new FileDialog((Frame)null, "Select File to Open");
            dialog.setMode(FileDialog.LOAD);
            dialog.setVisible(true);
            videoAbsolutePath = dialog.getDirectory() + dialog.getFile();
            videoCase = findVideoCase(videoAbsolutePath);
            if (videoCase == 0){
                LOG.info("Video required specifications not met, choose another video.");
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }
        }

        //first, add rules to ingress and egress nodes to forward their packets to controller
        if (NetworkGraph.getInstance().getGraphNodes() != null && NetworkGraph.getInstance().getGraphLinks() != null) {
            Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
            DomainNode sourceNode = domainNodes.get(srcNode);
            DomainNode destNode = domainNodes.get(dstNode);

            //check if the given switches are edge switches, therefore connected to hosts
            if (!checkIfEdgeSwitches(sourceNode, destNode)){
                LOG.info("Not edge switches given, returning...");
                return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
            }

            List<GraphPath<Integer, DomainLink>> possiblePaths = createPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
            if (possiblePaths.size() > 1) {
                GraphPath<Integer, DomainLink> mainPath = possiblePaths.get(0);
                GraphPath<Integer, DomainLink> failoverPath = possiblePaths.get(1);

                //determine main and failover path
                mainGraphWalk = mainPath;
                failoverGraphWalk = failoverPath;
                findPorts(mainGraphWalk, sourceNode, destNode);

                //register packet processing listener
                PacketProcessing packetProcessingListener = new PacketProcessing(srcNode, dstNode, srcMacForDelayMeasuring);
                if (notificationService != null) {
                    notificationService.registerNotificationListener(packetProcessingListener);
                }
                SwitchConfigurator switchConfigurator = new SwitchConfigurator(db);
                //configure ingress and egress switches to send their packets to controller (for packet loss monitoring)
                switchConfigurator.configureIngressAndEgressForMonitoring(srcNode, dstNode, inputPorts, outputPorts);
                /* then, add rules to all nodes of both main and failover path to forward packets
                   with specific MAC to controller (for delay monitoring) */
                switchConfigurator.configureNodesForDelayMonitoring(mainGraphWalk.getEdgeList(), srcMacForDelayMeasuring);
                switchConfigurator.configureNodesForDelayMonitoring(failoverGraphWalk.getEdgeList(), srcMacForDelayMeasuring);
                switchConfigurator.configureNodesForUDPTrafficForwarding(mainGraphWalk.getEdgeList(), inputPorts, outputPorts);
            }
        }
        else{
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }

        //finally, start monitoring links
        timer = new Timer();
        monitorLinksTask = new MonitorLinksTask(db, rpcProviderRegistry, srcMacForDelayMeasuring, videoAbsolutePath, Video.getVideoFPS(videoAbsolutePath), videoCase);
        timer.schedule(monitorLinksTask, 0, 5000);

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    public int findVideoCase(String videoAbsolutePath){

        //check what category video belongs to
        int keyFrame = Video.getKeyFrame(videoAbsolutePath);
        String codec = Video.getVideoCodec(videoAbsolutePath);
        String format = Video.getVideoFormat(videoAbsolutePath);
        if ((keyFrame == -1) || (codec == null) || (format == null)){
            return 0;
        }
        if ((keyFrame == 1) && codec.equals("h264") && format.equals("640x480")){
            return 5;
        }
        if ((keyFrame == 1) && codec.equals("mpeg4") && format.equals("640x480")){
            return 4;
        }
        if ((keyFrame == 1) && codec.equals("mpeg2") && format.equals("640x480")){
            return 3;
        }
        if ((keyFrame == 1) && codec.equals("mpeg4") && format.equals("160x120")){
            return 2;
        }
        if ((keyFrame == 1) && codec.equals("mpeg4") && format.equals("320x240")){
            return 1;
        }
        return 0;
    }

    /**
     * The method which changes the main with the failover path in case the QoE threshold is not met. This way,
     * packets start being forwarded to the failover path for better QoE.
     *
     */
    public static void changePath(){

        GraphPath<Integer, DomainLink> temp = mainGraphWalk;
        mainGraphWalk = failoverGraphWalk;
        failoverGraphWalk = temp;

        MonitorLinksTask.isFailover = true;

        if (NetworkGraph.getInstance().getGraphNodes() != null && NetworkGraph.getInstance().getGraphLinks() != null) {
            Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
            findPorts(mainGraphWalk, domainNodes.get(srcNode), domainNodes.get(dstNode));
            SwitchConfigurator switchConfigurator = new SwitchConfigurator(db);
            switchConfigurator.configureIngressAndEgressForMonitoring(srcNode, dstNode, inputPorts, outputPorts);
            switchConfigurator.configureNodesForUDPTrafficForwarding(mainGraphWalk.getEdgeList(), inputPorts, outputPorts);
        }

    }

    /**
     * The method which stops the timer task monitoring the links.
     *
     */
    @Override
    public Future<RpcResult<Void>> stopMonitoringLinks() {
        System.out.println("Stopping the monitoring of links.");
        if (monitorLinksTask != null) {
            monitorLinksTask.cancel();
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
        }

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
    }

    /**
     * The method which initializes the failover procedure.
     *
     * @param input     The user input, defining the two nodes between which a path will be established for traffic transmission.
     * @return          It returns a void future result.
     */
    @Override
    public Future<RpcResult<Void>> startFailover(StartFailoverInput input) {
        fastFailover = true;
        LOG.info("Configuring switches for resilience.");

        if (input.getSrcNode() != null && input.getDstNode() != null) {
            srcNode = input.getSrcNode();
            dstNode = input.getDstNode();
        }
        Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
        DomainNode sourceNode = domainNodes.get(srcNode);
        DomainNode destNode = domainNodes.get(dstNode);

        //check if the given switches are edge switches, therefore connected to hosts
        if (!checkIfEdgeSwitches(sourceNode, destNode)){
            LOG.info("Not edge switches given, returning...");
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }

        //create the two best paths (minimum number of hops) between the given switches
        List<GraphPath<Integer, DomainLink>> possiblePaths = createPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
        if (possiblePaths.size() < 2){
            LOG.info("There is no backup path between given switches, resilience cannot be achieved. Returning...");
            return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());
        }
        //configure the switches of the main path beforehand
        implementFailover();

        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());

    }

    /**
     * The method which implements the resilience. For each node of the main path, finds an alternative port (to send
     * the packet back to the ingress switch in case of a link failure) and configures the nodes before traffic generation.
     */
    public static void implementFailover(){
        if (NetworkGraph.getInstance().getGraphNodes() != null && NetworkGraph.getInstance().getGraphLinks() != null) {
            Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
            DomainNode sourceNode = domainNodes.get(srcNode);
            DomainNode destNode = domainNodes.get(dstNode);

            List<GraphPath<Integer, DomainLink>> possiblePaths = createPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
            //if there are two paths available, the first will be the main path and the second the backup path
            if (possiblePaths.size() > 1){
                GraphPath<Integer, DomainLink> mainPath = possiblePaths.get(0);
                GraphPath<Integer, DomainLink> failoverPath = possiblePaths.get(1);

                //find ports for nodes of main path
                findPorts(mainPath, sourceNode, destNode);

                //find failover ports for nodes of main path
                for (DomainLink link : mainPath.getEdgeList()){
                    failoverPorts.put(link.getLink().getDestination().getDestNode().getValue(), inputPorts.get(link.getLink().getDestination().getDestNode().getValue()));
                }
                failoverPorts.put(sourceNode.getODLNodeID(), Integer.parseInt(failoverPath.getEdgeList().get(0).getLink().getSource().getSourceTp().getValue().split(":")[2]));

                //find in and out ports for nodes of failover path
                for (DomainLink link : failoverPath.getEdgeList()){
                    inputPortsFailover.put(link.getLink().getDestination().getDestNode().getValue(), Integer.parseInt(link.getLink().getDestination().getDestTp().getValue().split(":")[2]));
                    outputPortsFailover.put(link.getLink().getSource().getSourceNode().getValue(), Integer.parseInt(link.getLink().getSource().getSourceTp().getValue().split(":")[2]));

                }
                inputPortsFailover.put(sourceNode.getODLNodeID(), inputPorts.get(sourceNode.getODLNodeID()));
                outputPortsFailover.put(destNode.getODLNodeID(), outputPorts.get(destNode.getODLNodeID()));

                //configure the switches of both main and failover path
                SwitchConfigurator switchConfigurator = new SwitchConfigurator(db);
                switchConfigurator.configureIngress(sourceNode, inputPorts.get(sourceNode.getODLNodeID()), outputPorts.get(sourceNode.getODLNodeID()), failoverPorts.get(sourceNode.getODLNodeID()));
                switchConfigurator.configureCoreAndEgress(mainPath.getEdgeList(), inputPorts, outputPorts, failoverPorts);
            }
        }
    }

    /**
     * The method which checks if both given nodes are edge nodes.
     *
     * @param sourceNode    The source node given by the user.
     * @param destNode      The destination node given by the user.
     * @return              It returns true if both given nodes are edge nodes and false if either of them is not.
     */
    private boolean checkIfEdgeSwitches(DomainNode sourceNode, DomainNode destNode){
        List<Link> graphLinks = NetworkGraph.getInstance().getGraphLinks();
        Boolean isSourceEdge = false, isDestEdge = false;

        //check if graph links containing "host" also contain the source or destination node
        for (Link graphLink : graphLinks){
            if (graphLink.getLinkId().getValue().contains("host")){
                if (graphLink.getLinkId().getValue().contains(destNode.getODLNodeID())){
                    isDestEdge = true;
                }
                else if (graphLink.getLinkId().getValue().contains(sourceNode.getODLNodeID())){
                    isSourceEdge = true;
                }
            }
        }
        if (isDestEdge && isSourceEdge) {
            return true;
        }
        else{
            return false;
        }
    }

    /**
     * The method which creates the two shortest paths between the two given nodes.
     *
     * @param graph         The network graph.
     * @param sourceNode    The source node given by the user.
     * @param destNode      The destination node given by the user.
     * @return              It returns a list of the created paths.
     */
    private static List<GraphPath<Integer, DomainLink>> createPaths(NetworkGraph graph, Integer sourceNode, Integer destNode) {
        //find all possible paths between source and destination
        if (graph != null) {
            KShortestPaths kPaths = new KShortestPaths<>(graph, 2);
            List<GraphPath<Integer, DomainLink>> graphPaths = kPaths.getPaths(sourceNode, destNode);
            return graphPaths;
        } else {
            System.out.println("Graph was null.");
            return null;
        }
    }

    /**
     * The method which determines the input port for the ingress path node (source) and the output port for the egress
     * path node (destination).
     *
     * @param sourceNode    The source node given by the user.
     * @param destNode      The destination node given by the user.
     * @return
     */
    private static void findPortsForIngressAndEgressNodes(DomainNode sourceNode, DomainNode destNode){
        List<Link> graphLinks = NetworkGraph.getInstance().getGraphLinks();

        //examine all links of the graph and find the ones connecting a host to a switch
        for (Link graphLink : graphLinks){
            if (graphLink.getLinkId().getValue().contains("host")){
                if (graphLink.getLinkId().getValue().contains(destNode.getODLNodeID())){
                    String[] firstSplit = graphLink.getLinkId().getValue().split("\\/");
                    if (firstSplit[0].contains("openflow")) {
                        String[] secondSplit = firstSplit[0].split("openflow:");
                        outputPorts.put(destNode.getODLNodeID(), Integer.parseInt(secondSplit[1].split(":")[1]));
                    }
                    else if (firstSplit[1].contains("openflow")){
                        String[] secondSplit = firstSplit[1].split("openflow:");
                        outputPorts.put(destNode.getODLNodeID(), Integer.parseInt(secondSplit[1].split(":")[1]));
                    }
                }
                else if (graphLink.getLinkId().getValue().contains(sourceNode.getODLNodeID())){
                    String[] firstSplit = graphLink.getLinkId().getValue().split("\\/");
                    if (firstSplit[0].contains("openflow")) {
                        String[] secondSplit = firstSplit[0].split("openflow:");
                        inputPorts.put(sourceNode.getODLNodeID(), Integer.parseInt(secondSplit[1].split(":")[1]));
                    }
                    else if (firstSplit[1].contains("openflow")){
                        String[] secondSplit = firstSplit[1].split("openflow:");
                        inputPorts.put(sourceNode.getODLNodeID(), Integer.parseInt(secondSplit[1].split(":")[1]));
                    }
                }
            }
        }
    }

    /**
     * The method which determines the input and output ports for the core nodes of the main path, as well as the input port
     * for the egress (destination) switch and the output port for the ingress (source) switch.
     *
     * @param path          The path which will be analyzed to find its nodes' input, output and failover ports.
     * @param sourceNode    The source node given by the user.
     * @param destNode      The destination node given by the user.
     * @return
     */
    public static void findPorts(GraphPath<Integer, DomainLink> path, DomainNode sourceNode, DomainNode destNode) {
        //find in and out ports for nodes of main path
        for (DomainLink link : path.getEdgeList()) {
            inputPorts.put(link.getLink().getDestination().getDestNode().getValue(), Integer.parseInt(link.getLink().getDestination().getDestTp().getValue().split(":")[2]));
            outputPorts.put(link.getLink().getSource().getSourceNode().getValue(), Integer.parseInt(link.getLink().getSource().getSourceTp().getValue().split(":")[2]));
            failoverPorts.put(link.getLink().getDestination().getDestNode().getValue(), inputPorts.get(link.getLink().getDestination().getDestNode().getValue()));
        }
        //find in port for ingress node and out port for egress node, for main path
        if (!inputPorts.containsKey(sourceNode.getODLNodeID()) || !outputPorts.containsKey(destNode.getODLNodeID())) {
            findPortsForIngressAndEgressNodes(sourceNode, destNode);
        }
    }

    public static List<Link> getAllLinks(DataBroker db) {
        List<Link> linkList = new ArrayList<>();

        try {
            TopologyId topoId = new TopologyId("flow:1");
            InstanceIdentifier<Topology> nodesIid = InstanceIdentifier.builder(NetworkTopology.class).child(Topology.class, new TopologyKey(topoId)).toInstance();
            ReadOnlyTransaction nodesTransaction = db.newReadOnlyTransaction();
            CheckedFuture<com.google.common.base.Optional<Topology>, ReadFailedException> nodesFuture = nodesTransaction
                    .read(LogicalDatastoreType.OPERATIONAL, nodesIid);
            com.google.common.base.Optional<Topology> nodesOptional = nodesFuture.checkedGet();

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
        CheckedFuture<com.google.common.base.Optional<Nodes>, ReadFailedException> nodesFuture = nodesTransaction
                .read(LogicalDatastoreType.OPERATIONAL, nodesIid);
        com.google.common.base.Optional<Nodes> nodesOptional = nodesFuture.checkedGet();

        if (nodesOptional != null && nodesOptional.isPresent()) {
            nodeList = nodesOptional.get().getNode();
        }
        return nodeList;
    }
}
