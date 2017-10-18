/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

/**
 * The class modelling a node of the domain.
 *
 * @author Marievi Xezonaki
 */
public class DomainNode {
    private int nodeID = -1;
    private int parentGraphID = -1;
    private String ODLNodeID;

    private static int nodeIDCnt = 0;

    /**
     * The method which returns the number of inserted nodes.
     *
     * @return The number of inserted nodes.
     */
    public static int getNodeIDCnt(){
        return ++nodeIDCnt;
    }


    /**
     * The method which returns the ID of the node.
     *
     * @return The ID of the node.
     */
    public int getNodeID() {
        return nodeID;
    }


    /**
     * The method which sets the ID of the node.
     *
     * @param nodeID    The ID to be set to the node.
     */
    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }


    /**
     * The method which returns the ODL ID of the node.
     *
     * @return The ODL ID of the node.
     */
    public String getODLNodeID() {
        return ODLNodeID;
    }


    /**
     * The method which sets the ODL ID of the node.
     *
     * @param ODLNodeID    The ODL ID to be set to the node.
     */
    public void setODLNodeID(String ODLNodeID) {
        this.ODLNodeID = ODLNodeID;
    }



    /**
     * The method which sets the ID of the graph where the node belongs.
     *
     * @param parentGraphID    The ID of the graph where the node belongs.
     */
    public void setParentGraphID(int parentGraphID) {
        this.parentGraphID = parentGraphID;
    }



    /**
     * The method which returns ID of the graph where the node belongs.
     *
     * @return  parentGraphID    The ID of the graph where the node belongs.
     */
    public int getParentGraphID() {
        return parentGraphID;
    }
}
