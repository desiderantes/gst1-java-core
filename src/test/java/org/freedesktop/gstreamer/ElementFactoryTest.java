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


import org.freedesktop.gstreamer.ElementFactory.ListType;
import org.freedesktop.gstreamer.PluginFeature.Rank;
import org.freedesktop.gstreamer.elements.DecodeBin;
import org.freedesktop.gstreamer.elements.PlayBin;
import org.freedesktop.gstreamer.elements.URIDecodeBin;
import org.junit.jupiter.api.*;

import java.lang.ref.WeakReference;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wayne
 */
@SuppressWarnings("deprecation")
public class ElementFactoryTest {

    public ElementFactoryTest() {
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
    public void testMakeFakesink() {
        Element e = ElementFactory.make("fakesink", "sink");
        assertNotNull(e, "Failed to create fakesink");
    }

    @Test
    public void testMakeFakesrc() {
        Element e = ElementFactory.make("fakesrc", "source");
        assertNotNull(e, "Failed to create fakesrc");
    }

    @Test
    public void testMakeFilesink() {
        Element e = ElementFactory.make("filesink", "sink");
        assertNotNull(e, "Failed to create filesink");
    }

    @Test
    public void testMakeFilesrc() {
        Element e = ElementFactory.make("filesrc", "source");
        assertNotNull(e, "Failed to create filesrc");
    }

    @Test
    public void testMakeBin() {
        Element e = ElementFactory.make("bin", "bin");
        assertNotNull(e, "Failed to create bin");
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
    }

    @Test
    public void testMakePipeline() {
        Element e = ElementFactory.make(Pipeline.GST_NAME, "bin");
        assertNotNull(e, "Failed to create " + Pipeline.GST_NAME);
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof Pipeline, "Element not a subclass of Pipeline");
    }

    @Test
    public void testMakePlaybin() {
        Element e = ElementFactory.make(PlayBin.GST_NAME, "bin");
        assertNotNull(e, "Failed to create " + PlayBin.GST_NAME);
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof Pipeline, "Element not a subclass of Pipeline");
        assertTrue(e instanceof PlayBin, "Element not a subclass of PlayBin");
    }

    @Test
    public void testMakeDecodeBin() {
        Element e = ElementFactory.make(DecodeBin.GST_NAME, "bin");
        assertNotNull(e, "Failed to create " + DecodeBin.GST_NAME);
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof DecodeBin, "Element not a subclass of DecodeBin");
    }

    @Test
    public void testMakeURIDecodeBin() {
        Element e = ElementFactory.make(URIDecodeBin.GST_NAME, "bin");
        assertNotNull(e, "Failed to create " + URIDecodeBin.GST_NAME);
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof URIDecodeBin, "Element not a subclass of DecodeBin");
    }

    @Test
    public void testCreateFakesrc() {
        ElementFactory factory = ElementFactory.find("fakesrc");
        assertNotNull(factory, "Could not locate fakesrc factory");
        Element e = factory.create("source");
        assertNotNull(e, "Failed to create fakesrc");
    }

    @Test
    public void testCreateBin() {
        ElementFactory factory = ElementFactory.find("bin");
        assertNotNull(factory, "Could not locate bin factory");
        Element e = factory.create("bin");
        assertNotNull(e, "Failed to create bin");
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
    }

    @Test
    public void testCreatePipeline() {
        ElementFactory factory = ElementFactory.find("pipeline");
        assertNotNull(factory, "Could not locate pipeline factory");
        Element e = factory.create("bin");
        assertNotNull(e, "Failed to create pipeline");
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof Pipeline, "Element not a subclass of Pipeline");
    }

    @Test
    public void testCreatePlaybin() {
        ElementFactory factory = ElementFactory.find("playbin");
        assertNotNull(factory, "Could not locate pipeline factory");
        System.out.println("PlayBin factory name=" + factory.getName());
        Element e = factory.create("bin");
        assertNotNull(e, "Failed to create playbin");
        assertTrue(e instanceof Bin, "Element not a subclass of Bin");
        assertTrue(e instanceof Pipeline, "Element not a subclass of Pipeline");
        assertTrue(e instanceof PlayBin, "Element not a subclass of PlayBin");
    }

    @Test
    public void testGarbageCollection() throws Throwable {
        ElementFactory factory = ElementFactory.find("fakesrc");
        assertNotNull(factory, "Could not locate fakesrc factory");
        WeakReference<ElementFactory> ref = new WeakReference<ElementFactory>(factory);
        factory = null;
        assertTrue(GCTracker.waitGC(ref), "Factory not garbage collected");
    }

    @Test
    public void testMakeGarbageCollection() throws Throwable {
        Element e = ElementFactory.make("fakesrc", "test");
        WeakReference<Element> ref = new WeakReference<Element>(e);
        e = null;
        assertTrue(GCTracker.waitGC(ref), "Element not garbage collected");

    }

    @Test
    public void testCreateGarbageCollection() throws Throwable {
        ElementFactory factory = ElementFactory.find("fakesrc");
        assertNotNull(factory, "Could not locate fakesrc factory");
        Element e = factory.create("bin");
        WeakReference<Element> ref = new WeakReference<Element>(e);
        e = null;
        assertTrue(GCTracker.waitGC(ref), "Element not garbage collected");
    }

    @Test
    public void getStaticPadTemplates() {
        ElementFactory f = ElementFactory.find("fakesink");
        List<StaticPadTemplate> templates = f.getStaticPadTemplates();
        assertFalse(templates.isEmpty(), "No static pad templates found");
        StaticPadTemplate t = templates.get(0);
        assertEquals("sink", t.getName(), "Not a sink");
        assertEquals(PadDirection.SINK, t.getDirection(), "Not a sink");
    }

    @Test
    public void listGetElement() {
        List<ElementFactory> list = ElementFactory.listGetElements(ListType.ANY,
                Rank.NONE);
        assertNotNull(list, "List of factories is null");
        assertFalse(list.isEmpty(), "No factories found");
//        System.out.println("Factories >>>");
//        for (ElementFactory fact : list) {
//            System.out.println(fact.getName());
//        }
//        System.out.println("<<<");
    }

