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
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
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
import java.util.concurrent.Future;

public class ExampleImpl implements OdlexampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);
    private DijkstraShortestPath<NodeId, Link> shortestPath = null;
    private BindingAwareBroker.ProviderContext session;
    private HashMap<String, Integer> inputPorts = new HashMap<>();
    private HashMap<String, Integer> outputPorts = new HashMap<>();
    private HashMap<String, Integer> failoverPorts = new HashMap<>();
    private HashMap<String, Integer> inputPortsFailover = new HashMap<>();
    private HashMap<String, Integer> outputPortsFailover = new HashMap<>();
    private DataBroker db;
    public boolean proactiveFF = false;
    public static boolean reactiveFF = false;
    public static String srcNode = null;
    public static String dstNode = null;
    public static GraphPath<Integer, DomainLink> path;

    public ExampleImpl(BindingAwareBroker.ProviderContext session, DataBroker db) {
        this.db = db;
        this.session = session;
    }

    @Override
    public Future<RpcResult<Void>> startFailover(StartFailoverInput input) {
        LOG.info("Configuring switches for resilience.");
        if (input != null && input.isProactiveFF() != null && input.isReactiveFF() != null) {
            if (input.isProactiveFF() && input.isReactiveFF()) {
                LOG.info("Both choices selected. Failover will be proactive.");
                proactiveFF = true;
            } else if (input.isProactiveFF()) {
                LOG.info("Failover will be proactive.");
                proactiveFF = true;
            } else if (input.isReactiveFF()) {
                LOG.info("Failover will be reactive.");
                reactiveFF = true;
            } else if (!input.isProactiveFF() && !input.isReactiveFF()) {
                LOG.info("Both choices selected. Failover will be proactive.");
                proactiveFF = true;
            }
        }

        if (input.getSrcNode() != null && input.getDstNode() != null) {
            if (proactiveFF && NetworkGraph.getInstance().getGraphNodes() != null && NetworkGraph.getInstance().getGraphLinks() != null) {
                Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
                DomainNode sourceNode = domainNodes.get(input.getSrcNode());
                DomainNode destNode = domainNodes.get(input.getDstNode());

                List<GraphPath<Integer, DomainLink>> possiblePaths = createAllPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
                if (possiblePaths.size() == 2){
                    GraphPath<Integer, DomainLink> mainPath = possiblePaths.get(0);
                    GraphPath<Integer, DomainLink> failoverPath = possiblePaths.get(1);

                    //find in and out ports for nodes of main path
                    for (DomainLink link : mainPath.getEdgeList()){
                        inputPorts.put(link.getLink().getDestination().getDestNode().getValue(), Integer.parseInt(link.getLink().getDestination().getDestTp().getValue().split(":")[2]));
                        outputPorts.put(link.getLink().getSource().getSourceNode().getValue(), Integer.parseInt(link.getLink().getSource().getSourceTp().getValue().split(":")[2]));
                        failoverPorts.put(link.getLink().getDestination().getDestNode().getValue(), inputPorts.get(link.getLink().getDestination().getDestNode().getValue()));

                    }
                    //find in port for ingress node and out port for egress node, for main path
                    if (!inputPorts.containsKey(sourceNode.getODLNodeID()) || !outputPorts.containsKey(destNode.getODLNodeID())) {
                        findPortsForIngressAndEgressNodes(sourceNode, destNode);
                    }

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

                    //have to print out an in port and an out port for all nodes of main path
          //          System.out.println("Inports " + inputPorts.toString());
         //           System.out.println("Outports " + outputPorts.toString());
          //          System.out.println("Failoverports " + failoverPorts.toString());

            //        System.out.println("Inports " + inputPortsFailover.toString());
            //        System.out.println("Outports " + outputPortsFailover.toString());

                    SwitchConfigurator switchConfigurator = new SwitchConfigurator(db);
                    switchConfigurator.configureIngress(sourceNode, inputPorts.get(sourceNode.getODLNodeID()), outputPorts.get(sourceNode.getODLNodeID()), failoverPorts.get(sourceNode.getODLNodeID()));
                    switchConfigurator.configureCoreAndEgress(mainPath.getEdgeList(), inputPorts, outputPorts, failoverPorts);
                    switchConfigurator.configureFailoverPath(failoverPath.getEdgeList(), inputPortsFailover, outputPortsFailover);
                }
            }
            else if (reactiveFF && NetworkGraph.getInstance().getGraphNodes() != null && NetworkGraph.getInstance().getGraphLinks() != null) {
                System.out.println("Welcome");
                Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
                DomainNode sourceNode = domainNodes.get(input.getSrcNode());
                DomainNode destNode = domainNodes.get(input.getDstNode());
                srcNode = input.getSrcNode();
                dstNode = input.getDstNode();
                List<GraphPath<Integer, DomainLink>> possiblePaths = createAllPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
                if (possiblePaths.size() > 0){
                    path = possiblePaths.get(0);
                }
            }
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());

    }


    private static List<GraphPath<Integer, DomainLink>> createAllPaths(NetworkGraph graph, Integer sourceNode, Integer destNode) {
        //find all possible paths between source and destination
        if (graph != null) {
            KShortestPaths kPaths = new KShortestPaths<>(graph, 2);
            List<GraphPath<Integer, DomainLink>> graphPaths = kPaths.getPaths(sourceNode, destNode);

            //      AllDirectedPaths allGraphPaths = new AllDirectedPaths(graph);
            //      List<GraphPath<Integer, DomainLink>> allPossiblePaths = allGraphPaths.getAllPaths(sourceNode, destNode, true, Integer.MAX_VALUE);
//            for (GraphPath<Integer, DomainLink> graphPath : graphPaths) {
//                System.out.println("Path: " + graphPath.getEdgeList());
//            }
            return graphPaths;
        } else {
            System.out.println("Graph was null.");
            return null;
        }
    }

    private void findPortsForIngressAndEgressNodes(DomainNode sourceNode, DomainNode destNode){
        List<Link> graphLinks = NetworkGraph.getInstance().getGraphLinks();

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

    public static void implementReactiveFailover(List<Link> linkList){

        System.out.println(NetworkGraph.getInstance().getGraphLinks().size() + " " + NetworkGraph.getInstance().edgeSet().size());
        Link linkDown = null;

        if (srcNode != null && dstNode != null) {
            boolean isLinkDown = false;
            for (DomainLink domainLink : path.getEdgeList()){
                for (Link link : linkList) {
                    if (domainLink.getLink().equals(link)){
                        System.out.println("The main path has link " + link.getLinkId().getValue() + " down");
                        isLinkDown = true;
                        linkDown = link;
                        break;
                    }
                }
            }
            if (!isLinkDown){
                System.out.println("The main path is not affected");
            }
            else if (linkDown != null){
                //find an alternative path from the source of the failed link to the destination
                Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
                List<GraphPath<Integer, DomainLink>> possiblePaths = createAllPaths(NetworkGraph.getInstance(), domainNodes.get(linkDown.getSource().getSourceNode().getValue()).getNodeID(), domainNodes.get(dstNode).getNodeID());
                List<GraphPath<Integer, DomainLink>> pathsToRemove = new ArrayList<>();
                System.out.println("Found " + possiblePaths.size() + " alternative paths" + possiblePaths.toString());
                for (GraphPath<Integer, DomainLink> possiblePath : possiblePaths){
                    for (DomainLink domainLink : possiblePath.getEdgeList()){
                        if (domainLink.getLink().equals(linkDown)){
                            pathsToRemove.add(possiblePath);
                            break;
                        }
                    }
                }
                possiblePaths.removeAll(pathsToRemove);
                System.out.println("Found " + possiblePaths.size() + " alternative paths" + possiblePaths.toString());
                if (possiblePaths.size() > 0){
                    GraphPath<Integer, DomainLink> reactivePath = possiblePaths.get(0);
                }
            }
        }
    }

}
