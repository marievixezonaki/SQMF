/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

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

    public Object getTarget() {
        return super.getTarget();
    }

    public Object getSource() {
        return super.getSource();
    }

    public boolean equals(Object o) {
        return ((DomainLink) o).getSource().equals(getSource()) && ((DomainLink) o).getTarget().equals(getTarget());
    }

    public String getLinkID() {
        return linkID;
    }

    public void setLinkID(String linkID) {
        this.linkID = linkID;
    }

    public Link getLink() {
        return link;
    }

    public void setLink(Link link) {
        this.link = link;
    }

}
