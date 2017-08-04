/*
 * Copyright © 2017 M.E. Xezonaki in the context of her MSc Thesis in the Department of
 * Informatics and Telecommunications, University of Athens.  All rights reserved.
 *
 */
package org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlexample.impl.rev141210;

import odl.example.impl.ExampleProvider;
import org.opendaylight.controller.sal.binding.api.NotificationProviderService;

public class ExampleModule extends org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlexample.impl.rev141210.AbstractExampleModule {
    public ExampleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver) {
        super(identifier, dependencyResolver);
    }

    public ExampleModule(org.opendaylight.controller.config.api.ModuleIdentifier identifier, org.opendaylight.controller.config.api.DependencyResolver dependencyResolver, org.opendaylight.yang.gen.v1.urn.opendaylight.params.xml.ns.yang.odlexample.impl.rev141210.ExampleModule oldModule, java.lang.AutoCloseable oldInstance) {
        super(identifier, dependencyResolver, oldModule, oldInstance);
    }

    @Override
    public void customValidation() {
        // add custom validation form module attributes here.
    }

    @Override
    public java.lang.AutoCloseable createInstance() {

        NotificationProviderService notificationService = getNotificationServiceDependency();

        ExampleProvider provider = new ExampleProvider(notificationService);
        getBrokerDependency().registerProvider(provider);
        return provider;
    }

}
