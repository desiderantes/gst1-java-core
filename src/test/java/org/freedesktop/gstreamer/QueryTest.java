/*
 * Copyright (c) 2009 Levente Farkas
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

package org.freedesktop.gstreamer;

import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstQueryAPI;
import org.freedesktop.gstreamer.query.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wayne
 */
public class QueryTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("QueryTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void gst_query_new_position() {
        Query query = GstQueryAPI.GSTQUERY_API.gst_query_new_position(Format.TIME);
        assertNotNull(query, "Query.newPosition returned null");
        assertTrue(query instanceof PositionQuery, "Returned query not instance of PositionQuery");
    }

    @Test
    public void getPositionQueryFormat() {
        for (Format format : Format.values()) {
            PositionQuery query = new PositionQuery(format);
            assertEquals(format, query.getFormat(), "Format returned from getFormat() is incorrect");
        }
    }

    @Test
    public void getPosition() {
        PositionQuery query = new PositionQuery(Format.TIME);
        final long POSITION = 0xdeadbeef;
        query.setPosition(Format.TIME, POSITION);
        assertEquals(POSITION, query.getPosition(), "Incorrect position returned");
    }

    @Test
    public void positionQueryToString() {
        PositionQuery query = new PositionQuery(Format.TIME);
        query.setPosition(Format.TIME, 1234);
        String s = query.toString();
        assertTrue(s.contains("format=TIME"), "toString() did not return format");
        assertTrue(s.contains("position=1234"), "toString() did not return position");
    }

    @Test
    public void newDurationQuery() {
        Query query = new DurationQuery(Format.TIME);
        assertNotNull(query, "Query.newDuration returned null");
        assertTrue(query instanceof DurationQuery, "Returned query not instance of DurationQuery");
    }

    @Test
    public void gst_query_new_duration() {
        Query query = GstQueryAPI.GSTQUERY_API.gst_query_new_duration(Format.TIME);
        assertNotNull(query, "Query.newDuration returned null");
        assertTrue(query instanceof DurationQuery, "Returned query not instance of DurationQuery");
    }

    @Test
    public void getDurationQueryFormat() {
        for (Format format : Format.values()) {
            DurationQuery query = new DurationQuery(format);
            assertEquals(format, query.getFormat(), "Format returned from getFormat() is incorrect");
        }
    }

    @Test
    public void getDuration() {
        DurationQuery query = new DurationQuery(Format.TIME);
        final long DURATION = 0xdeadbeef;
        query.setDuration(Format.TIME, DURATION);
        assertEquals(DURATION, query.getDuration(), "Incorrect duration returned");
    }

    @Test
    public void durationQueryToString() {
        DurationQuery query = new DurationQuery(Format.TIME);
        query.setDuration(Format.TIME, 1234);
        String s = query.toString();
        assertTrue(s.contains("format=TIME"), "toString() did not return format");
        assertTrue(s.contains("duration=1234"), "toString() did not return duration");
    }

    @Test
    public void gst_query_new_latency() {
        Query query = GstQueryAPI.GSTQUERY_API.gst_query_new_latency();
        assertNotNull(query, "gst_query_new_latency() returned null");
        assertTrue(query instanceof LatencyQuery, "Returned query not instance of LatencyQuery");
    }

    @Test
    public void latencyIsLive() {
        LatencyQuery query = new LatencyQuery();
        query.setLatency(true, 0, 0);
        assertTrue(query.isLive(), "isLive not set to true");
        query.setLatency(false, 0, 0);
        assertFalse(query.isLive(), "isLive not set to true");
    }

    @Test
    public void getMinimumLatency() {
        LatencyQuery query = new LatencyQuery();
//        final ClockTime MIN = ClockTime.fromMillis(13000);
        final long MIN = TimeUnit.MILLISECONDS.toNanos(13000);
        query.setLatency(false, MIN, ~0);
        assertEquals(MIN, query.getMinimumLatency(), "Min latency not set");
    }

    @Test
    public void getMaximumLatency() {
        LatencyQuery query = new LatencyQuery();
//        final ClockTime MAX = ClockTime.fromMillis(123000);
        final long MAX = TimeUnit.MILLISECONDS.toNanos(123000);
        query.setLatency(false, 0, MAX);
        assertEquals(MAX, query.getMaximumLatency(), "Min latency not set");
    }

    @Test
    public void latencyQueryToString() {
        LatencyQuery query = new LatencyQuery();
        long minLatency = TimeUnit.MILLISECONDS.toNanos(13000); //ClockTime.fromMillis(13000);
        long maxLatency = TimeUnit.MILLISECONDS.toNanos(200000);//ClockTime.fromMillis(200000);
        query.setLatency(true, minLatency, maxLatency);
        String s = query.toString();
        assertTrue(s.contains("live=true"), "toString() did not return isLive");
        assertTrue(s.contains("min=" + minLatency), "toString() did not return minLatency");
        assertTrue(s.contains("max=" + maxLatency), "toString() did not return minLatency");
    }

    @Test
    public void segmentQuery() {
        SegmentQuery query = new SegmentQuery(Format.TIME);
//        ClockTime end = ClockTime.fromMillis(1000);
        long end = TimeUnit.MILLISECONDS.toNanos(1000);
        query.setSegment(1.0, Format.TIME, 0, end);
        assertEquals(Format.TIME, query.getFormat(), "Format not set correctly");
        assertEquals(0, query.getStart(), "Start time not set correctly");
        assertEquals(end, query.getEnd(), "End time not set correctly");
    }

    @Test
    public void seekingQuery() {
        SeekingQuery query = new SeekingQuery(Format.TIME);
        long start = 0;
        long end = TimeUnit.MILLISECONDS.toNanos(1000);
        query.setSeeking(Format.TIME, true, start, end);
        assertEquals(Format.TIME, query.getFormat(), "Format not set");
        assertEquals(start, query.getStart(), "Start time not set");
        assertEquals(end, query.getEnd(), "End time not set");
    }

    @Test
    public void formatsQuery() {
        Query query = GstQueryAPI.GSTQUERY_API.gst_query_new_formats();
        assertNotNull(query, "gst_query_new_latency() returned null");
        assertTrue(query instanceof FormatsQuery, "Returned query not instance of LatencyQuery");
    }

    @Test
    public void formatsQueryCount() {
        FormatsQuery query = new FormatsQuery();
        query.setFormats(Format.TIME, Format.PERCENT);
        assertEquals(2, query.getCount(), "Wrong formats count");
    }

    @Test
    public void formatsQueryFormats() {
        FormatsQuery query = new FormatsQuery();
        query.setFormats(Format.TIME, Format.PERCENT);
        assertEquals(Format.TIME, query.getFormat(0), "First format incorrect");
        assertEquals(Format.PERCENT, query.getFormat(1), "Second format incorrect");
        List<Format> formats = query.getFormats();
        assertEquals(Format.TIME, formats.get(0), "First format incorrect");
        assertEquals(Format.PERCENT, formats.get(1), "Second format incorrect");
    }

    @Test
    public void makeWriteable() {
        Query query = new SegmentQuery(Format.TIME);
        assertTrue(query.isWritable(), "New query is not writable");
        // Bumping the ref count makes this instance non writable
//        GSTMINIOBJECT_API.gst_mini_object_ref(query);
        Natives.ref(query);
        assertFalse(query.isWritable(), "Query with multiple references should not be writable");
        // Now get a new reference that is writable
        query = query.makeWritable();
        assertTrue(query.isWritable(), "Query not writable after makeWritable");
    }

