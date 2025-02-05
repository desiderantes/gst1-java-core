/*
 * Copyright (c) 2016 Christophe Lafolet
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
import org.freedesktop.gstreamer.lowlevel.GObjectPtr;
import org.freedesktop.gstreamer.lowlevel.GType;
import org.freedesktop.gstreamer.lowlevel.GstTypes;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GstTypesTest {

    public GstTypesTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("GstTypesTest");
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

    @Test
    public void registeredClassTest() {
        // check a registered class
        GType elementType = GType.valueOf(Element.GTYPE_NAME);
        assertEquals(Element.class, GstTypes.classFor(elementType));
        assertEquals(elementType, GstTypes.typeFor(Element.class));
    }

    @Test
    public void unregisteredClassTest() {
        GType elementType = GType.valueOf(Element.GTYPE_NAME);
        // check an unregistered class which derived from Element
        Element anElement = ElementFactory.make("avidemux", "avidemux");
//    	assertEquals(Element.class, GstTypes.classFor(anElement.getType()));
        assertEquals(Element.class, GstTypes.classFor(
                Natives.getPointer(anElement)
                        .as(GObjectPtr.class, GObjectPtr::new).getGType()));

        // verify GType has not changed for Element.class
        assertEquals(elementType, GstTypes.typeFor(Element.class));
    }
}

