/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.jgrapht.graph.DefaultWeightedEdge;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

/**
 * The class modelling a link of the domain.
 *
 * @author Marievi Xezonaki
 */
public class DomainLink extends DefaultWeightedEdge {

    private String linkID;
    private Link link;


    /**
     * The method which returns the destination node of the link.
     *
     * @return The destination node of the link.
     */
    public Object getTarget() {
        return super.getTarget();
    }



    /**
     * The method which returns the source node of the link.
     *
     * @return The source node of the link.
     */
    public Object getSource() {
        return super.getSource();
    }

    public boolean equals(Object o) {
        return ((DomainLink) o).getSource().equals(getSource()) && ((DomainLink) o).getTarget().equals(getTarget());
    }


    /**
     * The method which returns the ID of the link.
     *
     * @return The ID of the link.
     */
    public String getLinkID() {
        return linkID;
    }


    /**
     * The method which sets the ID of the link.
     *
     * @param linkID    The ID to be set to the link.
     */
    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }


    /**
     * The method which returns the link.
     *
     * @return The link.
     */
    public Link getLink() {
        return link;
    }


    /**
     * The method which sets the link.
     *
     * @param link  The link to be set.
     */
    public void setLink(Link link) {
        this.link = link;
    }

}
