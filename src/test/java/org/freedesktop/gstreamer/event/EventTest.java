/*
 * Copyright (c) 2008 Wayne Meissner
 *
 * This file is part of gstreamer-java.
 *
 * gstreamer-java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gstreamer-java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with gstreamer-java.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.freedesktop.gstreamer.event;

import org.freedesktop.gstreamer.*;
import org.freedesktop.gstreamer.lowlevel.GstAPI;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.ref.WeakReference;
import java.util.EnumSet;

import static org.freedesktop.gstreamer.lowlevel.GstEventAPI.GSTEVENT_API;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wayne
 */
public class EventTest {
    public EventTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("EventTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void verifyFlags() {
        // Verify that the flags in the enum match the native ones.
        EventType[] types = EventType.values();
        for (EventType t : types) {
            int flags = GSTEVENT_API.gst_event_type_get_flags(t);
            assertEquals(flags, (t.intValue() & 0xFF), "Incorrect flags for: " + t.name());
        }
    }

    @Test
    public void createEOSEvent() {
        new EOSEvent();
    }

    @Test
    public void createFlushStartEvent() {
        new FlushStartEvent();
    }

    @Test
    public void createFlushStopEvent() {
        new FlushStopEvent();
    }

    @Test
    public void createLatencyEvent() {
        new LatencyEvent(ClockTime.ZERO);
    }

    @Test
    public void createSegmentEvent() {
        GstAPI.GstSegmentStruct struct = new GstAPI.GstSegmentStruct();
        struct.flags = 0;
        struct.rate = 1.0;
        struct.applied_rate = 1.0;
        struct.format = Format.TIME;
        new SegmentEvent(struct);
    }

    @Test
    public void createCapsEvent() {
        new CapsEvent(Caps.fromString("video/x-raw,format=I420"));
    }

    @Test
    public void createReconfigureEvent() {
        new ReconfigureEvent();
    }

    @Test
    public void createStreamStartEvent() {
        new StreamStartEvent("a stream_id");
    }

    @Test
    public void createStepEvent() {
        new StepEvent(Format.BUFFERS, 1, 1, true, false);
    }

    @Test
    public void gst_event_new_eos() {
        Event eos = GSTEVENT_API.gst_event_new_eos();
        assertNotNull(eos, "gst_event_new_eos returned null");
        assertTrue(eos instanceof EOSEvent, "gst_event_new_eos returned a non-EOS event");
    }

    @Test
    public void gst_event_new_flush_start() {
        Event ev = GSTEVENT_API.gst_event_new_flush_start();
        assertNotNull(ev, "gst_event_new_flush_start returned null");
        assertTrue(ev instanceof FlushStartEvent, "gst_event_new_flush_start returned a non-FLUSH_START event");
    }

    @Test
    public void gst_event_new_flush_stop() {
        Event ev = GSTEVENT_API.gst_event_new_flush_stop();
        assertNotNull(ev, "gst_event_new_flush_stop returned null");
        assertTrue(ev instanceof FlushStopEvent, "gst_event_new_flush_stop returned a non-FLUSH_STOP event");
    }

    @Test
    public void gst_event_new_latency() {
        Event ev = GSTEVENT_API.gst_event_new_latency(0);
        assertNotNull(ev, "gst_event_new_latency returned null");
        assertTrue(ev instanceof LatencyEvent, "gst_event_new_latency returned a non-LATENCY event");
    }

    @Test
    public void gst_event_new_new_segment() {
        GstAPI.GstSegmentStruct struct = new GstAPI.GstSegmentStruct();
        struct.flags = 0;
        struct.rate = 1.0;
        struct.applied_rate = 1.0;
        struct.format = Format.TIME;
        Event ev = GSTEVENT_API.gst_event_new_segment(struct);
        assertNotNull(ev, "gst_event_new_latency returned null");
        assertTrue(ev instanceof SegmentEvent, "gst_event_new_latency returned a non-NEWSEGMENT event");
    }

    @Test
    public void getLatency() {
//        final ClockTime MAGIC = ClockTime.valueOf(0xdeadbeef, TimeUnit.NANOSECONDS);
        long MAGIC = 0xdeadbeef;
        LatencyEvent ev = new LatencyEvent(MAGIC);
        assertEquals(MAGIC, ev.getLatency(), "Incorrect latency returned");
    }

    @Test
    public void NewSegment_getRate() {
        final double RATE = 0xdeadbeef;
        SegmentEvent ev = new SegmentEvent(new GstAPI.GstSegmentStruct(0, RATE, RATE, Format.TIME, 0, 0, 0, 0, 0, 0, 0));
        assertEquals(RATE, ev.getSegment().rate, 0.0, "Incorrect rate returned from getRate");
    }

