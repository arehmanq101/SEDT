/*
 * Copyright (c) 2017-2018 Bosch Software Innovations GmbH.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/org/documents/epl-2.0/index.php
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.ditto.services.connectivity.util;

import java.util.Objects;

import javax.annotation.concurrent.Immutable;

import org.eclipse.ditto.services.utils.config.ConfigWithFallback;
import org.eclipse.ditto.services.utils.config.ScopedConfig;

import com.typesafe.config.Config;

/**
 * TODO
 */
@Immutable
public final class DefaultMqttConfig implements ConnectivityConfig.ConnectionConfig.MqttConfig {

    private static final String CONFIG_PATH = "mqtt";

    private final int sourceBufferSize;

    private DefaultMqttConfig(final ScopedConfig config) {
        sourceBufferSize = config.getInt(MqttConfigValue.SOURCE_BUFFER_SIZE.getConfigPath());
    }

    /**
     * TODO
     *
     * @param config
     * @return the instance.
     * @throws org.eclipse.ditto.services.utils.config.DittoConfigError if {@code config} is invalid.
     */
    public static DefaultMqttConfig of(final Config config) {
        return new DefaultMqttConfig(ConfigWithFallback.newInstance(config, CONFIG_PATH, MqttConfigValue.values()));
    }

    @Override
    public int getSourceBufferSize() {
        return sourceBufferSize;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final DefaultMqttConfig that = (DefaultMqttConfig) o;
        return Objects.equals(sourceBufferSize, that.sourceBufferSize);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceBufferSize);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" +
                "sourceBufferSize=" + sourceBufferSize +
                "]";
    }

}