//    @Test public void testQueryTypeGetName() {
//        assertEquals(QueryType.JITTER.getName(), "jitter");
//    }

    @Test
    public void gst_query_new_allocation() {
        Query query = GstQueryAPI.GSTQUERY_API.gst_query_new_allocation(Caps.fromString("video/x-raw, format=I420"), true);
        assertNotNull(query, "gst_query_new_allocation returned null");
        assertTrue(query instanceof AllocationQuery, "Returned query not instance of AllocationQuery");
    }

    @Test
    public void newAllocationQuery() {
        Query query = new AllocationQuery(Caps.fromString("video/x-raw, format=I420"), true);
        assertNotNull(query, "Query.newAllocationQuery returned null");
        assertTrue(query instanceof AllocationQuery, "Returned query not instance of AllocationQuery");
    }

    @Test
    public void getCapsAllocationQuery() {
        Caps caps = Caps.fromString("video/x-raw, format=I420");
        AllocationQuery query = new AllocationQuery(caps, true);
        assertEquals(caps, query.getCaps());
    }

    @Test
    public void needPoolAllocationQuery() {
        Caps caps = Caps.fromString("video/x-raw, format=I420");
        AllocationQuery query = new AllocationQuery(caps, true);
        assertTrue(query.isPoolNeeded());
        query.dispose();
        query = new AllocationQuery(caps, false);
        assertFalse(query.isPoolNeeded());
    }
}