//    @Test
//    public void filterList() {
//        List<ElementFactory> list = ElementFactory.listGetElements(ListType.ENCODER,
//                Rank.NONE);
//        assertNotNull("List of factories is null", list);
//        assertTrue("No factories found", !list.isEmpty());
//        List<ElementFactory> filterList = ElementFactory.listFilter(list, new Caps("video/x-h263"),
//                PadDirection.SRC, false);
//
//        assertNotNull("List of factories is null", filterList);
//        assertTrue("No factories found", !filterList.isEmpty());
////        System.out.println("Filtered factories >>>");
////        for (ElementFactory fact : filterList) {
////            System.out.println(fact.getName());
////        }
////        System.out.println("<<<");
//    }

    @Test
    public void filterList2() {
        List<ElementFactory> list = ElementFactory.listGetElementsFilter(ListType.ENCODER, Rank.NONE, new Caps("video/x-h263"),
                PadDirection.SRC, false);
        assertNotNull(list, "List of factories is null");
        assertFalse(list.isEmpty(), "No factories found");

//        System.out.println("Factories >>>");
//        for (ElementFactory fact : list) {
//            System.out.println(fact.getName());
//        }
//        System.out.println("<<<");
    }

    @Test
    public void testMetaData() {
        ElementFactory f = ElementFactory.find("fakesink");
        String klass = f.getKlass();
        String longName = f.getLongName();
        String description = f.getDescription();
        String author = f.getAuthor();
        assertNotNull(klass, "Klass is null");
        assertNotNull(longName, "Long name is null");
        assertNotNull(description, "Description is null");
        assertNotNull(author, "Author is null");
        System.out.println("FakeSink MetaData");
        System.out.println("Klass : " + f.getKlass());
        System.out.println("Long Name : " + f.getLongName());
        System.out.println("Description : " + f.getDescription());
        System.out.println("Author : " + f.getAuthor());
    }
}
