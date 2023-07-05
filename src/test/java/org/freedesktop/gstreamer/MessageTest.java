/*
 * Copyright (c) 2020 Neil C Smith
 * Copyright (c) 2009 Levente Farkas
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

import org.freedesktop.gstreamer.message.*;
import org.junit.jupiter.api.*;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.freedesktop.gstreamer.lowlevel.GstElementAPI.GSTELEMENT_API;
import static org.freedesktop.gstreamer.lowlevel.GstMessageAPI.GSTMESSAGE_API;
import static org.freedesktop.gstreamer.lowlevel.GstTagListAPI.GSTTAGLIST_API;
import static org.junit.jupiter.api.Assertions.*;


/**
 *
 */
public class MessageTest {

    public MessageTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("MessageTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void gst_message_new_eos() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = GSTMESSAGE_API.gst_message_new_eos(fakesink);
        assertTrue(msg instanceof EOSMessage, "gst_message_new_eos did not return an instance of EOSMessage");
    }

    @Test
    public void EOSMessage_getSource() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = GSTMESSAGE_API.gst_message_new_eos(fakesink);
        assertEquals(fakesink, msg.getSource(), "Wrong source in message");
    }

    @Test
    public void postEOS() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<>(null);
        pipe.getBus().connect("message::eos", new Bus.MESSAGE() {

            @Override
            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        pipe.play();
        GSTELEMENT_API.gst_element_post_message(pipe.sink, new EOSMessage(pipe.sink));
        pipe.run();

        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.EOS, msg.getType(), "Wrong message type");
        assertTrue(msg instanceof EOSMessage, "Message not intance of EOSMessage");
        assertEquals(pipe.pipe, msg.getSource(), "Wrong source in message");
        pipe.dispose();
    }

    @Test
    public void gst_message_new_percent() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = GSTMESSAGE_API.gst_message_new_buffering(fakesink, 55);
        assertTrue(msg instanceof BufferingMessage, "gst_message_new_eos did not return an instance of BufferingMessage");
    }

    @Test
    public void BufferingMessage_getPercent() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        BufferingMessage msg = (BufferingMessage) GSTMESSAGE_API.gst_message_new_buffering(fakesink, 55);
        assertEquals(55, msg.getPercent(), "Wrong source in message");
    }

    @Test
    public void postBufferingMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<>(null);
        pipe.getBus().connect("message::buffering", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        final int PERCENT = 55;
        GSTELEMENT_API.gst_element_post_message(pipe.sink, new BufferingMessage(pipe.src, PERCENT));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.BUFFERING, msg.getType(), "Wrong message type");
        assertTrue(msg instanceof BufferingMessage, "Message not instance of BufferingMessage");
        assertEquals(pipe.src, msg.getSource(), "Wrong source in message");
        assertEquals(PERCENT, ((BufferingMessage) msg).getPercent(), "Wrong percent value in message");
        pipe.dispose();
    }

    @Test
    public void gst_message_new_duration() {
        Element fakesink = ElementFactory.make("fakesink", "sink");
        Message msg = GSTMESSAGE_API.gst_message_new_duration_changed(fakesink);
        assertTrue(msg instanceof DurationChangedMessage, "gst_message_new_duration did not return an instance of DurationMessage");
    }

    @Test
    public void postDurationMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<>(null);
        pipe.getBus().connect("message::duration-changed", new Bus.MESSAGE() {

            @Override
            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        GSTELEMENT_API.gst_element_post_message(pipe.src, new DurationChangedMessage(pipe.src));
        pipe.play().run();
        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.DURATION_CHANGED, msg.getType(), "Wrong message type");
        assertTrue(msg instanceof DurationChangedMessage, "Message not instance of EOSMessage");
        assertEquals(pipe.src, msg.getSource(), "Wrong source in message");
        pipe.dispose();
    }

    @Test
    public void gst_message_new_tag() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = GSTMESSAGE_API.gst_message_new_tag(src, new TagList());
        assertTrue(msg instanceof TagMessage, "gst_message_new_tag did not return an instance of TagMessage");
    }

    @Test
    public void TagMessage_getTagList() {
        Element src = ElementFactory.make("fakesrc", "src");
        TagList tl = new TagList();
        final String MAGIC = "fubar";
        GSTTAGLIST_API.gst_tag_list_add(tl, TagMergeMode.APPEND, "artist", MAGIC);
        TagMessage msg = (TagMessage) GSTMESSAGE_API.gst_message_new_tag(src, tl);
        tl = msg.getTagList();
        assertEquals(MAGIC, tl.getString("artist", 0), "Wrong artist in tag list");
    }

    @Test
    public void gst_message_new_state_changed() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = GSTMESSAGE_API.gst_message_new_state_changed(src, State.READY, State.PLAYING, State.VOID_PENDING);
        assertTrue(msg instanceof StateChangedMessage, "gst_message_new_state_changed did not return an instance of StateChangedMessage");
    }

    @Test
    public void constructStateChanged() {
        Element src = ElementFactory.make("fakesrc", "src");
        new StateChangedMessage(src, State.READY, State.PLAYING, State.VOID_PENDING);
    }

    @Test
    public void StateChanged_get() {
        Element src = ElementFactory.make("fakesrc", "src");
        StateChangedMessage msg = (StateChangedMessage) GSTMESSAGE_API.gst_message_new_state_changed(src, State.READY, State.PLAYING, State.VOID_PENDING);
        assertEquals(State.READY, msg.getOldState(), "Wrong old state");
        assertEquals(State.PLAYING, msg.getNewState(), "Wrong new state");
        assertEquals(State.VOID_PENDING, msg.getPendingState(), "Wrong pending state");
    }

    @Test
    public void postStateChangedMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<Message>(null);

        pipe.getBus().connect("message::state-changed", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                new StateChangedMessage(pipe.src, State.READY, State.PLAYING, State.VOID_PENDING));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.STATE_CHANGED, msg.getType(), "Wrong message type");
        StateChangedMessage smsg = (StateChangedMessage) msg;
        assertEquals(State.READY, smsg.getOldState(), "Wrong old state");
        assertEquals(State.PLAYING, smsg.getNewState(), "Wrong new state");
        assertEquals(State.VOID_PENDING, smsg.getPendingState(), "Wrong pending state");
        pipe.dispose();
    }

    @Test
    public void gst_message_new_segment_done() {
        Element src = ElementFactory.make("fakesrc", "src");
        Message msg = GSTMESSAGE_API.gst_message_new_segment_done(src, Format.TIME, 0xdeadbeef);
        assertTrue(msg instanceof SegmentDoneMessage,
                "gst_message_new_segment_done did not return an instance of SegmentDoneMessage");
    }

    @Test
    public void constructSegmentDone() {
        Element src = ElementFactory.make("fakesrc", "src");
        new SegmentDoneMessage(src, Format.TIME, 0xdeadbeef);
    }

    @Test
    public void parseSegmentDone() {
        Element src = ElementFactory.make("fakesrc", "src");
        SegmentDoneMessage msg = (SegmentDoneMessage) GSTMESSAGE_API.gst_message_new_segment_done(src, Format.TIME, 0xdeadbeef);
        assertEquals(Format.TIME, msg.getFormat(), "Wrong format");
        assertEquals(0xdeadbeef, msg.getPosition(), "Wrong position");
    }

    @Test
    public void postSegmentDoneMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<>(null);

        pipe.getBus().connect("message::segment-done", new Bus.MESSAGE() {

            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        final int POSITION = 0xdeadbeef;
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                new SegmentDoneMessage(pipe.src, Format.TIME, POSITION));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.SEGMENT_DONE, msg.getType(), "Wrong message type");
        SegmentDoneMessage smsg = (SegmentDoneMessage) msg;
        assertEquals(Format.TIME, smsg.getFormat(), "Wrong format");
        assertEquals(POSITION, smsg.getPosition(), "Wrong position");
        pipe.dispose();
    }

    @Test
    public void postLatencyMessage() {
        final TestPipe pipe = new TestPipe();
        final AtomicBoolean signalFired = new AtomicBoolean(false);
        final AtomicReference<Message> signalMessage = new AtomicReference<>(null);

        pipe.getBus().connect("message::latency", new Bus.MESSAGE() {

            @Override
            public void busMessage(Bus bus, Message msg) {
                signalFired.set(true);
                signalMessage.set(msg);
                pipe.quit();
            }
        });
        GSTELEMENT_API.gst_element_post_message(pipe.src,
                new LatencyMessage(pipe.src));
        pipe.run();
        Message msg = signalMessage.get();
        assertNotNull(msg, "No message available on bus");
        assertEquals(MessageType.LATENCY, msg.getType(), "Wrong message type");
        @SuppressWarnings("unused")
        LatencyMessage smsg = (LatencyMessage) msg;
        pipe.dispose();
    }
}
