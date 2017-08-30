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
import java.util.Date;

/**
 * Created by maxez on 30/8/2017.
 */
public class MonitorLinksTask extends TimerTask{

    private DataBroker db;

    public MonitorLinksTask(DataBroker db){
        this.db = db;
    }

    @Override
    public void run() {
        QoSOperations qoSOperations = new QoSOperations(db);
        qoSOperations.getAllLinksWithQos();
        System.out.println("-----------------------------------------------------------------------------------------------------");
    }

}

