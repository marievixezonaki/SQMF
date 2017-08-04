/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class SwitchConfigurator {

    private HashMap<String, Integer> inputPorts = new HashMap<>();

    public void configureSwitches(DomainNode sourceNode, String srcMAC, List<DomainLink> mainPathLinks, List<DomainLink> failoverPathLinks){

        for (DomainLink link : mainPathLinks){
            inputPorts.put(link.getLink().getDestination().getDestNode().getValue(), Integer.parseInt(link.getLink().getDestination().getDestTp().getValue().split(":")[2]));

            String source = link.getLink().getSource().getSourceNode().getValue();
            String dest = link.getLink().getDestination().getDestNode().getValue();
            System.out.println("Source is " + source + " and dest is " + dest);
        }


    }
}
