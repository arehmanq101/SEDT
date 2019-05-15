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
package org.eclipse.ditto.services.thingsearch.persistence.read.query;

import static org.eclipse.ditto.services.thingsearch.persistence.PersistenceConstants.DOT;
import static org.eclipse.ditto.services.thingsearch.persistence.PersistenceConstants.FIELD_SORTING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mutabilitydetector.unittesting.AllowedReason.provided;
import static org.mutabilitydetector.unittesting.MutabilityAssert.assertInstancesOf;
import static org.mutabilitydetector.unittesting.MutabilityMatchers.areImmutable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.eclipse.ditto.model.query.SortDirection;
import org.eclipse.ditto.model.query.SortOption;
import org.eclipse.ditto.model.query.criteria.Criteria;
import org.eclipse.ditto.model.query.expression.SimpleFieldExpressionImpl;
import org.eclipse.ditto.model.query.expression.SortFieldExpression;
import org.eclipse.ditto.model.query.expression.ThingsFieldExpressionFactory;
import org.eclipse.ditto.model.query.expression.ThingsFieldExpressionFactoryImpl;
import org.eclipse.ditto.services.base.DittoService;
import org.eclipse.ditto.services.base.config.limits.DefaultLimitsConfig;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mongodb.client.model.Sorts;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import nl.jqno.equalsverifier.EqualsVerifier;

/**
 * Tests {@link MongoQuery}.
 */
public final class MongoQueryTest {

    private static final Criteria KNOWN_CRIT = mock(Criteria.class);
    private static final ThingsFieldExpressionFactory EFT = new ThingsFieldExpressionFactoryImpl();

    private static int defaultPageSizeFromConfig;

    private List<SortOption> knownSortOptions;
    private Bson knownSortOptionsExpectedBson;

    @BeforeClass
    public static void initTestFixture() {
        final Config testConfig = ConfigFactory.load("test");
        final DefaultLimitsConfig limitsConfig =
                DefaultLimitsConfig.of(testConfig.getConfig(DittoService.DITTO_CONFIG_PATH));
        defaultPageSizeFromConfig = limitsConfig.getThingsSearchDefaultPageSize();
    }

    @Before
    public void before() {
        final SortFieldExpression sortExp1 = EFT.sortByThingId();
        final SortFieldExpression sortExp2 = EFT.sortByAttribute("test");
        knownSortOptions =
                Arrays.asList(new SortOption(sortExp1, SortDirection.ASC),
                        new SortOption(sortExp2, SortDirection.DESC));
        final String thingIdFieldName = ((SimpleFieldExpressionImpl) EFT.sortByThingId()).getFieldName();
        final String attributeFieldName = "attributes.test";
        knownSortOptionsExpectedBson = Sorts.orderBy(Arrays.asList(
                Sorts.ascending(thingIdFieldName),
                Sorts.descending(FIELD_SORTING + DOT + attributeFieldName)));
    }

    @Test
    public void hashcodeAndEquals() {
        EqualsVerifier.forClass(MongoQuery.class).verify();
    }

    @Test
    public void immutability() {
        assertInstancesOf(MongoQuery.class, areImmutable(),
                provided(Criteria.class, SortOption.class).isAlsoImmutable());
    }

    @Test
    public void criteriaIsCorrectlySet() {
        final MongoQuery query = new MongoQuery(KNOWN_CRIT, knownSortOptions, defaultPageSizeFromConfig,
                MongoQueryBuilder.DEFAULT_SKIP);

        assertThat(query.getCriteria()).isEqualTo(KNOWN_CRIT);
    }

    @Test
    public void emptySortOptions() {
        final MongoQuery query = new MongoQuery(KNOWN_CRIT, Collections.emptyList(), defaultPageSizeFromConfig,
                MongoQueryBuilder.DEFAULT_SKIP);

        assertThat(query.getCriteria()).isEqualTo(KNOWN_CRIT);
        assertThat(query.getSortOptions()).isEmpty();
        assertBson(new BsonDocument(), query.getSortOptionsAsBson());
    }

    @Test
    public void sortOptionsAreCorrectlySet() {
        final MongoQuery query = new MongoQuery(KNOWN_CRIT, knownSortOptions, defaultPageSizeFromConfig,
                MongoQueryBuilder.DEFAULT_SKIP);

        assertThat(query.getCriteria()).isEqualTo(KNOWN_CRIT);
        assertThat(query.getSortOptions()).isEqualTo(knownSortOptions);
        assertBson(knownSortOptionsExpectedBson, query.getSortOptionsAsBson());
    }

    private static void assertBson(final Bson expected, final Bson actual) {
        final BsonDocument expectedDoc =
                org.eclipse.ditto.services.utils.persistence.mongo.BsonUtil.toBsonDocument(expected);
        final BsonDocument actualDoc =
                org.eclipse.ditto.services.utils.persistence.mongo.BsonUtil.toBsonDocument(actual);

        assertThat(actualDoc).isEqualTo(expectedDoc);
    }

}
