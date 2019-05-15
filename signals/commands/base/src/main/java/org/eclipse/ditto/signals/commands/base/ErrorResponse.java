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
package org.eclipse.ditto.signals.commands.base;

import org.eclipse.ditto.model.base.exceptions.DittoRuntimeException;
import org.eclipse.ditto.model.base.headers.DittoHeaders;

/**
 * Aggregates all error responses relating to a given {@link Command}.
 *
 * @param <T> the type of the implementing class.
 */
public interface ErrorResponse<T extends ErrorResponse> extends CommandResponse<T> {

    @Override
    T setDittoHeaders(DittoHeaders dittoHeaders);

    /**
     * Returns the wrapped {@link DittoRuntimeException}.
     *
     * @return the wrapped exception.
     */
    DittoRuntimeException getDittoRuntimeException();
}
