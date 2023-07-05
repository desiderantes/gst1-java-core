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
import com.sun.jna.ptr.IntByReference;
import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.elements.BaseTransform;
import org.freedesktop.gstreamer.event.Event;
import org.freedesktop.gstreamer.lowlevel.GstAPI.GstSegmentStruct;
import org.freedesktop.gstreamer.lowlevel.GstElementAPI.GstElementClass;
import org.freedesktop.gstreamer.lowlevel.GstElementAPI.GstElementStruct;
import org.freedesktop.gstreamer.query.Query;

import java.util.Arrays;
import java.util.List;

/**
 * GstBaseTransform methods and structures
 *
 * @see https://cgit.freedesktop.org/gstreamer/gstreamer/tree/libs/gst/base/gstbasetransform.h?h=1.8
 */
public interface BaseTransformAPI extends Library {

    BaseTransformAPI BASETRANSFORM_API = GstNative.load("gstbase", BaseTransformAPI.class);

    int GST_PADDING = GstAPI.GST_PADDING;
    int GST_PADDING_LARGE = GstAPI.GST_PADDING_LARGE;

    GType gst_base_transform_get_type();

    void gst_base_transform_set_passthrough(BaseTransform trans, boolean passthrough);

    boolean gst_base_transform_is_passthrough(BaseTransform trans);

    void gst_base_transform_set_in_place(BaseTransform trans, boolean in_place);

    boolean gst_base_transform_is_in_place(BaseTransform trans);

    void gst_base_transform_update_qos(BaseTransform trans, double proportion, long diff, long timestamp);

    void gst_base_transform_set_qos_enabled(BaseTransform trans, boolean enabled);

    boolean gst_base_transform_is_qos_enabled(BaseTransform trans);

    void gst_base_transform_set_gap_aware(BaseTransform trans, boolean gap_aware);

    void gst_base_transform_suggest(BaseTransform trans, Caps caps, int size);

    void gst_base_transform_reconfigure(BaseTransform trans);

    interface TransformCaps extends Callback {
        Caps callback(BaseTransform trans, PadDirection direction, Caps caps, Caps filter);
    }

    interface FixateCaps extends Callback {
        Caps callback(BaseTransform trans, PadDirection direction, Caps caps, Caps othercaps);
    }

    interface AcceptCaps extends Callback {
        boolean callback(BaseTransform trans, PadDirection direction, Caps caps);
    }

    interface SetCaps extends Callback {
        boolean callback(BaseTransform trans, Caps incaps, Caps outcaps);
    }

    interface QueryFunc extends Callback {
        boolean callback(BaseTransform trans, PadDirection direction, Query query);
    }

    interface DecideAllocation extends Callback {
        boolean callback(BaseTransform trans, Query query);
    }

    interface FilterMeta extends Callback {
        boolean callback(BaseTransform trans, Query query, GType api, Pointer /* GstStructure */ params);
    }

    interface ProposeAllocation extends Callback {
        boolean callback(BaseTransform trans, Query decide_query, Query query);
    }

    interface TransformSize extends Callback {
        boolean callback(BaseTransform trans, PadDirection direction, Caps caps,
                                int size, Caps othercaps, IntByReference othersize);
    }

    interface GetUnitSize extends Callback {
        boolean callback(BaseTransform trans, Caps caps, IntByReference size);
    }


    interface BooleanFunc1 extends Callback {
        boolean callback(BaseTransform sink);
    }

    interface EventNotify extends Callback {
        boolean callback(BaseTransform trans, Event event);
    }

    interface PrepareOutput extends Callback {
        FlowReturn callback(BaseTransform trans, Buffer input, Pointer /*GstBuffer ** */ outbuf);
    }

    interface CopyMetadata extends Callback {
        boolean callback(BaseTransform trans, Buffer input, Buffer output);
    }

    interface TransformMeta extends Callback {
        boolean callback(BaseTransform trans, Buffer outbuf, Pointer /* GstMeta */ meta, Buffer output);
    }

    interface BeforeTransform extends Callback {
        void callback(BaseTransform trans, Buffer inbuf);
    }

    interface Transform extends Callback {
        FlowReturn callback(BaseTransform trans, Buffer inbuf, Buffer outbuf);
    }

    interface TransformIp extends Callback {
        FlowReturn callback(BaseTransform trans, Buffer buf);
    }

    interface SubmitInputBuffer extends Callback {
        FlowReturn callback(BaseTransform trans, boolean is_discont, Buffer input);
    }

    interface GenerateOutput extends Callback {
        FlowReturn callback(BaseTransform trans, Pointer /* GstBuffer ** */ outbuf);
    }

    final class GstBaseTransformStruct extends com.sun.jna.Structure {
        public GstElementStruct element;

        /*< protected >*/
        /* source and sink pads */
        public volatile Pad sinkpad;
        public volatile Pad srcpad;

        /* MT-protected (with STREAM_LOCK) */
        public volatile boolean have_segment;
        public volatile GstSegmentStruct segment;
        /* Default submit_input_buffer places the buffer here,
         * for consumption by the generate_output method: */
        public volatile Buffer queued_buf;

        /*< private >*/
        public volatile Pointer /* GstBaseTransformPrivate */ priv;

        public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING_LARGE - 1];

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("element", "sinkpad", "srcpad",
                    "have_segment", "segment", "queued_buf",
                    "priv", "_gst_reserved");
        }
    }

    final class GstBaseTransformClass extends com.sun.jna.Structure {
        //
        // Actual data members
        //
        public GstElementClass parent_class;
        /*< public >*/
        public volatile boolean passthrough_on_same_caps;
        public volatile boolean transform_ip_on_passthrough;
        /* virtual methods for subclasses */
        public TransformCaps transform_caps;
        public FixateCaps fixate_caps;
        public AcceptCaps accept_caps;
        public SetCaps set_caps;
        public QueryFunc query;
        /* decide allocation query for output buffers */
        public DecideAllocation decide_allocation;
        public FilterMeta filter_meta;
        /* propose allocation query parameters for input buffers */
        public ProposeAllocation propose_allocation;
        /* transform size */
        public TransformSize transform_size;
        public GetUnitSize get_unit_size;
        /* states */
        public BooleanFunc1 start;
        public BooleanFunc1 stop;
        /* sink and src pad event handlers */
        public EventNotify sink_event;
        public EventNotify src_event;
        public PrepareOutput prepare_output_buffer;
        /* metadata */
        public CopyMetadata copy_metadata;
        public TransformMeta transform_meta;
        public BeforeTransform before_transform;
        /* transform */
        public Transform transform;
        public TransformIp transform_ip;
        public SubmitInputBuffer submit_input_buffer;
        public GenerateOutput generate_output;
        /*< private >*/
        public volatile Pointer[] _gst_reserved = new Pointer[GST_PADDING_LARGE - 2];
        public GstBaseTransformClass() {
        }

        public GstBaseTransformClass(Pointer ptr) {
            useMemory(ptr);
            read();
        }

        @Override
        protected List<String> getFieldOrder() {
            return Arrays.asList("parent_class", "passthrough_on_same_caps", "transform_ip_on_passthrough",
                    "transform_caps", "fixate_caps", "accept_caps", "set_caps", "query",
                    "decide_allocation", "filter_meta", "propose_allocation",
                    "transform_size", "get_unit_size",
                    "start", "stop", "sink_event", "src_event",
                    "prepare_output_buffer", "copy_metadata", "transform_meta",
                    "before_transform", "transform", "transform_ip",
                    "submit_input_buffer", "generate_output", "_gst_reserved");
        }
    }
}
