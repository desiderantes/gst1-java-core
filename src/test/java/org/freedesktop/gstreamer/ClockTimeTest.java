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

package org.freedesktop.gstreamer;

import org.junit.jupiter.api.*;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author wayne
 */
public class ClockTimeTest {

    public ClockTimeTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void toSeconds() {
        final long TIME = TimeUnit.SECONDS.toNanos(0xdeadbeef);
        assertEquals(TimeUnit.NANOSECONDS.toSeconds(TIME), ClockTime.toSeconds(TIME), "toSeconds returned incorrect value");
    }

    @Test
    public void toMillis() {
        final long TIME = TimeUnit.SECONDS.toNanos(0xdeadbeef);
        assertEquals(TimeUnit.NANOSECONDS.toMillis(TIME), ClockTime.toMillis(TIME), "toMillis returned incorrect value");
    }

    @Test
    public void toMicros() {
        final long TIME = TimeUnit.SECONDS.toNanos(0xdeadbeef);
        assertEquals(TimeUnit.NANOSECONDS.toMicros(TIME), ClockTime.toMicros(TIME), "toMillis returned incorrect value");
    }

    @Test
    public void toStringRepresentation() {
        long hours = 3;
        long minutes = 27;
        long seconds = 13;
        long time = TimeUnit.HOURS.toNanos(hours) +
                TimeUnit.MINUTES.toNanos(minutes) +
                TimeUnit.SECONDS.toNanos(seconds);
        assertEquals("03:27:13", ClockTime.toString(time), "ClockTime.toString() incorrect");
    }
//    @Test public void toNanos() {
//        final long TIME = TimeUnit.SECONDS.toNanos(0xdeadbeef);
//        ClockTime time = ClockTime.valueOf(TIME, TimeUnit.NANOSECONDS);
//        assertEquals("toNanos returned incorrect value", 
//                TimeUnit.NANOSECONDS.toNanos(TIME), time.toNanos());
//        assertEquals("convertTo returned incorrect value", 
//                TimeUnit.NANOSECONDS.toNanos(TIME), time.convertTo(TimeUnit.NANOSECONDS));
//    }
//    @Test public void compareTo() {
//        // Collections.sort uses compareTo()
//        List<ClockTime> list = new ArrayList<ClockTime>();
//        list.add(ClockTime.valueOf(2, TimeUnit.SECONDS));
//        list.add(ClockTime.valueOf(3, TimeUnit.SECONDS));
//        list.add(ClockTime.valueOf(1, TimeUnit.SECONDS));
//        Collections.sort(list);
//        assertEquals("list not sorted correctly", 1, list.get(0).toSeconds());
//        assertEquals("list not sorted correctly", 2, list.get(1).toSeconds());
//        assertEquals("list not sorted correctly", 3, list.get(2).toSeconds());
//    }
//    
}