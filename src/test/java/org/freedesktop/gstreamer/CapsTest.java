/*
 * Copyright (c) 2007 Wayne Meissner
 * Copyright (C) 2005 Andy Wingo <wingo@pobox.com>
 * Copyright (C) <2005> Thomas Vander Stichele <thomas at apestaart dot org>
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


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


/**
 * Unit test for GstCaps
 */
public class CapsTest {

    private static final String non_simple_caps_string =
            "video/x-raw, format=I420, framerate=(fraction)[ 1/100, 100 ], "
                    + "width=(int)[ 16, 4096 ], height=(int)[ 16, 4096 ]; video/x-raw, "
                    + "format=YUY2, framerate=(fraction)[ 1/100, 100 ], width=(int)[ 16, 4096 ], "
                    + "height=(int)[ 16, 4096 ]; video/x-raw, format=RGB, bpp=(int)8, depth=(int)8, "
                    + "endianness=(int)1234, framerate=(fraction)[ 1/100, 100 ], width=(int)[ 16, 4096 ], "
                    + "height=(int)[ 16, 4096 ]; video/x-raw, "
                    + "format={ I420, YUY2, YV12 }, width=(int)[ 16, 4096 ], "
                    + "height=(int)[ 16, 4096 ], framerate=(fraction)[ 1/100, 100 ]";

