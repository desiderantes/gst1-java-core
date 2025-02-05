/*
 * Copyright (c) 2021 Neil C Smith
 * Copyright (c) 2016 Isaac Raño Jares
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

import static org.freedesktop.gstreamer.lowlevel.GioAPI.GIO_API;

public class GInetAddress extends GObject {

    public static final String GTYPE_NAME = "GInetAddress";

    GInetAddress(Initializer init) {
        super(init);
    }

    public String getAddress() {
        return GIO_API.g_inet_address_to_string(getRawPointer());
    }

}