    @Test
    public void NewSegment_getStart() {
        final long START = 0xdeadbeefL;
        SegmentEvent ev = new SegmentEvent(new GstAPI.GstSegmentStruct(0, 0.1, 0.1, Format.TIME, 0, 0, START, -1L, 0, 0, 0));
        assertEquals(START, ev.getSegment().start, "Incorrect rate returned from getStart");
    }

    @Test
    public void NewSegment_getStop() {
        final long STOP = 0xdeadbeefL;
        SegmentEvent ev = new SegmentEvent(new GstAPI.GstSegmentStruct(0, 0.1, 0.1, Format.TIME, 0, 0, 0L, STOP, 0, 0, 0));
        assertEquals(STOP, ev.getSegment().stop, "Incorrect rate returned from getRate");
    }

    @Test
    public void gst_event_new_tag() {
        Event ev = GSTEVENT_API.gst_event_new_tag(new TagList());
        assertNotNull(ev, "gst_event_new_tag returned null");
        assertTrue(ev instanceof TagEvent, "gst_event_new_tag returned a non-TAG event");
    }

    @Test
    public void TagEvent_testGC() {
        TagEvent ev = new TagEvent(new TagList());
        @SuppressWarnings("unused")
        TagList tl = ev.getTagList();
        WeakReference<Event> evRef = new WeakReference<Event>(ev);
        ev = null;
        assertFalse(GCTracker.waitGC(evRef), "Event ref collected before TagList is unreferenced");
        tl = null;
        assertTrue(GCTracker.waitGC(evRef), "Event ref not collected after TagList is unreferenced");
    }

    @Test
    public void Event_testGC() {
        Event ev = new LatencyEvent(100);
        @SuppressWarnings("unused")
        Structure s = ev.getStructure();
        WeakReference<Event> evRef = new WeakReference<Event>(ev);
        ev = null;
        assertFalse(GCTracker.waitGC(evRef), "Event ref collected before Structure is unreferenced");
        s = null;
        assertTrue(GCTracker.waitGC(evRef), "Event ref not collected after Structure is unreferenced");
    }

    @Test
    public void gst_event_new_buffer_size() {
        final long MIN = 0x1234;
        final long MAX = 0xdeadbeef;
        final boolean ASYNC = false;
        Event ev = GSTEVENT_API.gst_event_new_buffer_size(Format.BYTES, MIN, MAX, ASYNC);
        assertNotNull(ev, "gst_event_new_buffer_size returned null");
        assertTrue(ev instanceof BufferSizeEvent, "gst_event_new_buffer_size returned a non-BUFFERSIZE event");
    }

    @Test
    public void BufferSize_getMinimumSize() {
        final long MIN = 0x1234;
        final long MAX = 0xdeadbeef;
        final boolean ASYNC = false;
        BufferSizeEvent ev = (BufferSizeEvent) GSTEVENT_API.gst_event_new_buffer_size(Format.BYTES, MIN, MAX, ASYNC);
        assertEquals(MIN, ev.getMinimumSize(), "Wrong minimum size stored");
    }

    @Test
    public void BufferSize_getMaximumSize() {
        final long MIN = 0x1234;
        final long MAX = 0xdeadbeef;
        final boolean ASYNC = false;
        BufferSizeEvent ev = (BufferSizeEvent) GSTEVENT_API.gst_event_new_buffer_size(Format.BYTES, MIN, MAX, ASYNC);
        assertEquals(MAX, ev.getMaximumSize(), "Wrong minimum size stored");
    }

    @Test
    public void BufferSize_isAsync() {
        final long MIN = 0x1234;
        final long MAX = 0xdeadbeef;
        final boolean ASYNC = false;
        BufferSizeEvent ev = (BufferSizeEvent) GSTEVENT_API.gst_event_new_buffer_size(Format.BYTES, MIN, MAX, ASYNC);
        assertEquals(ASYNC, ev.isAsync(), "Wrong minimum size stored");
        BufferSizeEvent ev2 = (BufferSizeEvent) GSTEVENT_API.gst_event_new_buffer_size(Format.BYTES, MIN, MAX, !ASYNC);
        assertEquals(!ASYNC, ev2.isAsync(), "Wrong minimum size stored");
    }

    @Test
    public void gst_event_new_qos() {
        Event ev = GSTEVENT_API.gst_event_new_qos(QOSType.THROTTLE, 0.0, 0, ClockTime.NONE);
        assertNotNull(ev, "gst_event_new_qos returned null");
        assertTrue(ev instanceof QOSEvent, "gst_event_new_qos returned a non-QOS event");
    }

    @Test
    public void QOS_getProportion() {
        final double PROPORTION = 0xdeadbeef;
        QOSEvent ev = new QOSEvent(QOSType.THROTTLE, PROPORTION, 0, ClockTime.ZERO);
        assertEquals(PROPORTION, ev.getProportion(), 0d, "Wrong proportion");
    }

