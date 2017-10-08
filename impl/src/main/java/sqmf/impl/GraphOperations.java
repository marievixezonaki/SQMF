/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package sqmf.impl;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * The class which handles the domain graph.
 *
 * @author Marievi Xezonaki
 */
public class GraphOperations {

    /**
     * The method which adds updates the network graph with new links and nodes, when a change in the topology is detected.
     *
     * @param graph         The graph which links and nodes will be added to.
     * @param linkList      The links to be added to the graph, which the nodes will occur from.
     * @return              The updated graph.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TopologyListener.class);

    public NetworkGraph updateGraph(NetworkGraph graph, List<Link> linkList){

        if (graph == null) {
            LOG.info("Null graph, cannot update anything.");
            return null;
        }

        if (linkList != null) {
            for (Link link : linkList) {
                NodeId source = link.getSource().getSourceNode();
                NodeId dest = link.getDestination().getDestNode();
                DomainNode sourceNode = null, destNode = null;

                //add the domain nodes
                sourceNode = addNode(graph, source);
                destNode = addNode(graph, dest);
                if (graph.getGraphNodes() != null){
                    if (!graph.getGraphNodes().contains(source)){
                        graph.getGraphNodes().add(source);
                        System.out.println("added graph node with id " + sourceNode.getODLNodeID());
                    }
                    if (!graph.getGraphNodes().contains(dest)){
                        graph.getGraphNodes().add(dest);
                        System.out.println("added graph node with id " + destNode.getODLNodeID());
                    }
                }
                // if graph's node list is not initialized, initialize it and then add the source and destination nodes
                else{
                    List<NodeId> graphNodes = new ArrayList<>();
                    graph.setGraphNodes(graphNodes);
                    graph.getGraphNodes().add(source);
                    graph.getGraphNodes().add(dest);
                    System.out.println("added graph node with id " + sourceNode.getODLNodeID());
                    System.out.println("added graph node with id " + destNode.getODLNodeID());
                }
                //add the domain links
                DomainLink domainLink = (DomainLink) graph.addEdge(sourceNode.getNodeID(), destNode.getNodeID());
                if (domainLink != null) {
                    domainLink.setLinkID(link.getLinkId().getValue());
                    domainLink.setLink(link);
                }
                if (graph.getGraphLinks()!= null){
                    if (!graph.getGraphLinks().contains(link)) {
                        graph.getGraphLinks().add(link);
                    }
                }
                else{
                    List<Link> graphLinks = new ArrayList<>();
                    graph.setGraphLinks(graphLinks);
                    graph.getGraphLinks().add(link);
                }
            }
        }
        return graph;
    }

    /**
     * The method which adds a new node to the network graph.
     *
     * @param graph     The graph which a node will be added to.
     * @param node      The OF id of the node to be created.
     * @return          The created new node.
     */
    public DomainNode addNode(NetworkGraph graph, NodeId node){

        DomainNode domainNode;
        if (!graph.getDomainNodes().containsKey(node.getValue())) {
            domainNode = new DomainNode();
            domainNode.setODLNodeID(node.getValue());
            graph.addNode(node.getValue(), domainNode);
        } else {
            domainNode = (DomainNode) graph.getDomainNodes().get(node.getValue());
        }
        return domainNode;

    }

    /**
     * The method which removes a list of links from the network graph.
     *
     * @param graph     The graph which a node will be added to.
     * @param linkList  The links to be removed from the graph.
     * @return          The graph after the removal.
     */
    public NetworkGraph removeFromGraph(NetworkGraph graph, List<Link> linkList){

        if (graph == null) {
            System.out.println("Null graph, cannot remove anything.");
            return null;
        }
        if (linkList != null) {
            for (Link link : linkList) {
                // removing only links, nodes will be kept in the graph
                NodeId source = link.getSource().getSourceNode();
                NodeId dest = link.getDestination().getDestNode();
                DomainNode sourceNode = (DomainNode) graph.getDomainNodes().get(source.getValue());
                DomainNode destNode = (DomainNode) graph.getDomainNodes().get(dest.getValue());

                LOG.info("Removing link from edge " + sourceNode.getNodeID() + " to " + destNode.getNodeID());
                graph.removeEdge(sourceNode.getNodeID(), destNode.getNodeID());


                if (graph.getGraphLinks()!= null){
                    if (graph.getGraphLinks().contains(link)) {
                        graph.getGraphLinks().remove(link);
                    }
                }
            }
        }
        return graph;
    }
}
