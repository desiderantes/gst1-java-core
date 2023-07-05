package org.freedesktop.gstreamer.controller;

import org.freedesktop.gstreamer.*;
import org.junit.jupiter.api.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class InterpolationControlSourceTest {

    public InterpolationControlSourceTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        Gst.init("InterpolationControlSourceTest");
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of setMode method, of class InterpolationControlSource.
     */
    @Test
    public void testMode() {
        InterpolationControlSource controller = new InterpolationControlSource();
        InterpolationMode[] modes = InterpolationMode.values();
        for (InterpolationMode mode : modes) {
            controller.setMode(mode);
            assertEquals(mode, controller.getMode());
        }
    }

    @Test
    public void testSetValue() {
        List<ControlSource.TimedValue> timedValue
                = Collections.singletonList(
                new ControlSource.TimedValue(ClockTime.fromSeconds(10), 0.5)
        );
        InterpolationControlSource controller = new InterpolationControlSource();
        controller.set(timedValue.get(0).timestamp, timedValue.get(0).value);
        assertEquals(timedValue, controller.getAll());
    }

    @Test
    public void testSetValues() {
        List<ControlSource.TimedValue> timedValues
                = Stream.of(
                new ControlSource.TimedValue(ClockTime.fromSeconds(0), 0.0),
                new ControlSource.TimedValue(ClockTime.fromSeconds(1), 0.5),
                new ControlSource.TimedValue(ClockTime.fromSeconds(2), 0.2),
                new ControlSource.TimedValue(ClockTime.fromSeconds(4), 0.8)
        ).collect(Collectors.toList());
        InterpolationControlSource controller = new InterpolationControlSource();
        controller.setFromList(timedValues);
        assertEquals(timedValues, controller.getAll());
    }

    @Test
    public void testLinearInterpolation() {
        List<ControlSource.TimedValue> timedValues
                = Stream.of(
                new ControlSource.TimedValue(ClockTime.fromSeconds(0), 0.0),
                new ControlSource.TimedValue(ClockTime.fromSeconds(1), 1.0)
        ).collect(Collectors.toList());
        InterpolationControlSource controller = new InterpolationControlSource();
        controller.setMode(InterpolationMode.LINEAR);
        controller.setFromList(timedValues);

        Element volume = ElementFactory.make("volume", "volume");
        volume.addControlBinding(DirectControlBinding.create(volume, "volume", controller));
        volume.syncValues(0);
        assertEquals(0, ((Double) volume.get("volume")).doubleValue(), 0.001);
        volume.syncValues(ClockTime.fromMillis(500));
        assertEquals(5, ((Double) volume.get("volume")).doubleValue(), 0.001);
        volume.syncValues(ClockTime.fromSeconds(1));
        assertEquals(10, ((Double) volume.get("volume")).doubleValue(), 0.001);

    }

    @Test
    public void testLinearInterpolationAbsolute() {
        List<ControlSource.TimedValue> timedValues
                = Stream.of(
                new ControlSource.TimedValue(ClockTime.fromSeconds(0), 0.0),
                new ControlSource.TimedValue(ClockTime.fromSeconds(1), 5.0)
        ).collect(Collectors.toList());
        InterpolationControlSource controller = new InterpolationControlSource();
        controller.setMode(InterpolationMode.LINEAR);
        controller.setFromList(timedValues);

        Element volume = ElementFactory.make("volume", "volume");
        ControlBinding binding = DirectControlBinding.createAbsolute(volume, "volume", controller);

        assertEquals(2.5,
                (Double) binding.getValue(ClockTime.fromMillis(500)),
                0.01);

        Object[] values = new Object[3];
        binding.getValueArray(0, ClockTime.fromMillis(500), values);
        assertEquals(0, (Double) values[0], 0.01);
        assertEquals(2.5, (Double) values[1], 0.01);
        assertEquals(5, (Double) values[2], 0.01);

        volume.addControlBinding(binding);
        volume.syncValues(0);
        assertEquals(0, ((Double) volume.get("volume")), 0.001);
        volume.syncValues(ClockTime.fromMillis(500));
        assertEquals(2.5, ((Double) volume.get("volume")), 0.001);
        volume.syncValues(ClockTime.fromSeconds(1));
        assertEquals(5, ((Double) volume.get("volume")), 0.001);

    }

    @Test
    public void testGC() {
        InterpolationControlSource controller = new InterpolationControlSource();
        Element volume = ElementFactory.make("volume", "volume");
        ControlBinding binding = DirectControlBinding.create(volume, "volume", controller);
        volume.addControlBinding(binding);

        GCTracker tracker = new GCTracker(controller);
        controller = null;
        binding = null;
        volume = null;

        assertTrue(tracker.waitGC(), "Controller not garbage collected");
        assertTrue(tracker.waitDestroyed(), "Controller not destroyed");

    }

}
