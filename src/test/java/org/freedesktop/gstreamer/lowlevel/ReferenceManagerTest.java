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

package org.freedesktop.gstreamer.lowlevel;


import org.freedesktop.gstreamer.Caps;
import org.freedesktop.gstreamer.Gst;
import org.junit.jupiter.api.*;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class ReferenceManagerTest {

    public ReferenceManagerTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("ReferenceManagerTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    public boolean waitGC(Reference<? extends Object> ref) throws InterruptedException {
        System.gc();
        for (int i = 0; ref.get() != null && i < 20; ++i) {
            Thread.sleep(10);
            System.gc();
        }
        return ref.get() == null;
    }

    @Test
    public void testReference() throws Exception {
        Object ref = new Object();
        Caps target = new Caps("video/x-raw");
        ReferenceManager.addKeepAliveReference(ref, target);
        WeakReference<Object> targetRef = new WeakReference<Object>(target);
        target = null;
        assertFalse(waitGC(targetRef), "target collected prematurely");
        ref = null;
        assertTrue(waitGC(targetRef), "target not collected when ref is collected");
    }

    @Test
    public void testMultipleReferences() throws Exception {
        Object ref1 = new Object();
        Object ref2 = new Object();
        Caps target = new Caps("video/x-raw");
        ReferenceManager.addKeepAliveReference(ref1, target);
        ReferenceManager.addKeepAliveReference(ref2, target);
        WeakReference<Object> targetRef = new WeakReference<Object>(target);
        target = null;
        assertFalse(waitGC(targetRef), "target collected prematurely");
        ref1 = null;
        assertFalse(waitGC(targetRef), "target collected after only one ref disposed");
        ref2 = null;
        assertTrue(waitGC(targetRef), "target not collected when ref is dispose");
    }
}