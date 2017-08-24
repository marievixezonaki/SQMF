/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

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
     * Hashtable containing the structure of the domain graph nodes, <br>
     * relating the id of each node (vertex of a domain graph) to the node structure it represents. <br>
     */
    private Hashtable<String, DomainNode> domainNodes = new Hashtable<String, DomainNode>();

    public NetworkGraph(Class edgeClass) {
        super(edgeClass);
        graphID = ++graphIDCounter;
    }

    public static NetworkGraph getInstance() {
        if(networkGraph == null) {
            networkGraph = new NetworkGraph<Integer, DomainLink>(DomainLink.class);
        }
        return networkGraph;
    }

    public int getGraphID() {
        return graphID;
    }

    public List<Link> getGraphLinks(){ return graphLinks; }

    public void setGraphLinks(List<Link> links){ this.graphLinks = links; }

    public List<NodeId> getGraphNodes(){ return graphNodes; }

    public void setGraphNodes(List<NodeId> graphNodes){ this.graphNodes = graphNodes; }

    public Hashtable<String, DomainNode> getDomainNodes() {
        return domainNodes;
    }

    public void setDomainNodes(Hashtable<String, DomainNode> domainNodes) {
        this.domainNodes = domainNodes;
    }

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
