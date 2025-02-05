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

import com.sun.jna.Pointer;
import org.freedesktop.gstreamer.lowlevel.GPointer;
import org.freedesktop.gstreamer.lowlevel.GlibAPI;

import java.util.concurrent.Callable;

import static org.freedesktop.gstreamer.lowlevel.GlibAPI.GLIB_API;

/**
 *
 */
public class GSource extends RefCountedObject {

    private GlibAPI.GSourceFunc callback;

    GSource(Initializer init) {
        super(new Handle(init.ptr, init.ownsHandle), init.needRef);
    }

    public int attach(GMainContext context) {
        return GLIB_API.g_source_attach(this, context);
    }

    public void setCallback(final Callable<Boolean> call) {
        this.callback = data -> {
            if (GLIB_API.g_source_is_destroyed(getRawPointer())) {
                return false;
            }
            try {
                return call.call();
            } catch (Exception ex) {
                return false;
            }
        };
        GLIB_API.g_source_set_callback(this, callback, null, null);
    }

    private static final class Handle extends RefCountedObject.Handle {

        Handle(GPointer ptr, boolean ownsHandle) {
            super(ptr, ownsHandle);
        }

        @Override
        protected void disposeNativeHandle(GPointer ptr) {
            GLIB_API.g_source_destroy(ptr.getPointer());
            GLIB_API.g_source_unref(ptr.getPointer());
        }

        @Override
        protected void ref() {
            GLIB_API.g_source_ref(getPointer().getPointer());
        }

        @Override
        protected void unref() {
            GLIB_API.g_source_unref(getPointer().getPointer());
        }

    }
}
