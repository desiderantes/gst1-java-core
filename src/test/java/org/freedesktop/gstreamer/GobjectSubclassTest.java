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

import com.sun.jna.Pointer;
import org.freedesktop.gstreamer.lowlevel.BaseSrcAPI;
import org.freedesktop.gstreamer.lowlevel.GObjectAPI;
import org.freedesktop.gstreamer.lowlevel.GObjectAPI.GClassInitFunc;
import org.freedesktop.gstreamer.lowlevel.GObjectAPI.GInstanceInitFunc;
import org.freedesktop.gstreamer.lowlevel.GType;
import org.junit.jupiter.api.*;

import static org.freedesktop.gstreamer.lowlevel.GObjectAPI.GOBJECT_API;
import static org.freedesktop.gstreamer.lowlevel.GstPadTemplateAPI.GSTPADTEMPLATE_API;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author wayne
 */
public class GobjectSubclassTest {

    public GobjectSubclassTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
//        GObjectAPI.gobj.g_type_init_with_debug_flags(1 << 0);
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
    public void registerNewGObjectClass() throws Exception {
        final PadTemplate template = new PadTemplate("src", PadDirection.SRC,
                Caps.anyCaps());
        final boolean[] classInitCalled = {false};
        final GClassInitFunc classInit = new GClassInitFunc() {

            public void callback(Pointer g_class, Pointer class_data) {
                classInitCalled[0] = true;
            }
        };
        final GObjectAPI.GBaseInitFunc baseInit = new GObjectAPI.GBaseInitFunc() {

            public void callback(Pointer g_class) {
                GSTPADTEMPLATE_API.gst_element_class_add_pad_template(g_class, template);
            }
        };
        final boolean[] instanceInitCalled = {false};
        final GInstanceInitFunc instanceInit = (instance, g_class) -> instanceInitCalled[0] = true;
        final String name = "NewTestClass";

        GObjectAPI.GTypeInfo info = new GObjectAPI.GTypeInfo();
        info.clear();
        info.class_init = classInit;
        info.instance_init = instanceInit;
        info.class_size = (short) new BaseSrcAPI.GstBaseSrcClass().size();
        info.instance_size = (short) new BaseSrcAPI.GstBaseSrcStruct().size();
        info.class_size = 1024;
        info.base_init = baseInit;
        info.instance_size = 1024;

        GType type = GOBJECT_API.g_type_register_static(BaseSrcAPI.BASESRC_API.gst_base_src_get_type(),
                name, info, 0);
        System.out.println("New type=" + type);
        assertEquals(name, GOBJECT_API.g_type_name(type), "Name incorrect");
        assertEquals(type, GOBJECT_API.g_type_from_name(name), "Cannot locate type by name");

        //Pointer instance = GOBJECT_API.g_type_create_instance(type);
        GOBJECT_API.g_object_new(type);

    }
}
