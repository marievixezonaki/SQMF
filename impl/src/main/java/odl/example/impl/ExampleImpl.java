/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import com.google.common.util.concurrent.Futures;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlexample.rev150105.*;;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The core class of the implementation.
 *
 * @author Marievi Xezonaki
 */
public class ExampleImpl implements OdlexampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);
    private DijkstraShortestPath<NodeId, Link> shortestPath = null;
    private BindingAwareBroker.ProviderContext session;
    private static HashMap<String, Integer> inputPorts = new HashMap<>();
    private static HashMap<String, Integer> outputPorts = new HashMap<>();
    private static HashMap<String, Integer> failoverPorts = new HashMap<>();
    private static HashMap<String, Integer> inputPortsFailover = new HashMap<>();
    private static HashMap<String, Integer> outputPortsFailover = new HashMap<>();
    private static DataBroker db;
    public static String srcNode = null;
    public static String dstNode = null;

    public ExampleImpl(BindingAwareBroker.ProviderContext session, DataBroker db) {
        this.db = db;
        this.session = session;
    }

    /**
     * The method which starts monitoring the packet loss and delay of links, when the user asks it.
     */
    @Override
    public Future<RpcResult<Void>> startMonitoringLinks() {
        Timer time = new Timer();
        MonitorLinksTask monitorLinksTask = new MonitorLinksTask(db, "openflow:1:2", "openflow:8:2");
        time.schedule(monitorLinksTask, 0, 5000);

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
         //       switchConfigurator.configureFailoverPath(failoverPath.getEdgeList(), inputPortsFailover, outputPortsFailover);
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

}
