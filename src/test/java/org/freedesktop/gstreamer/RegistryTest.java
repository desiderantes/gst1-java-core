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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author wayne
 */
public class RegistryTest {

    public RegistryTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("RegistryTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void testGet() {
        Registry registry = Registry.get();
        assertNotNull(registry, "Registry.getDefault() returned null");
    }

    @Test
    public void testGetPluginList() {
        final String PLUGIN = "vorbis"; // Use something that is likely to be there
        Registry registry = Registry.get();
        List<Plugin> plugins = registry.getPluginList();
        assertFalse(plugins.isEmpty(), "No plugins found");
        boolean pluginFound = false;
        for (Plugin p : plugins) {
            if (p.getName().equals(PLUGIN)) {
                pluginFound = true;
            }
        }
        assertTrue(pluginFound, PLUGIN + " plugin not found");
    }

    @Test
    public void testGetPluginListFiltered() {
        final String PLUGIN = "vorbis"; // Use something that is likely to be there
        Registry registry = Registry.get();
        final boolean[] filterCalled = {false};
        List<Plugin> plugins = registry.getPluginList(new Registry.PluginFilter() {
            @Override
            public boolean accept(Plugin plugin) {
                filterCalled[0] = true;
                return plugin.getName().equals(PLUGIN);
            }
        });
        assertFalse(plugins.isEmpty(), "No plugins found");
        assertTrue(filterCalled[0], "PluginFilter not called");
        assertEquals(1, plugins.size(), "Plugin list should contain 1 item");
        assertEquals(PLUGIN, plugins.get(0).getName(), PLUGIN + " plugin not found");
    }

    @Test
    public void testGetPluginFeatureListByPlugin() {
        final String PLUGIN = "vorbis"; // Use something that is likely to be there
        final String FEATURE = "vorbisdec";
        Registry registry = Registry.get();
        List<PluginFeature> features = registry.getPluginFeatureListByPlugin(PLUGIN);
        assertFalse(features.isEmpty(), "No plugin features found");
        boolean pluginFound = false;
        for (PluginFeature p : features) {
            if (p.getName().equals(FEATURE)) {
                pluginFound = true;
            }
        }
        assertTrue(pluginFound, PLUGIN + " plugin not found");
    }

    @Test
    public void testFindPluginFeature() {
        PluginFeature f = Registry.get().lookupFeature("decodebin");
        assertNotNull(f);
    }

    @Test
    public void testFindPlugin() {
        Plugin f = Registry.get().findPlugin("playback");
        assertNotNull(f);
    }
}
