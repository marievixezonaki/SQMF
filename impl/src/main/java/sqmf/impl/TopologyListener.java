/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis,
 * Department of Informatics and Telecommunications, UoA.
 * All rights reserved.
 */
package sqmf.impl;

import org.opendaylight.controller.md.sal.binding.api.DataBroker;
import org.opendaylight.controller.md.sal.binding.api.DataChangeListener;
import org.opendaylight.controller.md.sal.common.api.data.AsyncDataChangeEvent;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.binding.DataObject;
import org.opendaylight.yangtools.yang.binding.InstanceIdentifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The class listening to topology changes and updating the domain graph appropriately.
 *
 * @author Marievi Xezonaki
 */
public class TopologyListener implements DataChangeListener {

    private static final Logger LOG = LoggerFactory.getLogger(TopologyListener.class);
    private NotificationProviderService notificationService;
    private final DataBroker dataBroker;
    GraphOperations graphOperations = new GraphOperations();
    private boolean senderLinkDown = false;
    private boolean receiverLinkDown = false;


    /**
     * The constructor method.
     *
     * @param dataBroker                The data broker which gives access to MD-SAL.
     * @param notificationService       The notification service which listens for change notifications.
     */
    public TopologyListener(DataBroker dataBroker, NotificationProviderService notificationService) {
        this.dataBroker = dataBroker;
        this.notificationService = notificationService;
    }




    /**
     * The method which detects the topology changes.
     *
     * @param dataChangeEvent   The data changed by the event detected.
     */
    @Override
    public void onDataChanged(AsyncDataChangeEvent<InstanceIdentifier<?>, DataObject> dataChangeEvent) {

        List<Link> linkList;

        if (dataChangeEvent == null)
            return;

        Map<InstanceIdentifier<?>, DataObject> createdData = dataChangeEvent.getCreatedData();
        Set<InstanceIdentifier<?>> removedPaths = dataChangeEvent.getRemovedPaths();
        Map<InstanceIdentifier<?>, DataObject> originalData = dataChangeEvent.getOriginalData();
        if (createdData != null && !createdData.isEmpty()) {

            Set<InstanceIdentifier<?>> data = createdData.keySet();
            linkList = new ArrayList<>();
            for (InstanceIdentifier<?> dataElement : data) {
                if (Link.class.isAssignableFrom(dataElement.getTargetType())) {
                    Link link = (Link) createdData.get(dataElement);
                    if (!linkList.contains(link)) {
                        linkList.add(link);
                        System.out.println("Added link " + link.getKey().getLinkId().getValue() + " from " + link.getSource().getSourceNode().getValue() + " to " + link.getDestination().getDestNode().getValue());
                    }

                 /*   if (link.getLinkId().getValue().contains("host")){
                        if (link.getLinkId().getValue().contains(SqmfImplementation.srcNode)){
                            MonitorLinksTask.senderLinkDown = false;
                        }
                        else if (link.getLinkId().getValue().contains(SqmfImplementation.dstNode)){
                            MonitorLinksTask.receiverLinkDown = false;
                        }
                    }*/
                }
            }
            //update the domain graph with the added links
            graphOperations.updateGraph(NetworkGraph.getInstance(), linkList);

        }
        if (removedPaths != null && !removedPaths.isEmpty() && originalData != null && !originalData.isEmpty()) {
            linkList = new ArrayList<>();
            for (InstanceIdentifier<?> instanceId : removedPaths) {
                if (Link.class.isAssignableFrom(instanceId.getTargetType())) {
                    Link link = (Link) originalData.get(instanceId);
                    if (!linkList.contains(link)) {
                        linkList.add(link);
                        LOG.info("Removed link " + link.getKey().getLinkId().getValue() + " from " + link.getSource().getSourceNode().getValue() + " to " + link.getDestination().getDestNode().getValue());
                    }

                    if (link.getLinkId().getValue().contains("host")){
                        //either the link from the sender (--> no QoE) or the link from the receiver (100% loss) is down
             /*           if (link.getLinkId().getValue().contains(SqmfImplementation.srcNode)){
                            MonitorLinksTask.senderLinkDown = true;
                       //     System.out.println("No QoE can be computed because sender's link is down --> no traffic sent.");
                        }
                        else if (link.getLinkId().getValue().contains(SqmfImplementation.dstNode)){
                            MonitorLinksTask.receiverLinkDown = true;
                   //         System.out.println("100% packet loss even if QoE in path > threshold.");
                        }*/
                    }
                    else {
                        for (DomainLink dl : SqmfImplementation.mainGraphWalk.getEdgeList()){
                            if (dl.getLink().getLinkId().getValue().equals(link.getLinkId().getValue())){
                                //main path contains the link which failed
                                MonitorLinksTask.linkFailure = true;
                            }
                        }
                    }
                }
            }
            //remove the removed links from the domain graph
            graphOperations.removeFromGraph(NetworkGraph.getInstance(), linkList);
        }
    }

}

