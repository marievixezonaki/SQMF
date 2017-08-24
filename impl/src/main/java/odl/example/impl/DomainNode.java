/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

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
    public static int getNodeIDCnt(){
        return ++nodeIDCnt;
    }

    public int getNodeID() {
        return nodeID;
    }

    public void setNodeID(int nodeID) {
        this.nodeID = nodeID;
    }

    public String getODLNodeID() {
        return ODLNodeID;
    }

    public void setODLNodeID(String ODLNodeID) {
        this.ODLNodeID = ODLNodeID;
    }

    public void setParentGraphID(int parentGraphID) {
        this.parentGraphID = parentGraphID;
    }

    public int getParentGraphID() {
        return parentGraphID;
    }
}