    @Test
    public void QOS_getDifference() {
        long DIFF = 0x4096;
        QOSEvent ev = new QOSEvent(QOSType.THROTTLE, 0d, DIFF, ClockTime.ZERO);
        assertEquals(DIFF, ev.getDifference(), "Wrong difference");
    }

    @Test
    public void QOS_getTimestamp() {
        final long STAMP = 0xdeadbeef;
        QOSEvent ev = new QOSEvent(QOSType.THROTTLE, 0d, 0, STAMP);
        assertEquals(STAMP, ev.getTimestamp(), "Wrong timestamp");
    }

    @Test
    public void QOS_getType() {
        final long STAMP = 0xdeadbeef;
        QOSEvent ev = new QOSEvent(QOSType.THROTTLE, 0d, 0, STAMP);
        assertEquals(QOSType.THROTTLE, ev.getType(), "Wrong QOSType");
    }

    @Test
    public void gst_event_new_seek() {
        Event ev = GSTEVENT_API.gst_event_new_seek(1.0, Format.TIME, 0,
                SeekType.SET, 0, SeekType.SET, 0);
        assertNotNull(ev, "gst_event_new_seek returned null");
        assertTrue(ev instanceof SeekEvent, "gst_event_new_seek returned a non-SEEK event");
    }

    @Test
    public void Seek_getFormat() {
        for (Format FORMAT : new Format[]{Format.TIME, Format.BYTES}) {
            SeekEvent ev = new SeekEvent(1.0, FORMAT, EnumSet.noneOf(SeekFlags.class),
                    SeekType.SET, 0, SeekType.SET, 0);
            assertEquals(FORMAT, ev.getFormat(), "Wrong format in SeekEvent");
        }
    }

    @Test
    public void Seek_getStartType() {
        for (SeekType TYPE : new SeekType[]{SeekType.SET, SeekType.END}) {
            SeekEvent ev = new SeekEvent(1.0, Format.TIME, EnumSet.noneOf(SeekFlags.class),
                    TYPE, 0, SeekType.NONE, 0);
            assertEquals(TYPE, ev.getStartType(), "Wrong startType in SeekEvent");
        }
    }

    @Test
    public void Seek_getStopType() {
        for (SeekType TYPE : new SeekType[]{SeekType.SET, SeekType.END}) {
            SeekEvent ev = new SeekEvent(1.0, Format.TIME, EnumSet.noneOf(SeekFlags.class),
                    SeekType.NONE, 0, TYPE, 0);
            assertEquals(TYPE, ev.getStopType(), "Wrong stopType in SeekEvent");
        }
    }

    @Test
    public void Seek_getStart() {
        final long START = 0xdeadbeef;
        SeekEvent ev = new SeekEvent(1.0, Format.TIME, EnumSet.noneOf(SeekFlags.class),
                SeekType.SET, START, SeekType.SET, -1);
        assertEquals(START, ev.getStart(), "Wrong start in SeekEvent");
    }

    @Test
    public void Seek_getStop() {
        final long STOP = 0xdeadbeef;
        SeekEvent ev = new SeekEvent(1.0, Format.TIME, EnumSet.noneOf(SeekFlags.class),
                SeekType.SET, 0, SeekType.SET, STOP);
        assertEquals(STOP, ev.getStop(), "Wrong stop in SeekEvent");
    }

    @Test
    public void Seek_rateZero() {
        assertThrows(IllegalArgumentException.class, () -> new SeekEvent(0.0, Format.TIME, EnumSet.noneOf(SeekFlags.class),
                SeekType.SET, 0, SeekType.SET, -1), "A rate of 0.0 should throw an exception");

    }

    @Test
    public void gst_event_new_caps() {
        Event ev = GSTEVENT_API.gst_event_new_caps(Caps.fromString("video/x-raw,format=I420"));
        assertNotNull(ev, "gst_event_new_caps returned null");
        assertTrue(ev instanceof CapsEvent, "gst_event_new_caps returned a non-CAPS event");
    }

    @Test
    public void gst_event_new_reconfigure() {
        Event ev = GSTEVENT_API.gst_event_new_reconfigure();
        assertNotNull(ev, "gst_event_new_reconfigure returned null");
        assertTrue(ev instanceof ReconfigureEvent, "gst_event_new_reconfigure returned a non-RECONFIGURE event");
    }

    @Test
    public void gst_event_new_stream_start() {
        Event ev = GSTEVENT_API.gst_event_new_stream_start("a stream id");
        assertNotNull(ev, "gst_event_new_stream_start returned null");
        assertTrue(ev instanceof StreamStartEvent, "gst_event_new_stream_start returned a non-STREAM-START event");
    }

    @Test
    public void gst_event_new_step() {
        Event ev = GSTEVENT_API.gst_event_new_step(Format.BUFFERS, 1, 1, true, false);
        assertNotNull(ev, "gst_event_new_step returned null");
        assertTrue(ev instanceof StepEvent, "gst_event_new_step returned a non-STEP event");
    }
}
