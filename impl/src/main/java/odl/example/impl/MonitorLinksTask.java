/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import java.util.TimerTask;

/**
 * The class implementing a task which monitors the topology's links.
 *
 * @author Marievi Xezonaki
 */
public class MonitorLinksTask extends TimerTask{

    private DataBroker db;
    private String pathInputPort, pathOutputPort;

    public MonitorLinksTask(DataBroker db, String pathInputPort, String pathOutputPort){
        this.db = db;
        this.pathInputPort = pathInputPort;
        this.pathOutputPort = pathOutputPort;
    }

    @Override
    public void run() {
        QoSOperations qoSOperations = new QoSOperations(db, "openflow:1:2", "openflow:8:2");
        qoSOperations.getAllLinksWithQos();
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

}

