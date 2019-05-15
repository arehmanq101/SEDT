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
package org.eclipse.ditto.json;

import java.net.URI;
import java.text.MessageFormat;

import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * Thrown if a {@link JsonPointer} was in an invalid format.
 */
public final class JsonPointerInvalidException extends JsonRuntimeException {

    /**
     * Error code of this exception.
     */
    public static final String ERROR_CODE = "json.pointer.invalid";

    private static final String DEFAULT_DESCRIPTION = "Check, for example, if the JSON Pointer is not empty.";

    private static final long serialVersionUID = -6773700329225961931L;

    private JsonPointerInvalidException(@Nullable final String message,
            @Nullable final String description,
            @Nullable final Throwable cause,
            @Nullable final URI href) {

        super(ERROR_CODE, message, description, cause, href);
    }

    /**
     * Returns a builder for fluently creating instances of {@code JsonPointerInvalidException}s..
     *
     * @return a new builder for JsonPointerInvalidException objects.
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * A mutable builder for a {@code JsonPointerInvalidException}.
     */
    @NotThreadSafe
    public static final class Builder extends AbstractJsonExceptionBuilder<JsonPointerInvalidException> {

        private Builder() {
            super(ERROR_CODE);
            description(DEFAULT_DESCRIPTION);
        }

        /**
         * Sets a message which points to the name of the invalid JSON pointer. Thus if this method is called,
         * {@link #message} should not be called.
         *
         * @param jsonPointerString the string representation of the invalid JSON pointer.
         * @return this builder to allow method chaining.
         */
        public Builder jsonPointer(@Nullable final CharSequence jsonPointerString) {
            message(MessageFormat.format("The JSON pointer <{0}> is invalid!", jsonPointerString));
            return this;
        }

        @Override
        protected JsonPointerInvalidException doBuild(final String errorCode,
                @Nullable final String message,
                @Nullable final String description,
                @Nullable final Throwable cause,
                @Nullable final URI href) {

            return new JsonPointerInvalidException(message, description, cause, href);
        }

    }

}
