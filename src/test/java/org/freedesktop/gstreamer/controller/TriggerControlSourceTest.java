package org.freedesktop.gstreamer.controller;

import org.freedesktop.gstreamer.*;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


/**
 *
 */
public class TriggerControlSourceTest {

    public TriggerControlSourceTest() {
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


    @Test
    public void testTolerance() {
        List<ControlSource.TimedValue> timedValues
                = Stream.of(
                new ControlSource.TimedValue(ClockTime.fromSeconds(0), 0.5),
                new ControlSource.TimedValue(ClockTime.fromSeconds(2), 1.0)
        ).collect(Collectors.toList());
        TriggerControlSource controller = new TriggerControlSource();
        controller.setFromList(timedValues);

        Element volume = ElementFactory.make("volume", "volume");
        volume.addControlBinding(DirectControlBinding.create(volume, "volume", controller));
        volume.syncValues(0);
        assertEquals(5, ((Double) volume.get("volume")), 0.001);
        volume.set("volume", 0);
        volume.syncValues(ClockTime.fromSeconds(1));
        assertEquals(0, ((Double) volume.get("volume")), 0.001);
        volume.syncValues(ClockTime.fromSeconds(2));
        assertEquals(10, ((Double) volume.get("volume")), 0.001);

        controller.setTolerance(ClockTime.fromMillis(500));
        volume.syncValues(ClockTime.fromMillis(450));
        assertEquals(5, ((Double) volume.get("volume")), 0.001);
        volume.set("volume", 0);
        volume.syncValues(ClockTime.fromMillis(550));
        assertEquals(0, ((Double) volume.get("volume")), 0.001);
        volume.syncValues(ClockTime.fromMillis(1650));
        assertEquals(10, ((Double) volume.get("volume")), 0.001);

    }

    @Test
    public void testGC() {
        TriggerControlSource controller = new TriggerControlSource();
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
