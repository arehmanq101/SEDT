/*
 * Copyright (c) 2017 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.things.persistence.actors;

import org.eclipse.ditto.services.utils.persistence.mongo.config.MongoDbConfig;
import org.eclipse.ditto.services.utils.persistence.mongo.namespace.AbstractEventSourceNamespaceOpsActor;
import org.eclipse.ditto.signals.commands.things.ThingCommand;
import org.eclipse.ditto.utils.jsr305.annotations.AllValuesAreNonnullByDefault;

import com.typesafe.config.Config;

import akka.actor.ActorRef;
import akka.actor.Props;

/**
 * Namespace operations for the event-sourcing persistence of things.
 */
@AllValuesAreNonnullByDefault
public final class ThingNamespaceOpsActor extends AbstractEventSourceNamespaceOpsActor {

    public static final String ACTOR_NAME = "thingNamespaceOps";

    private ThingNamespaceOpsActor(final ActorRef pubSubMediator, final Config config,
            final MongoDbConfig mongoDbConfig) {

        super(pubSubMediator, config, mongoDbConfig);
    }

    /**
     * Create Props of this actor.
     *
     * @param pubSubMediator Akka pub-sub mediator.
     * @param config Configuration with info about event journal, snapshot store, suffix-builder and database.
     * @param mongoDbConfig the configuration settings for MongoDB.
     * @return a Props object.
     */
    public static Props props(final ActorRef pubSubMediator, final Config config, final MongoDbConfig mongoDbConfig) {
        return Props.create(ThingNamespaceOpsActor.class,
                () -> new ThingNamespaceOpsActor(pubSubMediator, config, mongoDbConfig));
    }

    @Override
    protected String getPersistenceIdPrefix() {
        return ThingPersistenceActor.PERSISTENCE_ID_PREFIX;
    }

    @Override
    protected String getJournalPluginId() {
        return ThingPersistenceActor.JOURNAL_PLUGIN_ID;
    }

    @Override
    protected String getSnapshotPluginId() {
        return ThingPersistenceActor.SNAPSHOT_PLUGIN_ID;
    }

    @Override
    protected String getResourceType() {
        return ThingCommand.RESOURCE_TYPE;
    }

}
