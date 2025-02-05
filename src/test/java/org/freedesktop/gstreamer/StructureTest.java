/*
 * Copyright (c) 2020 Neil C Smith
 * Copyright (c) 2009 Levente Farkas
 * Copyright (C) 2009 Tamas Korodi <kotyo@zamba.fm>
 * Copyright (C) 2007 Wayne Meissner
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freedesktop.gstreamer;

import org.freedesktop.gstreamer.glib.GCancellable;
import org.freedesktop.gstreamer.lowlevel.GType;
import org.freedesktop.gstreamer.lowlevel.GValueAPI;
import org.freedesktop.gstreamer.util.TestAssumptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


public class StructureTest {

    private Structure structure;

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init(Gst.getVersion(), "StructureTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @BeforeEach
    public void setUp() {
        structure = new Structure("nazgul");
    }

    @Test
    public void testGetName() {
        assertEquals("nazgul", structure.getName());
    }

    @Test
    public void testGetValue() {
        structure.setValue("uint", GType.UINT, 9);
        assertEquals(9, structure.getValue("uint"));

        try {
            structure.getValue("noexist");
            fail("Structure.InvalidFieldException should have been thrown");
        } catch (Structure.InvalidFieldException e) {
        }

        structure.setDouble("double", 9.0);
        assertEquals(9.0, structure.getValue("double"));

        structure.setValue("bool", GType.BOOLEAN, true);
        assertEquals(true, structure.getValue("bool"));

    }

    @Test
    public void testGetValues() {
        GValueAPI.GValueArray ar = new GValueAPI.GValueArray(2);
        ar.append(new GValueAPI.GValue(GType.DOUBLE, 7.5));
        ar.append(new GValueAPI.GValue(GType.DOUBLE, 14.3));
        structure.setValue("valuearray", GType.valueOf(GValueAPI.GValueArray.GTYPE_NAME), ar);
        List<Double> doubles = structure.getValues(Double.class, "valuearray");
        assertEquals(7.5, doubles.get(0), 0.001);
        assertEquals(14.3, doubles.get(1), 0.001);
        try {
            List<String> strings = structure.getValues(String.class, "valuearray");
            fail("Trying to extract the wrong type from GValueArray not throwing exception");
        } catch (Structure.InvalidFieldException ex) {
        }
        try {
            List<Double> strings = structure.getValues(Double.class, "non_existent");
            fail("Trying to extract a non-existent GValueArray field");
        } catch (Structure.InvalidFieldException ex) {
        }
    }

    @Test
    public void testGetInteger() {
        structure.setInteger("int", 9);
        assertEquals(9, structure.getInteger("int"));

        structure.setInteger("int", -9);
        assertEquals(-9, structure.getInteger("int"));
    }

    @Test
    public void testGetIntegers() {
        GValueAPI.GValueArray ar = new GValueAPI.GValueArray(2);
        ar.append(new GValueAPI.GValue(GType.INT, 32));
        ar.append(new GValueAPI.GValue(GType.INT, -49));
        structure.setValue("integers", GType.valueOf(GValueAPI.GValueArray.GTYPE_NAME), ar);
        int[] in = new int[2];
        int[] ints = structure.getIntegers("integers", in);
        assertSame(in, ints);
        assertEquals(32, ints[0]);
        assertEquals(-49, ints[1]);

        in = new int[1];
        ints = structure.getIntegers("integers", in);
        assertNotSame(in, ints);
        assertEquals(32, ints[0]);
        assertEquals(-49, ints[1]);

        structure.setInteger("single_integer", 18);
        int[] single = structure.getIntegers("single_integer", in);
        assertSame(in, single);
        assertEquals(18, single[0]);
    }

    @Test
    public void testGetDouble() {
        structure.setDouble("double", 9.0);
        assertEquals(9.0, structure.getDouble("double"), 0);

        structure.setDouble("double", -9.0);
        assertEquals(-9.0, structure.getDouble("double"), 0);
    }

    @Test
    public void testGetDoubles() {
        GValueAPI.GValueArray ar = new GValueAPI.GValueArray(2);
        ar.append(new GValueAPI.GValue(GType.DOUBLE, 3.25));
        ar.append(new GValueAPI.GValue(GType.DOUBLE, 79.6));
        structure.setValue("doubles", GType.valueOf(GValueAPI.GValueArray.GTYPE_NAME), ar);
        double[] in = new double[2];
        double[] doubles = structure.getDoubles("doubles", in);
        assertSame(in, doubles);
        assertEquals(3.25, doubles[0], 0.001);
        assertEquals(79.6, doubles[1], 0.001);

        in = new double[1];
        doubles = structure.getDoubles("doubles", in);
        assertNotSame(in, doubles);
        assertEquals(3.25, doubles[0], 0.001);
        assertEquals(79.6, doubles[1], 0.001);

        structure.setDouble("single_double", 18.2);
        double[] single = structure.getDoubles("single_double", in);
        assertSame(in, single);
        assertEquals(18.2, single[0], 0.001);
    }

    @Test
    public void testFraction() {
        structure.setFraction("fraction", 10, 1);

        assertTrue(structure.hasField("fraction"));

        assertEquals(10, structure.getFraction("fraction").getNumerator());
        assertEquals(1, structure.getFraction("fraction").getDenominator());

        structure.setFraction("fraction", 17, 10);
        assertEquals(17, structure.getFraction("fraction").getNumerator());
        assertEquals(10, structure.getFraction("fraction").getDenominator());
    }

    @Test
    public void testValueListInteger() {
        Caps caps = Caps.fromString("audio/x-raw,rate={44100,48000}");
        List<Integer> rates = caps.getStructure(0).getValues(Integer.class, "rate");
        assertEquals(Arrays.asList(44100, 48000), rates);
    }

    @Test
    public void testValueListStrings() {
        Caps caps = Caps.fromString("video/x-raw,format={RGB, BGR, RGBx, BGRx}");
        List<String> formats = caps.getStructure(0).getValues(String.class, "format");
        assertEquals(Arrays.asList("RGB", "BGR", "RGBx", "BGRx"), formats);
    }

    @Test
    public void testValueListChecksType() {
        assertThrows(Structure.InvalidFieldException.class, () -> {
            Caps caps = Caps.fromString("video/x-raw,format={RGB, BGR, RGBx, BGRx}");
            caps.getStructure(0).getValues(Integer.class, "format");
        });
    }

    @Test
    public void testSetMistypedObject() {
        GCancellable notACapsInstance = new GCancellable();
        assertThrows(IllegalArgumentException.class, () -> structure.setObject("whatever", Caps.GTYPE_NAME, notACapsInstance));
    }

    @Test
    public void testSetUntypedObject() {
        GCancellable anyKindOfObject = new GCancellable();
        structure.setObject("whatever", GType.OBJECT.getTypeName(), anyKindOfObject);
        Object value = structure.getValue("whatever");
        assertSame(anyKindOfObject, value);
    }

    @Test
    public void testSetObject() {
        GCancellable anyKindOfObject = new GCancellable();
        structure.setObject("whatever", GCancellable.GTYPE_NAME, anyKindOfObject);
        Object value = structure.getValue("whatever");
        assertSame(anyKindOfObject, value);
    }

    @Test
    public void testSetNullObject() {
        assertThrows(IllegalArgumentException.class, () -> structure.setObject("whatever", GCancellable.GTYPE_NAME, null));
        Object value = structure.getValue("whatever");
        assertNull(value);
    }

    @Test
    public void testIssue173() {
        TestAssumptions.requireGstVersion(1, 16);
        TestAssumptions.requireElement("srtsink");

        Element srtsink = ElementFactory.make("srtsink", "srtsink");
        srtsink.set("uri", "srt://:8888/");
        Object stats = srtsink.get("stats");
        assertTrue(stats instanceof Structure);
    }
}
