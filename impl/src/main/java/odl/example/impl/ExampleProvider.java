/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.*;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.impl.rev141210.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev150105.SqmfService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ExampleProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);
    private BindingAwareBroker.RpcRegistration<SqmfService> exampleService;
    private NotificationProviderService notificationService;
    private DataBroker db;
    private RpcProviderRegistry rpcProviderRegistry;

    public ExampleProvider(){
        this.notificationService = notificationService;
    //    this.rpcProviderRegistry = rpcProviderRegistry;
        this.db = db;
    }

    @Override
    public void onSessionInitiated(ProviderContext session) {

        rpcProviderRegistry = session.getSALService(RpcProviderRegistry.class);
        notificationService = session.getSALService(NotificationProviderService.class);

        LOG.info("ExampleProvider Session Initiated");
        DataBroker db = session.getSALService(DataBroker.class);
        //Setting the Topology Listener for catching topology changes and updating the network graph
        InstanceIdentifier<Link> linkInstance = InstanceIdentifier.builder(NetworkTopology.class)
                .child(Topology.class, new TopologyKey(new TopologyId("flow:1"))).child(Link.class).build();
        db.registerDataChangeListener(LogicalDatastoreType.OPERATIONAL, linkInstance,
                new TopologyListener(db, notificationService),
                AsyncDataBroker.DataChangeScope.BASE);
        LOG.info("Topology Listener set");
  /*      PacketProcessing packetProcessingListener = new PacketProcessing();

   //     this.notificationService.registerNotificationListener(packetProcessingListener);
        if (notificationService != null) {
            notificationService.registerNotificationListener(packetProcessingListener);
            System.out.println("Registered packet processing listener");
        }
        else{
            System.out.println("null");
        }
*/
        //starting the ExampleImpl class
        exampleService = session.addRpcImplementation(SqmfService.class, new ExampleImpl(session, db, rpcProviderRegistry, notificationService));
    }

    @Override
    public void close() throws Exception {
        LOG.info("ExampleProvider Closed");
    }

}
