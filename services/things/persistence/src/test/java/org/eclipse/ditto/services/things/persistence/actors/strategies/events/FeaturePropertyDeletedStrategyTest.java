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
package org.eclipse.ditto.services.things.persistence.actors.strategies.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import org.eclipse.ditto.model.base.headers.DittoHeaders;
import org.eclipse.ditto.model.things.FeatureProperties;
import org.eclipse.ditto.model.things.Thing;
import org.eclipse.ditto.signals.events.things.FeaturePropertyDeleted;
import org.junit.Test;

/**
 * Unit test for {@link FeaturePropertyDeletedStrategy}.
 */
public final class FeaturePropertyDeletedStrategyTest extends AbstractStrategyTest {

    @Test
    public void assertImmutability() {
        assertInstancesOf(FeaturePropertyDeletedStrategy.class, areImmutable());
    }

    @Test
    public void appliesEventCorrectly() {
        final FeaturePropertyDeletedStrategy strategy = new FeaturePropertyDeletedStrategy();
        final FeaturePropertyDeleted event = FeaturePropertyDeleted.of(THING_ID, FEATURE_ID, FEATURE_PROPERTY_POINTER,
                REVISION, DittoHeaders.empty());

        final Thing thingWithFeatureWithProperty = THING.toBuilder()
                .setFeature(FEATURE.toBuilder()
                        .properties(FeatureProperties.newBuilder()
                                .set(FEATURE_PROPERTY_POINTER, FEATURE_PROPERTY_VALUE)
                                .build())
                        .build())
                .build();
        final Thing thingWithEventApplied = strategy.handle(event, thingWithFeatureWithProperty,
                NEXT_REVISION);

        final Thing expected = THING.toBuilder()
                .setFeatureProperties(FEATURE_ID, FeatureProperties.newBuilder().build())
                .setRevision(NEXT_REVISION)
                .build();
        assertThat(thingWithEventApplied).isEqualTo(expected);
    }

}
