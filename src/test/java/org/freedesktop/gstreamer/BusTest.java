/*
 * Copyright (c) 2020 Neil C Smith
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

import org.freedesktop.gstreamer.lowlevel.GlibAPI;
import org.freedesktop.gstreamer.lowlevel.GstAPI.GErrorStruct;
import org.freedesktop.gstreamer.message.EOSMessage;
import org.freedesktop.gstreamer.message.Message;
import org.freedesktop.gstreamer.message.MessageType;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.freedesktop.gstreamer.lowlevel.GstElementAPI.GSTELEMENT_API;
import static org.freedesktop.gstreamer.lowlevel.GstMessageAPI.GSTMESSAGE_API;
import static org.junit.jupiter.api.Assertions.*;


public class BusTest {

    public BusTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("BusTest");
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
    public void endOfStream() {
        final TestPipe pipe = new TestPipe("endOfStream");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();

        Bus.EOS eosSignal = (GstObject source) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.play();
        pipe.getBus().connect(eosSignal);
        //
        // For the pipeline to post an EOS message, all sink elements must post it
        //
        for (Element elem : pipe.pipe.getSinks()) {
            GSTELEMENT_API.gst_element_post_message(elem,
                    GSTMESSAGE_API.gst_message_new_eos(elem));
        }
        pipe.run();
        pipe.getBus().disconnect(eosSignal);

        assertTrue(signalFired.get(), "EOS signal not received");
        pipe.dispose();
    }

    @Test
    public void stateChanged() {
        final TestPipe pipe = new TestPipe("stateChanged");
        final AtomicBoolean signalFired = new AtomicBoolean(false);

        Bus.STATE_CHANGED stateChanged = (GstObject source, State old, State current, State pending) -> {
            if (pending == State.PLAYING || current == State.PLAYING) {
                signalFired.set(true);
                pipe.quit();
            }
        };
        pipe.getBus().connect(stateChanged);
        pipe.play().run();
        pipe.getBus().disconnect(stateChanged);
        assertTrue(signalFired.get(), "STATE_CHANGED signal not received");
        pipe.dispose();
    }

    @Test
    public void asyncDone() {
        final TestPipe pipe = new TestPipe("asyncDone");
        final AtomicBoolean signalFired = new AtomicBoolean(false);

        Bus.ASYNC_DONE asyncDone = (GstObject source) -> {
            signalFired.set(true);
            pipe.quit();
        };
        pipe.getBus().connect(asyncDone);
        pipe.play().run();
        pipe.getBus().disconnect(asyncDone);
        assertTrue(signalFired.get(), "ASYNC_DONE message not received");
        pipe.dispose();
    }

    @Test
    public void errorMessage() {
        final TestPipe pipe = new TestPipe("errorMessage");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();

        Bus.ERROR errorSignal = (GstObject source, int code, String message) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(errorSignal);

        GErrorStruct msg = GlibAPI.GLIB_API.g_error_new(1, 1, "MSG");
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_error(pipe.src, msg, "testing error messages"));
        pipe.play().run();
        pipe.getBus().disconnect(errorSignal);
        pipe.dispose();
        assertTrue(signalFired.get(), "ERROR signal not received");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
        GlibAPI.GLIB_API.g_error_free(msg);
    }

    @Test
    public void warningMessage() {
        final TestPipe pipe = new TestPipe("warningMessage");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();

        Bus.WARNING signal = (GstObject source, int code, String message) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(signal);

        GErrorStruct msg = GlibAPI.GLIB_API.g_error_new(1, 1, "MSG");
        pipe.play();
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_warning(pipe.src, msg, "testing warning messages"));
        pipe.run();
        pipe.getBus().disconnect(signal);
        pipe.dispose();
        assertTrue(signalFired.get(), "WARNING signal not received");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
        GlibAPI.GLIB_API.g_error_free(msg);
    }

    @Test
    public void infoMessage() {
        final TestPipe pipe = new TestPipe("infoMessage");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();

        Bus.INFO signal = (GstObject source, int code, String message) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(signal);

        GErrorStruct msg = GlibAPI.GLIB_API.g_error_new(1, 1, "MSG");
        pipe.play();
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_info(pipe.src, msg, "testing info messages"));
        pipe.run();
        pipe.getBus().disconnect(signal);
        pipe.dispose();
        assertTrue(signalFired.get(), "INFO signal not received");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
        GlibAPI.GLIB_API.g_error_free(msg);
    }

    @Test
    public void bufferingData() {
        final TestPipe pipe = new TestPipe("bufferingData");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicInteger signalValue = new AtomicInteger(-1);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();
        final int PERCENT = 95;

        Bus.BUFFERING signal = (GstObject source, int percent) -> {
            signalFired.set(true);
            signalValue.set(percent);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(signal);
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_buffering(pipe.src, PERCENT));
        pipe.play().run();
        pipe.getBus().disconnect(signal);
        pipe.dispose();
        assertTrue(signalFired.get(), "BUFFERING signal not received");
        assertEquals(PERCENT, signalValue.get(), "Wrong percent value received for signal");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
    }

    @Test
    public void tagsFound() {
        final TestPipe pipe = new TestPipe("tagsFound");

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();
        Bus.TAG signal = (GstObject source, TagList tagList) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(signal);

        TagList tagList = new TagList();
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_tag(pipe.src, tagList));
        pipe.play().run();
        pipe.getBus().disconnect(signal);
        pipe.dispose();
        assertTrue(signalFired.get(), "TAG signal not received");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
    }

    @Test
    public void durationChanged() {
        final TestPipe pipe = new TestPipe("testDurationChanged");
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>(null);
        Bus.DURATION_CHANGED signal = (GstObject source) -> {
            signalFired.set(true);
            signalSource.set(source);
            pipe.quit();
        };
        pipe.getBus().connect(signal);
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_duration_changed(pipe.src));
        pipe.play().run();
        pipe.getBus().disconnect(signal);
        pipe.dispose();
        assertTrue(signalFired.get(), "DURATION signal not received");
        assertEquals(pipe.src, signalSource.get(), "Incorrect source object on signal");
    }

    @Test
    public void segmentDone() {
        final TestPipe pipe = new TestPipe("segmentDone");
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Format> formatReceived = new AtomicReference<>(null);
        final AtomicLong positionReceived = new AtomicLong(0);

        Bus.SEGMENT_DONE segmentDone = (source, format, position) -> {
            signalFired.set(true);
            formatReceived.set(format);
            positionReceived.set(position);
        };

        pipe.getBus().connect(segmentDone);

        final long POSITION = 0xdeadbeef;
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                GSTMESSAGE_API.gst_message_new_segment_done(pipe.src, Format.TIME, POSITION));
        pipe.run();

        assertTrue(signalFired.get(), "No segment done message received");
        assertEquals(Format.TIME, formatReceived.get(), "Wrong format");
        assertEquals(POSITION, positionReceived.get(), "Wrong position");
        pipe.dispose();
    }

    @Test
    public void anyMessage() {
        final TestPipe pipe = new TestPipe("anyMessage");

        final AtomicReference<Message> firstMessage = new AtomicReference<>();

        Bus.MESSAGE listener = (Bus bus, Message msg) -> {
            firstMessage.compareAndSet(null, msg);
            pipe.quit();
        };

        pipe.getBus().connect(listener);
        pipe.play().run();
        pipe.getBus().disconnect(listener);
        pipe.dispose();

        Message message = firstMessage.getAndSet(null);
        assertNotNull(message, "No message received");
        GCTracker gc = new GCTracker(message);
        message = null;
        assertTrue(gc.waitGC(), "Message not garbage collected");
        assertTrue(gc.waitDestroyed(), "Message not destroyed");
    }

    @Test
    public void postMessage() {
        final TestPipe pipe = new TestPipe();

        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<GstObject> signalSource = new AtomicReference<>();

        Bus.MESSAGE listener = (Bus bus, Message msg) -> {
            signalFired.set(true);
            signalSource.set(msg.getSource());
            pipe.quit();
        };

        pipe.getBus().connect(listener);
        Message message = new EOSMessage(pipe.src);
        pipe.getBus().post(message);
        pipe.run();
        assertTrue(signalFired.get(), "Message not posted");
        assertEquals(pipe.src, signalSource.get(), "Wrong source in message");
        pipe.dispose();

        GCTracker gc = new GCTracker(message);
        message = null;
        assertTrue(gc.waitGC(), "Message not garbage collected");
        assertTrue(gc.waitDestroyed(), "Message not destroyed");
    }

    @Test
    public void syncHandler() {
        final TestPipe pipe = new TestPipe("syncHandler");

        final AtomicReference<Message> firstMessageSync = new AtomicReference<>();
        final AtomicReference<Message> firstMessageAsync = new AtomicReference<>();

        BusSyncHandler syncHandler = (Message message) -> {
            firstMessageSync.compareAndSet(null, message);
            return BusSyncReply.PASS;
        };

        Bus.MESSAGE listener = (Bus bus, Message msg) -> {
            firstMessageAsync.compareAndSet(null, msg);
            pipe.quit();
        };

        pipe.getBus().setSyncHandler(syncHandler);
        pipe.getBus().connect(listener);
        pipe.play().run();
        pipe.getBus().disconnect(listener);
        pipe.dispose();

        Message message = firstMessageSync.getAndSet(null);
        Message asyncMessage = firstMessageAsync.getAndSet(null);
        assertNotNull(message, "No message received");
        assertSame(message, asyncMessage, "Sync and listeners messages not equal");
        GCTracker gc = new GCTracker(message);
        message = null;
        asyncMessage = null;
        assertTrue(gc.waitGC(), "Message not garbage collected");
        assertTrue(gc.waitDestroyed(), "Message not destroyed");
    }

    @Test
    public void syncHandlerRemoval() {
        final TestPipe pipe = new TestPipe("syncHandlerRemoval");

        final AtomicReference<Message> firstMessageSync = new AtomicReference<>();
        final AtomicReference<Message> firstMessageAsync = new AtomicReference<>();

        BusSyncHandler syncHandler = (Message message) -> {
            firstMessageSync.compareAndSet(null, message);
            return BusSyncReply.PASS;
        };

        Bus.MESSAGE listener = (Bus bus, Message msg) -> {
            firstMessageAsync.compareAndSet(null, msg);
            pipe.quit();
        };

        pipe.getBus().setSyncHandler(syncHandler);
        pipe.getBus().connect(listener);
        pipe.getBus().setSyncHandler(null);
        pipe.play().run();
        pipe.getBus().disconnect(listener);
        pipe.dispose();

        assertNull(firstMessageSync.getAndSet(null),
                "Removed sync handler received message");
        Message message = firstMessageAsync.getAndSet(null);
        assertNotNull(message, "No message received");
        GCTracker gc = new GCTracker(message);
        message = null;
        assertTrue(gc.waitGC(), "Message not garbage collected");
        assertTrue(gc.waitDestroyed(), "Message not destroyed");
    }

    @Test
    public void listenerRemoval() {
        final TestPipe pipe = new TestPipe("checkListenerRemoval");

        final AtomicReference<Message> firstMessage = new AtomicReference<>(null);
        final AtomicBoolean stateChangedFired = new AtomicBoolean(false);

        Bus.MESSAGE listener = (Bus bus, Message msg) -> {
            firstMessage.compareAndSet(null, msg);
            pipe.quit();
        };

        Bus.STATE_CHANGED stateListener = (GstObject source, State old, State current, State pending) -> {
            stateChangedFired.set(true);
        };

        pipe.getBus().connect(listener);
        pipe.getBus().connect(stateListener);
        pipe.getBus().disconnect(stateListener);
        pipe.play().run();
        pipe.getBus().disconnect(listener);
        pipe.dispose();

        Message message = firstMessage.getAndSet(null);
        assertNotNull(message, "No message received");

        assertFalse(stateChangedFired.get(), "State changed fired after removal");

        GCTracker gc = new GCTracker(message);
        message = null;
        assertTrue(gc.waitGC(), "Message not garbage collected");
        assertTrue(gc.waitDestroyed(), "Message not destroyed");
    }

    @Test
    public void extendedMessageIssue202() {
        final TestPipe pipe = new TestPipe("issue202");

        final AtomicBoolean signalFired = new AtomicBoolean(false);

        Bus.MESSAGE msgListener = (Bus bus, Message msg) -> {
            signalFired.set(true);
        };

        Bus.ERROR errListener = (GstObject source, int code, String message) -> {
            // @TODO If used as flags, DEVICE_REMOVED and ERROR overlap.
            // but an exception will be thrown in the executor and logged
            // rather than this method being called - need a way to fail with
            // executor exceptions in tests?
        };

        pipe.getBus().connect(errListener);
        pipe.getBus().connect(msgListener);

        for (Element elem : pipe.pipe.getSources()) {
            GSTELEMENT_API.gst_element_post_message(elem,
                    GSTMESSAGE_API
                            .gst_message_new_custom(MessageType.DEVICE_REMOVED,
                                    elem, null)
            );
        }
        pipe.play().run();
        pipe.getBus().disconnect(msgListener);
        pipe.getBus().disconnect(errListener);

        assertTrue(signalFired.get(), "Custom message not received");
        pipe.dispose();
    }

}
