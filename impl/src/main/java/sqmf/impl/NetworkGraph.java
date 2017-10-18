/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NodeId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import java.util.Hashtable;
import java.util.List;

/**
 * The class modelling the domain.
 *
 * @author Marievi Xezonaki
 */
public class NetworkGraph<T, E> extends SimpleDirectedWeightedGraph<T, E> {
    public static int graphIDCounter = 0;
    private int graphID = -1;
    private static NetworkGraph networkGraph = null;
    private List<Link> graphLinks = null;
    private List<NodeId> graphNodes = null;

    /**
     * Hashtable containing the structure of the domain graph nodes,
     * relating the id of each node (vertex of a domain graph) to the node structure it represents.
     */
    private Hashtable<String, DomainNode> domainNodes = new Hashtable<String, DomainNode>();

    /**
     * The constructor method.
     */
    public NetworkGraph(Class edgeClass) {
        super(edgeClass);
        graphID = ++graphIDCounter;
    }



    /**
     * The method which returns the domain graph's instance.
     *
     * @return The domain graph instance.
     */
    public static NetworkGraph getInstance() {
        if(networkGraph == null) {
            networkGraph = new NetworkGraph<Integer, DomainLink>(DomainLink.class);
        }
        return networkGraph;
    }



    /**
     * The method which returns the ID of the graph.
     *
     * @return The ID of the graph.
     */
    public int getGraphID() {
        return graphID;
    }



    /**
     * The method which returns the links of the graph.
     *
     * @return The links' list of the graph.
     */
    public List<Link> getGraphLinks(){ return graphLinks; }



    /**
     * The method which sets the links of the graph.
     *
     * @param links  The links to be set to the graph.
     */
    public void setGraphLinks(List<Link> links){ this.graphLinks = links; }



    /**
     * The method which returns the nodes of the graph.
     *
     * @return The nodes of the graph.
     */
    public List<NodeId> getGraphNodes(){ return graphNodes; }



    /**
     * The method which sets the nodes of the graph.
     *
     * @param graphNodes  The nodes to be set to the graph.
     */
    public void setGraphNodes(List<NodeId> graphNodes){ this.graphNodes = graphNodes; }



    /**
     * The method which returns the domainNodes hashtable.
     *
     * @return The domainNodes hashtable.
     */
    public Hashtable<String, DomainNode> getDomainNodes() {
        return domainNodes;
    }



    /**
     * The method which sets the domainNodes hashtable.
     *
     * @param domainNodes  The domainNodes hashtable.
     */
    public void setDomainNodes(Hashtable<String, DomainNode> domainNodes) {
        this.domainNodes = domainNodes;
    }



    /**
     * The method which adds a node to the domain graph.
     *
     * @param id        The id of the new node.
     * @param newNode   The new node to add.
     *
     * @return          The created new node.
     */
    public DomainNode addNode(String id, DomainNode newNode) {

        if (newNode.getNodeID() == -1) newNode.setNodeID(DomainNode.getNodeIDCnt());

        newNode.setParentGraphID(graphID);
        if (addVertex((T) new Integer(newNode.getNodeID()))) {
            domainNodes.put(id, newNode);
            return newNode;

        } else {
            return domainNodes.get(newNode.getNodeID());
        }
    }
}
