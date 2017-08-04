/*
 * Copyright © 2017 M.E Xezonaki in the context of her MSc Thesis, Department of Informatics and Telecommunications, UoA.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package odl.example.impl;

import com.google.common.util.concurrent.Futures;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AllDirectedPaths;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.opendaylight.controller.sal.binding.api.BindingAwareBroker;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.*;
import org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlexample.rev150105.*;;
import org.opendaylight.yang.gen.v1.urn.tbd.params.xml.ns.yang.network.topology.rev131021.network.topology.topology.Link;
import org.opendaylight.yangtools.yang.common.RpcResult;
import org.opendaylight.yangtools.yang.common.RpcResultBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.*;
import java.util.concurrent.Future;

public class ExampleImpl implements OdlexampleService {

    private static final Logger LOG = LoggerFactory.getLogger(ExampleProvider.class);
    private DijkstraShortestPath<NodeId, Link> shortestPath = null;
    private BindingAwareBroker.ProviderContext session;

    public ExampleImpl(BindingAwareBroker.ProviderContext session) {
        this.session = session;
    }

    @Override
    public Future<RpcResult<Void>> startFailover(StartFailoverInput input) {
        LOG.info("Configuring switches for resilience.");
        if (input != null) {
            if (input.isProactiveFF() && input.isReactiveFF()) {
                LOG.info("Both choices selected. Failover will be proactive.");
            } else if (input.isProactiveFF()) {
                LOG.info("Failover will be proactive.");
            } else if (input.isReactiveFF()) {
                LOG.info("Failover will be reactive.");

            } else if (!input.isProactiveFF() && !input.isReactiveFF()) {
                LOG.info("Both choices selected. Failover will be proactive.");
            }
        }

        if (input.getSrcNode() != null && input.getDstNode() != null) {
            if (NetworkGraph.getInstance().getGraphNodes() != null /*&& NetworkGraph.getInstance().getGraphLinks() != null*/) {
                Hashtable<String, DomainNode> domainNodes = NetworkGraph.getInstance().getDomainNodes();
                DomainNode sourceNode = domainNodes.get(input.getSrcNode());
                DomainNode destNode = domainNodes.get(input.getDstNode());

                List<GraphPath<Integer, DomainLink>> possiblePaths = createAllPaths(NetworkGraph.getInstance(), sourceNode.getNodeID(), destNode.getNodeID());
                if (possiblePaths.size() == 2){
                    GraphPath<Integer, DomainLink> mainPath = possiblePaths.get(0);
                    GraphPath<Integer, DomainLink> failoverPath = possiblePaths.get(1);

                    SwitchConfigurator switchConfigurator = new SwitchConfigurator();
                    switchConfigurator.configureSwitches(sourceNode, input.getSrcMAC(), mainPath.getEdgeList(), failoverPath.getEdgeList());
                }
            }
        }
        return Futures.immediateFuture(RpcResultBuilder.<Void>success().build());

    }


    private List<GraphPath<Integer, DomainLink>> createAllPaths(NetworkGraph graph, Integer sourceNode, Integer destNode) {
        //find all possible paths between source and destination
        if (graph != null) {
            KShortestPaths kPaths = new KShortestPaths<>(graph, 2);
            List<GraphPath<Integer, DomainLink>> graphPaths = kPaths.getPaths(sourceNode, destNode);

            //      AllDirectedPaths allGraphPaths = new AllDirectedPaths(graph);
            //      List<GraphPath<Integer, DomainLink>> allPossiblePaths = allGraphPaths.getAllPaths(sourceNode, destNode, true, Integer.MAX_VALUE);
//            for (GraphPath<Integer, DomainLink> graphPath : graphPaths) {
//                System.out.println("Path: " + graphPath.getEdgeList());
//            }
            return graphPaths;
        } else {
            System.out.println("Graph was null.");
            return null;
        }
    }
}
