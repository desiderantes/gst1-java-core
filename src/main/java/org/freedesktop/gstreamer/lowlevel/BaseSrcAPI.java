/*
 * Copyright (c) 2009 Levente Farkas
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

package org.freedesktop.gstreamer.lowlevel;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Union;
import com.sun.jna.ptr.LongByReference;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.BaseSrc;
import org.freedesktop.gstreamer.event.Event;
import org.freedesktop.gstreamer.lowlevel.GstAPI.GstSegmentStruct;
import org.freedesktop.gstreamer.lowlevel.GstElementAPI.GstElementClass;
import org.freedesktop.gstreamer.lowlevel.GstElementAPI.GstElementStruct;
import org.freedesktop.gstreamer.query.Query;

import java.util.Arrays;
import java.util.List;

/**
 * GstBaseSrc methods and structures
 *
 * @see https://cgit.freedesktop.org/gstreamer/gstreamer/tree/libs/gst/base/gstbasesrc.h?h=1.8
 */
public interface BaseSrcAPI extends Library {

    BaseSrcAPI BASESRC_API = GstNative.load("gstbase", BaseSrcAPI.class);

    int GST_PADDING = GstAPI.GST_PADDING;
    int GST_PADDING_LARGE = GstAPI.GST_PADDING_LARGE;

    GType gst_base_src_get_type();

    FlowReturn gst_base_src_wait_playing(BaseSrc src);

    void gst_base_src_set_live(BaseSrc src, boolean live);

    boolean gst_base_src_is_live(BaseSrc src);

    void gst_base_src_set_format(BaseSrc src, Format format);

    boolean gst_base_src_query_latency(BaseSrc src, boolean[] live, long[] min_latency, long[] max_latency);

    void gst_base_src_set_blocksize(BaseSrc src, long blocksize);

    long gst_base_src_get_blocksize(BaseSrc src);

    void gst_base_src_set_do_timestamp(BaseSrc src, boolean timestamp);

    boolean gst_base_src_get_do_timestamp(BaseSrc src);

    boolean gst_base_src_new_seamless_segment(BaseSrc src, long start, long stop, long position);

    // -------------- Callbacks -----------------
    interface GetCaps extends Callback {
        Caps callback(BaseSrc src, Caps filter);
    }

    interface SetCaps extends Callback {
        boolean callback(BaseSrc src, Caps caps);
    }

    interface BooleanFunc1 extends Callback {
        boolean callback(BaseSrc src);
    }

    interface DecideAllocation extends Callback {
        boolean callback(BaseSrc src, Query query);
    }

    interface GetTimes extends Callback {
        void callback(BaseSrc src, Buffer buffer,
                             Pointer /* GstClockTime */ start, Pointer /* GstClockTime */ end);
    }

    interface GetSize extends Callback {
        boolean callback(BaseSrc src, LongByReference size);
    }

    interface EventNotify extends Callback {
        boolean callback(BaseSrc src, Event event);
    }

    interface Create extends Callback {
        FlowReturn callback(BaseSrc src, long offset, int size,
                                   Pointer /* GstBuffer ** */ bufRef);
    }

    interface Fill extends Callback {
        FlowReturn callback(BaseSrc src, long offset, int size,
                                   Buffer buffer);
    }

    interface Seek extends Callback {
        boolean callback(BaseSrc src, GstSegmentStruct segment);
    }

    interface QueryFunc extends Callback {
        boolean callback(BaseSrc src, Query query);
    }

    interface Fixate extends Callback {
        Caps callback(BaseSrc src, Caps caps);
    }

    interface PrepareSeek extends Callback {
        boolean callback(BaseSrc src, Event seek, GstSegmentStruct segment);
    }

    final class GstBaseSrcStruct extends com.sun.jna.Structure {
        public GstElementStruct element;

        /*< protected >*/
        public volatile Pad srcpad;

        /* available to subclass implementations */
        /* MT-protected (with LIVE_LOCK) */
        public volatile /* GMutex */ Pointer live_lock;
        public volatile /* GCond */ Pointer live_cond;
        public volatile boolean is_live;
        public volatile boolean live_running;

