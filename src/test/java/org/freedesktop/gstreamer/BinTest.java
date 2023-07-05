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

import org.freedesktop.gstreamer.glib.GError;
import org.freedesktop.gstreamer.lowlevel.GstBinAPI;
import org.freedesktop.gstreamer.lowlevel.GstPipelineAPI;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wayne
 */
public class BinTest {
    public BinTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("BinTest");
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
    public void testGetElements() {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);
        List<Element> elements = bin.getElements();
        assertFalse(elements.isEmpty(), "Bin returned empty list from getElements");
        assertTrue(elements.contains(e1), "Element list does not contain e1");
        assertTrue(elements.contains(e2), "Element list does not contain e2");
    }

    @Test
    public void testGetSinks() {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);
        List<Element> elements = bin.getSinks();
        assertFalse(elements.isEmpty(), "Bin returned empty list from getElements");
        assertTrue(elements.contains(e2), "Element list does not contain sink");
    }

    @Test
    public void testGetSources() {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);
        List<Element> elements = bin.getSources();
        assertFalse(elements.isEmpty(), "Bin returned empty list from getElements");
        assertTrue(elements.contains(e1), "Element list does not contain source");
    }

    @Test
    public void testGetElementByName() {
        Bin bin = new Bin("test");
        Element e1 = ElementFactory.make("fakesrc", "source");
        Element e2 = ElementFactory.make("fakesink", "sink");
        bin.addMany(e1, e2);

        assertEquals(e1, bin.getElementByName("source"), "source not returned");
        assertEquals(e2, bin.getElementByName("sink"), "sink not returned");
    }

    @Test
    public void testElementAddedCallback() {
        Bin bin = new Bin("test");
        final Element e1 = ElementFactory.make("fakesrc", "source");
        final Element e2 = ElementFactory.make("fakesink", "sink");
        final AtomicInteger added = new AtomicInteger(0);

        bin.connect((Bin.ELEMENT_ADDED) (bin1, elem) -> {
            if (elem == e1 || elem == e2) {
                added.incrementAndGet();
            }
        });
        bin.addMany(e1, e2);

        assertEquals(2, added.get(), "Callback not called");
    }

    @Test
    public void testElementRemovedCallback() {
        Bin bin = new Bin("test");
        final Element e1 = ElementFactory.make("fakesrc", "source");
        final Element e2 = ElementFactory.make("fakesink", "sink");
        final AtomicInteger removed = new AtomicInteger(0);

        bin.connect((Bin.ELEMENT_ADDED) (bin1, elem) -> {
            if (elem == e1 || elem == e2) {
                removed.incrementAndGet();
            }
        });
        bin.addMany(e1, e2);

        assertEquals(2, removed.get(), "Callback not called");
    }

    @Test
    public void addLinked()
            throws PadLinkException {
        /* adding an element with linked pads to a bin unlinks the pads */
        Pipeline pipeline = new Pipeline((String) null);
        assertNotNull(pipeline, "Could not create pipeline");

        Element src = ElementFactory.make("fakesrc", null);
        assertNotNull(src, "Could not create fakesrc");
        Element sink = ElementFactory.make("fakesink", null);
        assertNotNull(sink, "Could not create fakesink");

        Pad srcpad = src.getStaticPad("src");
        assertNotNull(srcpad, "Could not get src pad");
        Pad sinkpad = sink.getStaticPad("sink");
        assertNotNull(sinkpad, "Could not get sink pad");

        srcpad.link(sinkpad);

        /* pads are linked now */
        assertTrue(srcpad.isLinked(), "srcpad not linked");
        assertTrue(sinkpad.isLinked(), "sinkpad not linked");

        /* adding element to bin voids hierarchy so pads are unlinked */
        pipeline.add(src);

        /* check if pads really are unlinked */
        assertFalse(srcpad.isLinked(), "srcpad is still linked after being added to bin");
        assertFalse(sinkpad.isLinked(), "sinkpad is still linked after being added to bin");

        /* cannot link pads in wrong hierarchy */
        assertThrows(PadLinkException.class, () ->
                        srcpad.link(sinkpad)
                , "Should not be able to link pads in different hierarchy");

        /* adding other element to bin as well */
        pipeline.add(sink);

        /* now we can link again */
        srcpad.link(sinkpad);

        /* check if pads really are linked */
        assertTrue(srcpad.isLinked(), "srcpad not linked");
        assertTrue(sinkpad.isLinked(), "sinkpad not linked");

        // Force disposal to flush out any refcounting bugs.
        pipeline.dispose();
        src.dispose();
        sink.dispose();
        srcpad.dispose();
        sinkpad.dispose();
    }

    @Test
    public void addSelf() {
        Bin bin = new Bin("");
        // Enable the line below once we know how to avoid gstreamer spitting out warnings
        //assertFalse("Should not be able to add bin to itself", bin.add(bin));
        bin.dispose();
    }

    // This test doesn't work correctly on older gstreamer?
    //@Test 
    public void iterateSorted() {
        Pipeline pipeline = GstPipelineAPI.GSTPIPELINE_API.gst_pipeline_new(null);
        assertNotNull(pipeline, "Failed to create Pipeline");
        Bin bin = GstBinAPI.GSTBIN_API.gst_bin_new(null);
        assertNotNull(bin, "Failed to create bin");

        Element src = ElementFactory.make("fakesrc", null);
        assertNotNull(src, "Failed to create fakesrc");

        Element tee = ElementFactory.make("tee", null);
        assertNotNull(tee, "Failed to create tee");

        Element sink1 = ElementFactory.make("fakesink", null);
        assertNotNull(sink1, "Failed to create fakesink");

        bin.addMany(src, tee, sink1);
        assertTrue(src.link(tee), "Could not link fakesrc to tee");
        assertTrue(tee.link(sink1), "Could not link tee to fakesink");

        Element identity = ElementFactory.make("identity", null);
        assertNotNull(identity, "Failed to create identity");


        Element sink2 = ElementFactory.make("fakesink", null);
        assertNotNull(sink2, "Failed to create fakesink");
        pipeline.addMany(bin, identity, sink2);
//  gst_bin_add_many (GST_BIN (pipeline), bin, identity, sink2, NULL);
        assertTrue(tee.link(identity), "Could not link tee to identity");
        assertTrue(identity.link(sink2), "Could not link identity to second fakesink");
        Iterator<Element> it = pipeline.getElementsSorted().iterator();

        assertEquals(sink2, it.next(), "First sorted element should be sink2");
        assertEquals(identity, it.next(), "Second sorted element should be identity");
        assertEquals(bin, it.next(), "Third sorted element should be bin");
        pipeline.dispose();
    }

    @Test
    public void testParseBin() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", false, errors);
        assertNotNull(bin, "Bin not created");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinElementCount() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", false, errors);
        assertEquals(2, bin.getElements().size(), "Number of elements in pipeline incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinSrcElement() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", false, errors);
        assertEquals("fakesrc", bin.getSources().get(0).getFactory().getName(), "First element not a fakesrc");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinSinkElement() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", false, errors);
        assertEquals("fakesink", bin.getSinks().get(0).getFactory().getName(), "First element not a fakesink");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinDisabledGhostPadsForSource() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc", false, errors);
        assertEquals(0, bin.getSrcPads().size(), "Number of src pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinDisabledGhostPadsForSink() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesink", false, errors);
        assertEquals(0, bin.getSinkPads().size(), "Number of sink pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinEnabledGhostPadsForSource() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc", true, errors);
        assertEquals(1, bin.getSrcPads().size(), "Number of src pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinEnabledGhostPadsForSink() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesink", true, errors);
        assertEquals(1, bin.getSinkPads().size(), "Number of sink pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinEnabledGhostPadsForSourceWithNoUsablePads() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", true, errors);
        assertEquals(0, bin.getSrcPads().size(), "Number of src pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinEnabledGhostPadsForSinkWithNoUsablePads() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", true, errors);
        assertEquals(0, bin.getSinkPads().size(), "Number of sink pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }

    @Test
    public void testParseBinEnabledGhostPadsWithNoUsablePads() {
        ArrayList<GError> errors = new ArrayList<GError>();
        Bin bin = Gst.parseBinFromDescription("fakesrc ! fakesink", true, errors);
        assertEquals(0, bin.getPads().size(), "Number of pads incorrect");
        assertEquals(0, errors.size(), "parseBinFromDescription with error!");
    }
}