    public CapsTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("CapsTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void capsMerge() {
        Caps c1 = new Caps("video/x-raw, format=RGB, bpp=32, depth=24");
        Caps c2 = new Caps("video/x-raw, format=RGB, width=640, height=480");
        Caps c3 = Caps.merge(c1, c2);
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "merged caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "merged caps not invalidated");

        boolean widthFound = false, heightFound = false;
        for (int i = 0; i < c3.size(); ++i) {
            Structure s = c3.getStructure(i);
            if (s.hasIntField("width")) {
                widthFound = true;
            }
            if (s.hasIntField("height")) {
                heightFound = true;
            }
        }
        assertTrue(widthFound, "width not appended");
        assertTrue(heightFound, "height not appended");
        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void capsAppend() {
        Caps c1 = new Caps("video/x-raw, format=RGB, bpp=32, depth=24");
        Caps c2 = new Caps("video/x-raw, format=RGB, width=640, height=480");
        c1.append(c2);
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        boolean widthFound = false, heightFound = false;
        for (int i = 0; i < c1.size(); ++i) {
            Structure s = c1.getStructure(i);
            if (s.hasIntField("width")) {
                widthFound = true;
            }
            if (s.hasIntField("height")) {
                heightFound = true;
            }
        }
        assertTrue(widthFound, "width not appended");
        assertTrue(heightFound, "height not appended");
        // Verify reference count before dispose
        assertEquals(1, c1.getRefCount());
        // Force cleanup to bring out any memory bugs
        c1.dispose();
    }

    @Test
    public void simplify() {
        Caps c1 = new Caps(non_simple_caps_string);
        assertNotNull(c1, "Caps not created");
        Caps c2 = c1.simplify();
        assertNotNull(c2, "Simplify returned null");
        /* check simplified caps, should be:
         *
         * video/x-raw, format=RGB, bpp=(int)8, depth=(int)8, endianness=(int)1234,
         *     framerate=(fraction)[ 1/100, 100 ], width=(int)[ 16, 4096 ],
         *     height=(int)[ 16, 4096 ];
         * video/x-raw, format={ YV12, YUY2, I420 },
         *     width=(int)[ 16, 4096 ], height=(int)[ 16, 4096 ],
         *     framerate=(fraction)[ 1/100, 100 ]
         */
        assertEquals(2, c2.size(), "Caps not simplified to 2 structures");
        Structure s1 = c2.getStructure(0);
        assertNotNull(s1, "Caps.getStructure(0) failed");
        Structure s2 = c2.getStructure(1);
        assertNotNull(s2, "Caps.getStructure(1) failed");
        if (!s1.hasName("video/x-raw")) {
            Structure tmp = s1;
            s1 = s2;
            s2 = tmp;
        }
        assertTrue(s1.hasName("video/x-raw"), "Could not locate video/x-raw structure");
        assertEquals(8, s1.getInteger("bpp"), "bpp not retrieved");
        assertEquals(8, s1.getInteger("depth"), "depth not retrieved");

        assertTrue(s2.hasName("video/x-raw"), "Could not locate video/x-raw structure");

        // Verify reference count before dispose
        assertEquals(1, c1.getRefCount());
        assertEquals(1, c2.getRefCount());
        // Force cleanup to bring out any memory bugs
        c1.dispose();
        c2.dispose();
    }

    @Test
    public void truncate() {

        Caps c1 = Caps.fromString(non_simple_caps_string);
        assertNotNull(c1, "Caps.fromString failed");
        assertEquals(4, c1.size(), "Incorrect number of structures in caps");
        Caps c2 = c1.truncate();
        assertEquals(1, c2.size(), "Caps not truncated");
        assertEquals(4, c1.size(), "Original caps untouched");
        // Verify reference count before dispose
        assertEquals(1, c1.getRefCount());
        assertEquals(1, c2.getRefCount());
        // Force cleanup to bring out any memory bugs
        c1.dispose();
        c2.dispose();
    }

    @Test
    public void mergeANYAndSpecific() {
        /* ANY + specific = ANY */
        Caps c1 = Caps.anyCaps();
        Caps c2 = Caps.fromString("audio/x-raw,rate=44100");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(0, c3.size(), "Too many structures in merged caps");
        assertTrue(c3.isAny(), "Merged caps should be ANY");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSpecificAndANY() {
        /* specific + ANY = ANY */
        Caps c1 = Caps.fromString("audio/x-raw,rate=44100");
        Caps c2 = Caps.anyCaps();
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(0, c3.size(), "Too many structures in merged caps");
        assertTrue(c3.isAny(), "Merged caps should be ANY");
        // Verify that the victim caps were invalidated and cannot be used.
        try {
            c1.toString();
            fail("appended caps not invalidated");
        } catch (IllegalStateException ex) {
        }
        try {
            c2.toString();
            fail("appended caps not invalidated");
        } catch (IllegalStateException ex) {
        }
        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSpecificAndEMPTY() {
        /* specific + EMPTY = specific */
        Caps c1 = Caps.fromString("audio/x-raw,rate=44100");
        Caps c2 = Caps.emptyCaps();
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(1, c3.size(), "Wrong number of structures in merged structure");
        assertFalse(c3.isEmpty(), "Merged caps should not be empty");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeEMPTYAndSpecific() {
        /* EMPTY + specific = specific */
        Caps c1 = Caps.emptyCaps();
        Caps c2 = Caps.fromString("audio/x-raw,rate=44100");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(1, c3.size(), "Merged Caps structure count incorrect");
        assertFalse(c3.isEmpty(), "Merged caps should not be empty");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSame() {
        /* this is the same */
        Caps c1 = Caps.fromString("audio/x-raw,rate=44100,channels=1");
        Caps c2 = Caps.fromString("audio/x-raw,rate=44100,channels=1");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(1, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSameWithDifferentOrder() {
        /* and so is this */
        Caps c1 = Caps.fromString("audio/x-raw,rate=44100,channels=1");
        Caps c2 = Caps.fromString("audio/x-raw,channels=1,rate=44100");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(1, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSameWithBufferData() {
        Caps c1 = Caps.fromString("video/x-foo, data=(buffer)AA");
        Caps c2 = Caps.fromString("video/x-foo, data=(buffer)AABB");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(2, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSameWithBufferDataReversed() {
        Caps c1 = Caps.fromString("video/x-foo, data=(buffer)AABB");
        Caps c2 = Caps.fromString("video/x-foo, data=(buffer)AA");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(2, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSameWithBufferDataSame() {
        Caps c1 = Caps.fromString("video/x-foo, data=(buffer)AA");
        Caps c2 = Caps.fromString("video/x-foo, data=(buffer)AA");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(1, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeDifferentWithBufferDataSame() {
        Caps c1 = Caps.fromString("video/x-foo, data=(buffer)AA");
        Caps c2 = Caps.fromString("video/x-bar, data=(buffer)AA");
        Caps c3 = Caps.merge(c1, c2);
        assertEquals(2, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void mergeSubset() {
        /* the 2nd is already covered */
        Caps c2 = Caps.fromString("audio/x-raw,channels=[1,2]");
        Caps c1 = Caps.fromString("audio/x-raw,channels=1");
        Caps c3 = Caps.merge(c1, c2).simplify();
        System.out.println(c3.toString());
        assertEquals(1, c3.size(), "Merged Caps structure count incorrect");
        // Verify that the victim caps were invalidated and cannot be used.
        assertThrows(IllegalStateException.class, () -> c1.toString(), "appended caps not invalidated");

        assertThrows(IllegalStateException.class, () -> c2.toString(), "appended caps not invalidated");

        // Verify reference count before dispose
        assertEquals(1, c3.getRefCount());
        // Force cleanup to bring out any memory bugs
        c3.dispose();
    }

    @Test
    public void intersect() {
        Caps c2 = Caps.fromString("video/x-raw,format=I420,width=20");
        Caps c1 = Caps.fromString("video/x-raw,format=I420,height=30");

        Caps ci1 = c2.intersect(c1);
        assertEquals(1, ci1.size(), "Intersected Caps structure count incorrect");

        Structure s = ci1.getStructure(0);
        assertTrue(s.hasName("video/x-raw"), "Incorrect name on intersected structure");
        assertTrue(s.hasField("format"), "Intersected structure does not have 'format' field");
        assertTrue(s.hasField("width"), "Intersected structure does not have 'width' field");
        assertTrue(s.hasField("height"), "Intersected structure does not have 'height' field");

        /* with changed order */
        Caps ci2 = c1.intersect(c2);
        assertEquals(1, ci2.size(), "Intersected Caps structure count incorrect");
        s = ci2.getStructure(0);
        assertTrue(s.hasName("video/x-raw"), "Incorrect name on intersected structure");
        assertTrue(s.hasField("format"), "Intersected structure does not have 'format' field");
        assertTrue(s.hasField("width"), "Intersected structure does not have 'width' field");
        assertTrue(s.hasField("height"), "Intersected structure does not have 'height' field");

        assertTrue(ci1.isEqual(ci2), "Intersection should be same in both directions");
        // Force cleanup to bring out any memory bugs
        c2.dispose();
        c1.dispose();
        ci1.dispose();
        ci2.dispose();
    }

    @Test
    public void intersectUnspecified() {
        /* field not specified = any value possible, so the intersection
         * should keep fields which are only part of one set of caps */
        Caps c2 = Caps.fromString("video/x-raw,format=I420,width=20");
        Caps c1 = Caps.fromString("video/x-raw,format=I420");

        Caps ci1 = c2.intersect(c1);
        assertEquals(1, ci1.size(), "Intersected Caps structure count incorrect");
        Structure s = ci1.getStructure(0);
        assertTrue(s.hasName("video/x-raw"), "Incorrect name on intersected structure");
        assertTrue(s.hasField("format"), "Intersected structure does not have 'format' field");
        assertTrue(s.hasField("width"), "Intersected structure does not have 'width' field");

        /* with changed order */

        Caps ci2 = c1.intersect(c2);
        assertEquals(1, ci2.size(), "Intersected Caps structure count incorrect");
        s = ci2.getStructure(0);
        assertTrue(s.hasName("video/x-raw"), "Incorrect name on intersected structure");
        assertTrue(s.hasField("format"), "Intersected structure does not have 'format' field");
        assertTrue(s.hasField("width"), "Intersected structure does not have 'width' field");
        assertTrue(ci1.isEqual(ci2), "Intersection should be same in both directions");
        // Force cleanup to bring out any memory bugs
        c2.dispose();
        c1.dispose();
        ci1.dispose();
        ci2.dispose();
    }

    @Test
    public void intersectUnequal() {
        Caps c2 = Caps.fromString("video/x-raw,format=I420,width=20");
        Caps c1 = Caps.fromString("video/x-raw,format=I420,width=30");

        Caps ci1 = c2.intersect(c1);
        assertTrue(ci1.isEmpty(), "Intersection of unequal caps should be empty");
        /* with changed order */
        Caps ci2 = c1.intersect(c2);
        assertTrue(ci1.isEmpty(), "Intersection of unequal caps should be empty");
        assertTrue(ci1.isEqual(ci2), "Intersection should be same in both directions");
        // Force cleanup to bring out any memory bugs
        c2.dispose();
        c1.dispose();
        ci1.dispose();
        ci2.dispose();
    }

    @Test
    public void intersectDifferentType() {
        Caps c2 = Caps.fromString("video/x-raw,format=I420,width=20");
        Caps c1 = Caps.fromString("video/x-raw,format=RGB,width=20");

        Caps ci1 = c2.intersect(c1);
        assertTrue(ci1.isEmpty(), "Intersection of different type caps should be empty");

        /* with changed order */
        Caps ci2 = c1.intersect(c2);
        assertTrue(ci1.isEmpty(), "Intersection of different type caps should be empty");
        assertTrue(ci1.isEqual(ci2), "Intersection should be same in both directions");
        // Force cleanup to bring out any memory bugs
        c2.dispose();
        c1.dispose();
        ci1.dispose();
        ci2.dispose();
    }

}