        /* MT-protected (with LOCK) */
        public volatile int blocksize;    /* size of buffers when operating push based */
        public volatile boolean can_activate_push;    /* some scheduling properties */
        public volatile boolean random_access;

        public volatile /* GstClockID */ Pointer clock_id;    /* for syncing */

        /* MT-protected (with STREAM_LOCK) */
        public volatile GstSegmentStruct segment;
        /* MT-protected (with STREAM_LOCK) */
        public volatile boolean need_newsegment;

        public volatile int num_buffers;
        public volatile int num_buffers_left;

        public volatile boolean typefind;
        public volatile boolean running;
        public volatile Event pending_seek;

        public volatile Pointer /* GstBaseSrcPrivate */ priv;

        /*< private >*/
        public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING_LARGE];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("element", "srcpad",
                    "live_lock", "live_cond", "is_live", "live_running",
                    "blocksize", "can_activate_push", "random_access",
                    "clock_id", "segment", "need_newsegment",
                    "num_buffers", "num_buffers_left",
                    "typefind", "running", "pending_seek",
                    "priv", "_gst_reserved");
        }
    }

    final class GstBaseSrcAbiData extends Union {
        public volatile GstBaseSrcAbi abi;
        public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING_LARGE - 1];
    }

    final class GstBaseSrcAbi extends com.sun.jna.Structure {
        public volatile boolean typefind;
        public volatile boolean running;
        public volatile Pointer /* GstEvent */ pending_seek;

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("typefind", "running", "pending_seek");
        }
    }

    final class GstBaseSrcClass extends com.sun.jna.Structure {
        //
        // Actual data members
        //
        public GstElementClass parent_class;
        /* get caps from subclass */
        public GetCaps get_caps;
        /* decide on caps */
        public BooleanFunc1 negotiate;

        /*< public >*/
        /* virtual methods for subclasses */
        /* called if, in negotation, caps need fixating */
        public Fixate fixate;
        /* notify the subclass of new caps */
        public SetCaps set_caps;
        /* setup allocation query */
        public DecideAllocation decide_allocation;
        /* start and stop processing, ideal for opening/closing the resource */
        public BooleanFunc1 start;
        public BooleanFunc1 stop;
        /*
         * Given a buffer, return start and stop time when it should be pushed
         * out. The base class will sync on the clock using these times.
         */
        public GetTimes get_times;
        /* get the total size of the resource in bytes */
        public GetSize get_size;
        /* check if the resource is seekable */
        public BooleanFunc1 is_seekable;
        /* Prepare the segment on which to perform do_seek(), converting to the
         * current basesrc format. */
        public PrepareSeek prepare_seek_segment;
        /* notify subclasses of a seek */
        public Seek do_seek;
        /* unlock any pending access to the resource. subclasses should unlock
         * any function ASAP. */
        public BooleanFunc1 unlock;
        /* Clear any pending unlock request, as we succeeded in unlocking */
        public BooleanFunc1 unlock_stop;
        /* notify subclasses of a query */
        public QueryFunc query;
        /* notify subclasses of an event */
        public EventNotify event;
        /* ask the subclass to create a buffer with offset and size, the default
         * implementation will call alloc and fill. */
        public Create create;
        /* ask the subclass to allocate an output buffer. The default implementation
         * will use the negotiated allocator. */
        public Create alloc;
        public Fill fill;
        /*< private >*/
        public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING_LARGE];

        public GstBaseSrcClass() {
        }

        public GstBaseSrcClass(Pointer ptr) {
            useMemory(ptr);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("parent_class",
                    "get_caps", "negotiate", "fixate", "set_caps",
                    "decide_allocation",
                    "start", "stop",
                    "get_times", "get_size", "is_seekable",
                    "prepare_seek_segment", "do_seek",
                    "unlock", "unlock_stop",
                    "query", "event", "create", "alloc", "fill",
                    "_gst_reserved");
        }

    }
}
