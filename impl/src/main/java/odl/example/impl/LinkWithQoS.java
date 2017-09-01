/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;

/**
 * The class which emulates a link with its QoS characteristics.
 *
 * @author Marievi Xezonaki
 */
public class LinkWithQoS {

    private String linkId;
    private Long packetLoss;
    private Long packetDelay;
    private Link link;

    public LinkWithQoS(Long packetLoss, Long packetDelay, Link link) {
        this.linkId = link.getLinkId().getValue();;
        this.packetLoss = packetLoss;
        this.packetDelay = packetDelay;
        this.link = link;
    }

    public Long getPacketLoss(){
        return this.packetLoss;
    }

    public Long getPacketDelay(){
        return this.packetDelay;
    }

    public void setLink(Link link) {
        this.link = link;
    }

    public Link getLink() {
        return link;
    }
}
