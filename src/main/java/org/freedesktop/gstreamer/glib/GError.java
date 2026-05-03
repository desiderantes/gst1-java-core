/*
 * Copyright (c) 2019 Neil C Smith
 * Copyright (c) 2007 Wayne Meissner
 *
 * This file is part of gstreamer-java.
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freedesktop.gstreamer.glib;

import java.util.Objects;

/**
 * Base GLib error type.
 *
 * @param code a numeric code representing this error
 * @param message a string representation of this error
 */
public record GError(int code, String message) {

    /**
     * Creates a new instance of GError
     *
     * @param code    native int error code
     * @param message error message text
     */
    public GError(int code, String message) {
        this.code = code;
        this.message = Objects.requireNonNull(message);
    }
}
