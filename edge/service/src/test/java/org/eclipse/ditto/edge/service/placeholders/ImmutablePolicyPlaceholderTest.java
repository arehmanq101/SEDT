/*
 * Copyright (c) 2020 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.edge.service.placeholders;

import static org.assertj.core.api.Assertions.assertThat;

import org.eclipse.ditto.policies.model.PolicyId;
import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

/**
 * Tests {@link ImmutablePolicyPlaceholder}.
 */
public class ImmutablePolicyPlaceholderTest {

    private static final String NAME = "ditto";
    private static final String NAMESPACE = "eclipse";
    private static final PolicyId POLICY_ID = PolicyId.of(NAMESPACE, NAME);
    private static final PolicyPlaceholder UNDER_TEST = ImmutablePolicyPlaceholder.INSTANCE;

    @Test
    public void testHashCodeAndEquals() {
        EqualsVerifier.forClass(ImmutablePolicyPlaceholder.class)
                .suppress(Warning.INHERITED_DIRECTLY_FROM_OBJECT)
                .usingGetClass()
                .verify();
    }

    @Test
    public void testReplacePolicyId() {
        assertThat(UNDER_TEST.resolveValues(POLICY_ID, "id")).contains(POLICY_ID.toString());
    }

    @Test
    public void testReplacePolicyName() {
        assertThat(UNDER_TEST.resolveValues(POLICY_ID, "name")).contains(NAME);
    }

    @Test
    public void testReplacePolicyNamespace() {
        assertThat(UNDER_TEST.resolveValues(POLICY_ID, "namespace")).contains(NAMESPACE);
    }

    @Test
    public void testUnknownPlaceholderReturnsEmpty() {
        assertThat(UNDER_TEST.resolveValues(POLICY_ID, "policy_id")).isEmpty();
    }

}
