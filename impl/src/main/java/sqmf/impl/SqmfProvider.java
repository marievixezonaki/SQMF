/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataBroker;
import org.opendaylight.controller.md.sal.common.api.data.LogicalDatastoreType;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker.ProviderContext;
import org.opendaylight.controller.sal.binding.api.BindingAwareProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.controller.sal.binding.api.RpcProviderRegistry;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.sqmf.rev141210.SqmfService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.NetworkTopology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.TopologyId;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.Topology;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.TopologyKey;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SqmfProvider implements BindingAwareProvider, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(SqmfProvider.class);
    private BindingAwareBroker.RpcRegistration<SqmfService> exampleService;
    private NotificationProviderService notificationService;
    private DataBroker db;
    private RpcProviderRegistry rpcProviderRegistry;

    public SqmfProvider(){
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
        exampleService = session.addRpcImplementation(SqmfService.class, new SqmfImplementation(session, db, rpcProviderRegistry, notificationService));
    }

    @Override
    public void close() throws Exception {
        LOG.info("ExampleProvider Closed");
    }

}
