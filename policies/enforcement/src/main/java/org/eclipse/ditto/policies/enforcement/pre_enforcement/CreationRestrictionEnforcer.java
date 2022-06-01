/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
package org.eclipse.ditto.policies.enforcement.pre_enforcement;

import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionStage;
import java.util.regex.Pattern;

import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.base.model.entity.id.EntityId;
import org.eclipse.ditto.base.model.entity.id.NamespacedEntityId;
import org.eclipse.ditto.base.model.entity.id.WithEntityId;
import org.eclipse.ditto.base.model.exceptions.DittoInternalErrorException;
import org.eclipse.ditto.base.model.exceptions.EntityNotCreatableException;
import org.eclipse.ditto.base.model.headers.DittoHeaders;
import org.eclipse.ditto.base.model.headers.DittoHeadersSettable;
import org.eclipse.ditto.base.model.headers.WithDittoHeaders;
import org.eclipse.ditto.base.model.signals.Signal;
import org.eclipse.ditto.internal.utils.akka.logging.DittoLoggerFactory;
import org.eclipse.ditto.internal.utils.akka.logging.ThreadSafeDittoLogger;
import org.eclipse.ditto.internal.utils.config.DefaultScopedConfig;
import org.eclipse.ditto.policies.enforcement.config.CreationRestrictionConfig;
import org.eclipse.ditto.policies.enforcement.config.DefaultEntityCreationConfig;
import org.eclipse.ditto.policies.enforcement.config.EntityCreationConfig;

import akka.actor.ActorSystem;

/**
 * PreEnforcer for evaluating if the entity creation should be restricted.
 */
@Immutable
public class CreationRestrictionEnforcer implements PreEnforcer {

    private static final ThreadSafeDittoLogger log = DittoLoggerFactory.getThreadSafeLogger(CreationRestrictionEnforcer.class);

    private final EntityCreationConfig config;
    private final ExistenceChecker existenceChecker;

    public CreationRestrictionEnforcer(final ActorSystem actorSystem) {
        config = DefaultEntityCreationConfig.of(DefaultScopedConfig.dittoScoped(actorSystem.settings().config()));
        existenceChecker = ExistenceChecker.get(actorSystem);
    }

    boolean canCreate(final Context context) {
        return matchesList(this.config.getGrant(), context)
                && !matchesList(this.config.getRevoke(), context);
    }

    private boolean matchesList(final List<CreationRestrictionConfig> list, final Context context) {
        return list.stream().anyMatch(entry -> matches(entry, context));
    }

    private static boolean matches(final CreationRestrictionConfig entry, final Context context) {

        return matchesResourceType(entry.getResourceTypes(), context)
                && matchesAuthSubjectPattern(entry.getAuthSubject(), context)
                && matchesNamespacePattern(entry.getNamespace(), context);

    }

    private static boolean matchesResourceType(final Set<String> resourceTypes, final Context context) {
        if (resourceTypes.isEmpty()) {
            // no restriction -> pass
            log.withCorrelationId(context.headers()).debug("No resource type restriction: pass");
            return true;
        }

        if (resourceTypes.contains(context.resourceType())) {
            return true;
        }

        log.withCorrelationId(context.headers()).debug("No resource type match: reject");
        // no match, but non-empty list -> reject
        return false;
    }

    private static boolean matchesAuthSubjectPattern(final List<Pattern> authSubjectPatterns, final Context context) {
        if (authSubjectPatterns.isEmpty()) {
            // no restrictions -> pass
            log.withCorrelationId(context.headers()).debug("No auth subject restriction: pass");
            return true;
        }

        for (final var subject : context.headers().getAuthorizationContext().getAuthorizationSubjectIds()) {
            for (final var authSubjectPattern : authSubjectPatterns) {
                if (authSubjectPattern.matcher(subject).matches()) {
                    log.withCorrelationId(context.headers()).debug("Matched auth subject {}: pass", subject);
                    // entry found -> pass
                    return true;
                }
            }
        }

        log.withCorrelationId(context.headers()).debug("No auth subject match: reject");
        // no match, but non-empty list -> reject
        return false;
    }

    private static boolean matchesNamespacePattern(final List<Pattern> namespacePatterns, final Context context) {

        if (namespacePatterns.isEmpty()) {
            log.withCorrelationId(context.headers()).debug("No namespace restriction: pass");
            // no restrictions -> pass
            return true;
        }

        var namespace = context.namespace();
        for (final var namespacePattern : namespacePatterns) {
            if (namespacePattern.matcher(namespace).matches()) {
                log.withCorrelationId(context.headers()).debug("Namespace '{}' matched {}: pass", namespace,
                        namespacePattern);
                return true;
            }
        }

        log.withCorrelationId(context.headers()).debug("No namespace match: reject");
        // no match, but non-empty list -> reject
        return false;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "config=" + config +
                ']';
    }

    @Override
    public CompletionStage<DittoHeadersSettable<?>> apply(final DittoHeadersSettable<?> dittoHeadersSettable) {
        final Signal<?> messageAsSignal = getMessageAsSignal(dittoHeadersSettable);
        final WithEntityId withEntityId = getMessageAsWithEntityId(dittoHeadersSettable);
        final NamespacedEntityId entityId = getEntityIdAsNamespacedEntityId(withEntityId.getEntityId());
        final var context = new Context(messageAsSignal.getResourceType(), entityId.getNamespace(), messageAsSignal.getDittoHeaders());
        return existenceChecker.checkExistence(messageAsSignal).thenApply(exists -> {
            if (Boolean.TRUE.equals(exists) || canCreate(context)) {
                return dittoHeadersSettable;
            } else {
                throw EntityNotCreatableException.newBuilder(withEntityId.getEntityId())
                        .dittoHeaders(messageAsSignal.getDittoHeaders())
                        .build();
            }
        });
    }

    private static WithEntityId getMessageAsWithEntityId(@Nullable final WithDittoHeaders message) {
        if (message instanceof WithEntityId) {
            return (WithEntityId) message;
        }
        if (null == message) {
            // just in case
            LOGGER.error("Given message is null!");
            throw DittoInternalErrorException.newBuilder().build();
        }
        final String msgPattern = "Message of type <{0}> does not implement WithEntityId!";
        throw new IllegalArgumentException(MessageFormat.format(msgPattern, message.getClass()));
    }

    private static NamespacedEntityId getEntityIdAsNamespacedEntityId(@Nullable final EntityId entityId) {
        if (entityId instanceof NamespacedEntityId) {
            return (NamespacedEntityId) entityId;
        }
        if (null == entityId) {
            // just in case
            LOGGER.error("Given entityId is null!");
            throw DittoInternalErrorException.newBuilder().build();
        }
        final String msgPattern = "Entity of type <{0}> is not namespaced!";
        throw new IllegalArgumentException(MessageFormat.format(msgPattern, entityId.getClass()));
    }

    /**
     * The context for evaluating if an entity may be created or not.
     */
    record Context(String resourceType, String namespace, DittoHeaders headers) {}

}
