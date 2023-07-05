/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author inx
 */
public class PadTemplateTest {

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
    public void padTemplate()
            throws Exception {
        Element src = ElementFactory.make("fakesrc", "src");
        Element sink = ElementFactory.make("fakesink", "sink");
        Pad srcPad = src.getStaticPad("src");
        Pad sinkPad = sink.getStaticPad("sink");
        PadTemplate template;

        template = srcPad.getTemplate();
        assertEquals(template.getTemplateName(), "src", "wrong name!");
        assertEquals(template.getDirection(), PadDirection.SRC, "wrong direction!");
        assertEquals(template.getPresence(), PadPresence.ALWAYS, "wrong presence!");

        template = sinkPad.getTemplate();
        assertEquals(template.getTemplateName(), "sink", "wrong name!");
        assertEquals(template.getDirection(), PadDirection.SINK, "wrong direction!");
        assertEquals(template.getPresence(), PadPresence.ALWAYS, "wrong presence!");
    }
}
