/*
 * Copyright (c) 2021 Neil C Smith
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

package org.freedesktop.gstreamer.lowlevel;

/**
 * GstIterator functions
 */
public interface GstIteratorAPI extends com.sun.jna.Library {

    GstIteratorAPI GSTITERATOR_API = GstNative.load(GstIteratorAPI.class);

    void gst_iterator_free(GstIteratorPtr iter);

    int gst_iterator_next(GstIteratorPtr iter, GValueAPI.GValue next);

    void gst_iterator_resync(GstIteratorPtr iter);
}
