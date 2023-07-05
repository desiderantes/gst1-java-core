/*
 * Copyright (c) 2007 Wayne Meissner
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


import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author wayne
 */
public class GarbageCollectionTest {

    public GarbageCollectionTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("test");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    @Test
    public void testElement() throws Exception {

        Element e = ElementFactory.make("fakesrc", "test element");
        GCTracker tracker = new GCTracker(e);
        e = null;
        assertTrue(tracker.waitGC(), "Element not garbage collected");
        assertTrue(tracker.waitDestroyed(), "GObject not destroyed");
    }

    @Test
    public void testBin() throws Exception {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);

        assertEquals(e1, bin.getElementByName("source"), "source not returned");
        assertEquals(e2, bin.getElementByName("sink"), "sink not returned");
        GCTracker binTracker = new GCTracker(bin);
        bin = null;
        assertTrue(binTracker.waitGC(), "Bin not garbage collected");
        assertTrue(binTracker.waitDestroyed(), "Bin not destroyed");
        GCTracker e1Tracker = new GCTracker(e1);
        GCTracker e2Tracker = new GCTracker(e2);
        e1 = null;
        e2 = null;

        assertTrue(e1Tracker.waitGC(), "First Element not garbage collected");
        assertTrue(e1Tracker.waitDestroyed(), "First Element not destroyed");
        assertTrue(e2Tracker.waitGC(), "Second Element not garbage collected");
        assertTrue(e2Tracker.waitDestroyed(), "Second Element not destroyed");

    }

    @Test
    public void testBinParsed() throws Exception {
        Bin bin = Gst.parseBinFromDescription("fakesrc name=source ! fakesink name=sink", false);
        int binRefCount = bin.getRefCount();
        List<Element> children = bin.getElements();
        assertEquals(binRefCount, bin.getRefCount(), "Iteration increased Bin refcount");
        assertEquals(2, children.size(), "Wrong number of child elements");
        Element e1 = children.get(0);
        Element e2 = children.get(1);
        GCTracker binTracker = new GCTracker(bin);
        bin = null;
        assertTrue(binTracker.waitGC(), "Bin not garbage collected");
        assertTrue(binTracker.waitDestroyed(), "Bin not destroyed");
        GCTracker e1Tracker = new GCTracker(e1);
        GCTracker e2Tracker = new GCTracker(e2);
        children = null;
        e1 = null;
        e2 = null;

        assertTrue(e1Tracker.waitGC(), "First Element not garbage collected");
        assertTrue(e1Tracker.waitDestroyed(), "First Element not destroyed");
        assertTrue(e2Tracker.waitGC(), "Second Element not garbage collected");
        assertTrue(e2Tracker.waitDestroyed(), "Second Element not destroyed");

    }

    @Test
    public void testBinRetrieval() throws Exception {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);
        int id1 = System.identityHashCode(e1);
        int id2 = System.identityHashCode(e2);

        e1 = null;
        e2 = null;
        System.gc();
        Thread.sleep(10);
        // Should return the same object that was put into the bin
        assertEquals(id1, System.identityHashCode(bin.getElementByName("source")), "source ID does not match");
        assertEquals(id2, System.identityHashCode(bin.getElementByName("sink")), "sink ID does not match");
    }

    @Test
    public void pipeline() {
        Pipeline pipe = new Pipeline("test");
        GCTracker pipeTracker = new GCTracker(pipe);
        pipe = null;
        assertTrue(pipeTracker.waitGC(), "Pipe not garbage collected");
        System.out.println("checking if pipeline is destroyed");
        assertTrue(pipeTracker.waitDestroyed(), "Pipe not destroyed");
    }

    @Test
    public void pipelineBus() {
        Pipeline pipe = new Pipeline("test");
        Bus bus = pipe.getBus();
        GCTracker busTracker = new GCTracker(bus);
        GCTracker pipeTracker = new GCTracker(pipe);

        pipe = null;
        bus = null;
        assertTrue(busTracker.waitGC(), "Bus not garbage collected");
        assertTrue(busTracker.waitDestroyed(), "Bus not destroyed");
        assertTrue(pipeTracker.waitGC(), "Pipe not garbage collected");
        assertTrue(pipeTracker.waitDestroyed(), "Pipe not destroyed");

    }

    @Test
    public void busWithListeners() {
        Pipeline pipe = new Pipeline("test");
        Bus bus = pipe.getBus();
        bus.connect((Bus.EOS) source -> {
        });

        GCTracker busTracker = new GCTracker(bus);
        GCTracker pipeTracker = new GCTracker(pipe);
        bus = null;
        pipe = null;
        assertTrue(busTracker.waitGC(), "Bus not garbage collected");
        assertTrue(busTracker.waitDestroyed(), "Bus not destroyed");
        assertTrue(pipeTracker.waitGC(), "Pipe not garbage collected");
        assertTrue(pipeTracker.waitDestroyed(), "Pipe not destroyed");
    }
}
