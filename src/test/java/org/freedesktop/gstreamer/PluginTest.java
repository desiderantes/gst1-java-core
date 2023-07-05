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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class PluginTest {
    private static Plugin playbackPlugin;

    public PluginTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("PluginTest");
        playbackPlugin = Plugin.loadByName("playback");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void testLoad_String() {
        assertNotNull(playbackPlugin);
    }

    @Test
    public void testGetName() {
        assertEquals("playback", playbackPlugin.getName());
    }

    @Test
    public void testGetDescription() {
        assertEquals("various playback elements", playbackPlugin.getDescription());
    }

    @Test
    public void testGetFilename() {
        assertTrue(playbackPlugin.getFilename().contains("gstplayback"));
    }

    @Test
    public void testGetVersion() {
        assertTrue(playbackPlugin.getVersion().matches("^(?:\\d+\\.)*\\d+$"));
    }

    @Test
    public void testGetLicense() {
        assertEquals("LGPL", playbackPlugin.getLicense());
    }

    @Test
    public void testGetSource() {
        assertEquals("gst-plugins-base", playbackPlugin.getSource());
    }

    @Test
    public void testGetPackage() {
        String pkg = playbackPlugin.getPackage();
        assertTrue(pkg.contains("GStreamer Base")
                || pkg.contains("Gentoo GStreamer"));
    }

    @Test
    public void testGetOrigin() {
        assertTrue(playbackPlugin.getOrigin().length() > 0);
    }

    @Test
    public void testGetReleaseDateString() {
        assertTrue(playbackPlugin.getReleaseDateString().matches(".*\\d{4}-\\d{2}-\\d{2}.*"));
    }

    public void testIsLoaded() {
        assertTrue(playbackPlugin.isLoaded());
    }
}
