/*
 * Copyright (c) 2019 Neil C Smith
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

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
public class InitTest {

    public InitTest() {

    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @Test
    public void testInit() {
        Version available = Gst.getVersion();
        Version notAvailable = Version.of(available.getMajor(), available.getMinor() + 2);

        assertThrows(GstException.class, () -> Gst.init(notAvailable), "Version check exception not thrown!");

        String[] args = Gst.init(available, "InitTest", "--gst-plugin-spew");
        assertEquals(0, args.length);

        assertTrue(Gst.testVersion(available.getMajor(), available.getMinor()));
        assertTrue(Gst.testVersion(available.getMajor(), available.getMinor() - 2));
        assertFalse(Gst.testVersion(notAvailable.getMajor(), notAvailable.getMinor()));

        Gst.deinit();
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

}
