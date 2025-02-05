/*
 * Copyright (c) 2020 Neil C Smith
 * Copyright (C) 2014 Tom Greenwood <tgreenwood@cafex.com>
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 2004 Wim Taymans <wim@fluendo.com>
 *
 * This file is part of gstreamer-java.
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

import com.sun.jna.Callback;
import com.sun.jna.CallbackThreadInitializer;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.LongByReference;
import com.sun.jna.ptr.PointerByReference;
import org.freedesktop.gstreamer.glib.GObject;
import org.freedesktop.gstreamer.glib.NativeEnum;
import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstAPI.GErrorStruct;
import org.freedesktop.gstreamer.lowlevel.GstBusAPI;
import org.freedesktop.gstreamer.lowlevel.GstBusAPI.BusCallback;
import org.freedesktop.gstreamer.lowlevel.GstBusPtr;
import org.freedesktop.gstreamer.lowlevel.GstMessagePtr;
import org.freedesktop.gstreamer.message.Message;
import org.freedesktop.gstreamer.message.MessageType;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.freedesktop.gstreamer.lowlevel.GlibAPI.GLIB_API;
import static org.freedesktop.gstreamer.lowlevel.GstBusAPI.GSTBUS_API;
import static org.freedesktop.gstreamer.lowlevel.GstMessageAPI.GSTMESSAGE_API;
import static org.freedesktop.gstreamer.lowlevel.GstMiniObjectAPI.GSTMINIOBJECT_API;

/**
 * The {@link Bus} is an object responsible for delivering {@link Message}s in a
 * first-in first-out way from the streaming threads to the application.
 * <p>
 * See upstream documentation at
 * <a href="https://gstreamer.freedesktop.org/documentation/gstreamer/gstbus.html"
 * >https://gstreamer.freedesktop.org/documentation/gstreamer/gstbus.html</a>
 * <p>
 * Since the application typically only wants to deal with delivery of these
 * messages from one thread, the Bus will marshal the messages between different
 * threads. This is important since the actual streaming of media is done in
 * another thread than the application.
 * <p>
 * It is also possible to get messages from the bus without any thread
 * marshalling with the {@link #setSyncHandler} method. This makes it possible
 * to react to a message in the same thread that posted the message on the bus.
 * This should only be used if the application is able to deal with messages
 * from different threads.
 * <p>
 * Every {@link Pipeline} has one bus.
 * <p>
 * Note that a Pipeline will set its bus into flushing state when changing from
 * READY to NULL state.
 */
public class Bus extends GstObject {

    public static final String GTYPE_NAME = "GstBus";

    private static final Logger LOG = Logger.getLogger(Bus.class.getName());
    private static final SyncCallback SYNC_CALLBACK = new SyncCallback();
    private final Object lock = new Object();
    private final List<MessageProxy<?>> messageProxies = new CopyOnWriteArrayList<>();
    private volatile BusSyncHandler syncHandler = null;
    private boolean watchAdded = false;

    /**
     * This constructor is used internally by gstreamer-java
     *
     * @param init internal initialization data
     */
    Bus(Initializer init) {
        super(init);
        GSTBUS_API.gst_bus_set_sync_handler(this, null, null, null);
        GSTBUS_API.gst_bus_set_sync_handler(this, SYNC_CALLBACK, null, null);
    }

    /**
     * Instructs the bus to flush out any queued messages.
     * <p>
     * If flushing, flush out any messages queued in the bus. Will flush future
     * messages until {@link #setFlushing} is called with false.
     *
     * @param flushing true if flushing is desired.
     */
    public void setFlushing(boolean flushing) {
        GSTBUS_API.gst_bus_set_flushing(this, flushing ? 1 : 0);
    }

    /**
     * Add a listener for end-of-stream messages.
     *
     * @param listener The listener to be called when end-of-stream is
     *                 encountered.
     */
    public void connect(final EOS listener) {
        connect(EOS.class, listener, (bus, msg, user_data) -> {
            listener.endOfStream(Natives.objectFor(msg.getSource(), GstObject.class, true, true));
            return true;
        });
    }

    /**
     * Disconnect the listener for end-of-stream messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(EOS listener) {
        disconnect(EOS.class, listener);
    }

    /**
     * Add a listener for error messages.
     *
     * @param listener The listener to be called when an error in the stream is
     *                 encountered.
     */
    public void connect(final ERROR listener) {
        connect(ERROR.class, listener, (bus, msg, user_data) -> {
            PointerByReference err = new PointerByReference();
            GSTMESSAGE_API.gst_message_parse_error(msg, err, null);
            GErrorStruct error = new GErrorStruct(err.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.errorMessage(source, error.getCode(), error.getMessage());
            GLIB_API.g_error_free(err.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for error messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(ERROR listener) {
        disconnect(ERROR.class, listener);
    }

    /**
     * Add a listener for warning messages.
     *
     * @param listener The listener to be called when an {@link Element} emits a
     *                 warning.
     */
    public void connect(final WARNING listener) {
        connect(WARNING.class, listener, (bus, msg, user_data) -> {
            PointerByReference err = new PointerByReference();
            GSTMESSAGE_API.gst_message_parse_warning(msg, err, null);
            GErrorStruct error = new GErrorStruct(err.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.warningMessage(source, error.getCode(), error.getMessage());
            GLIB_API.g_error_free(err.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for warning messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(WARNING listener) {
        disconnect(WARNING.class, listener);
    }

    /**
     * Add a listener for informational messages.
     *
     * @param listener The listener to be called when an {@link Element} emits a
     *                 an informational message.
     */
    public void connect(final INFO listener) {
        connect(INFO.class, listener, (bus, msg, user_data) -> {
            PointerByReference err = new PointerByReference();
            GSTMESSAGE_API.gst_message_parse_info(msg, err, null);
            GErrorStruct error = new GErrorStruct(err.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.infoMessage(source, error.getCode(), error.getMessage());
            GLIB_API.g_error_free(err.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for informational messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(INFO listener) {
        disconnect(INFO.class, listener);
    }

    /**
     * Add a listener for {@link State} changes in the Pipeline.
     *
     * @param listener The listener to be called when the Pipeline changes
     *                 state.
     */
    public void connect(final STATE_CHANGED listener) {
        connect(STATE_CHANGED.class, listener, (bus, msg, user_data) -> {
            IntByReference oldPtr = new IntByReference();
            IntByReference currentPtr = new IntByReference();
            IntByReference pendingPtr = new IntByReference();
            GSTMESSAGE_API.gst_message_parse_state_changed(msg, oldPtr, currentPtr, pendingPtr);
            State old = NativeEnum.fromInt(State.class, oldPtr.getValue());
            State current = NativeEnum.fromInt(State.class, currentPtr.getValue());
            State pending = NativeEnum.fromInt(State.class, pendingPtr.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.stateChanged(source, old, current, pending);
            return true;
        });
    }

    /**
     * Disconnect the listener for {@link State} change messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(STATE_CHANGED listener) {
        disconnect(STATE_CHANGED.class, listener);
    }

    /**
     * Add a listener for new media tags.
     *
     * @param listener The listener to be called when new media tags are found.
     */
    public void connect(final TAG listener) {
        connect(TAG.class, listener, (bus, msg, user_data) -> {
            PointerByReference list = new PointerByReference();
            GSTMESSAGE_API.gst_message_parse_tag(msg, list);
            TagList tl = new TagList(Natives.initializer(list.getValue()));
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.tagsFound(source, tl);
            return true;
        });
    }

    /**
     * Disconnect the listener for tag messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(TAG listener) {
        disconnect(TAG.class, listener);
    }

    /**
     * Add a listener for {@link BUFFERING} messages in the Pipeline.
     *
     * @param listener The listener to be called when the Pipeline buffers data.
     */
    public void connect(final BUFFERING listener) {
        connect(BUFFERING.class, listener, (bus, msg, user_data) -> {
            IntByReference percent = new IntByReference();
            GSTMESSAGE_API.gst_message_parse_buffering(msg, percent);
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.bufferingData(source, percent.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for buffering messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(BUFFERING listener) {
        disconnect(BUFFERING.class, listener);
    }

    /**
     * Add a listener for duration changes.
     *
     * @param listener The listener to be called when the duration changes.
     */
    public void connect(final DURATION_CHANGED listener) {
        connect(DURATION_CHANGED.class, listener, (bus, msg, user_data) -> {
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.durationChanged(source);
            return true;
        });
    }

    /**
     * Disconnect the listener for duration change messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(DURATION_CHANGED listener) {
        disconnect(DURATION_CHANGED.class, listener);
    }

    /**
     * Add a listener for {@link SEGMENT_START} messages in the Pipeline.
     *
     * @param listener The listener to be called when the Pipeline has started a
     *                 segment.
     */
    public void connect(final SEGMENT_START listener) {
        connect(SEGMENT_START.class, listener, (bus, msg, user_data) -> {
            IntByReference formatPtr = new IntByReference();
            LongByReference positionPtr = new LongByReference();
            GSTMESSAGE_API.gst_message_parse_segment_start(msg, formatPtr, positionPtr);
            Format format = NativeEnum.fromInt(Format.class, formatPtr.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.segmentStart(source, format, positionPtr.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for segment-start messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(SEGMENT_START listener) {
        disconnect(SEGMENT_START.class, listener);
    }

    /**
     * Add a listener for {@link SEGMENT_DONE} messages in the Pipeline.
     *
     * @param listener The listener to be called when the Pipeline has finished
     *                 a segment.
     */
    public void connect(final SEGMENT_DONE listener) {
        connect(SEGMENT_DONE.class, listener, (bus, msg, user_data) -> {
            IntByReference formatPtr = new IntByReference();
            LongByReference positionPtr = new LongByReference();
            GSTMESSAGE_API.gst_message_parse_segment_done(msg, formatPtr, positionPtr);
            Format format = NativeEnum.fromInt(Format.class, formatPtr.getValue());
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.segmentDone(source, format, positionPtr.getValue());
            return true;
        });
    }

    /**
     * Disconnect the listener for segment-done messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(SEGMENT_DONE listener) {
        disconnect(SEGMENT_DONE.class, listener);
    }

    /**
     * Add a listener for {@link ASYNC_DONE} messages in the Pipeline.
     *
     * @param listener The listener to be called when the an element has
     *                 finished an async state change.
     */
    public void connect(final ASYNC_DONE listener) {
        connect(ASYNC_DONE.class, listener, (bus, msg, user_data) -> {
            GstObject source = Natives.objectFor(msg.getSource(), GstObject.class, true, true);
            listener.asyncDone(source);
            return true;
        });
    }

    /**
     * Disconnect the listener for async-done messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(ASYNC_DONE listener) {
        disconnect(ASYNC_DONE.class, listener);
    }

    /**
     * Add a listener for all messages posted on the Bus.
     *
     * @param listener The listener to be called when a {@link Message} is
     *                 posted.
     */
    public void connect(final MESSAGE listener) {
        connect(MESSAGE.class, listener, (bus, msg, user_data) -> {
            listener.busMessage(Bus.this, Natives.objectFor(msg, Message.class, true, true));
            return true;
        });
    }

    /**
     * Add a listener for messages of type {@code signal} posted on the Bus.
     *
     * @param signal   the signal to connect to.
     * @param listener The listener to be called when a {@link Message} is
     *                 posted.
     */
    public void connect(String signal, final MESSAGE listener) {
        //
        // Deal with being called as e.g. "message::eos"
        //
        if (signal.contains("::")) {
            signal = signal.substring(signal.lastIndexOf("::") + 2);
        }
        connect(signal, MESSAGE.class, listener, (BusCallback) (bus, msg, user_data) -> {
            listener.busMessage(Bus.this, Natives.objectFor(msg, Message.class, true, true));
            return true;
        });
    }

    /**
     * Disconnect the listener for segment-done messages.
     *
     * @param listener The listener that was registered to receive the message.
     */
    public void disconnect(MESSAGE listener) {
        disconnect(MESSAGE.class, listener);
    }

    /**
     * Posts a {@link Message} on this Bus.
     *
     * @param message the message to post.
     * @return <tt>true</tt> if the message could be posted, <tt>false</tt> if
     * the bus is flushing.
     */
    public boolean post(Message message) {
        return GSTBUS_API.gst_bus_post(this, message);
    }

    /**
     * Clear the synchronous handler.
     * <p>
     * This is a convenience method equivalent to {@code setSyncHandler(null)}
     */
    public void clearSyncHandler() {
        setSyncHandler(null);
    }

    /**
     * Get the current synchronous handler.
     *
     * @return current sync handler, or null
     */
    public BusSyncHandler getSyncHandler() {
        return syncHandler;
    }

    /**
     * Sets the synchronous handler (message listener) on the bus. The handler
     * will be called every time a new message is posted on the bus. Note that
     * the handler will be called in the same thread context as the posting
     * object. Applications should generally handle messages asynchronously
     * using the other message listeners.
     * <p>
     * Only one handler may be attached to the bus at any one time. An attached
     * sync handler forces creation of {@link Message} objects for all messages
     * on the bus, so the handler should be removed if no longer required.
     * <p>
     * A single native sync handler is used at all times, with synchronous and
     * asynchronous dispatch handled on the Java side, so the bindings do not
     * inherit issues in clearing or replacing the sync handler with versions of
     * GStreamer prior to 1.16.3.
     *
     * @param handler bus sync handler, or null to remove
     */
    public void setSyncHandler(BusSyncHandler handler) {
        syncHandler = handler;
    }

    /**
     * Connects to a signal.
     * <p>
     * The signal name is deduced from the listenerClass name.
     *
     * @param listenerClass the class of the listener.
     * @param listener      the listener to associate with the {@code callback}
     * @param callback      The callback to call when the signal is emitted.
     */
    private <T> void connect(Class<T> listenerClass, T listener, BusCallback callback) {
        String className = listenerClass.getSimpleName();
        MessageType type;
        if ("MESSAGE".equals(className)) {
            type = MessageType.ANY;
        } else {
            type = MessageType.valueOf(listenerClass.getSimpleName());
        }
        addMessageProxy(type, listenerClass, listener, callback);
    }

    /**
     * Connects a callback to a signal.
     * <p>
     * This differs to {@link GObject#connect} in that it hooks up Bus signals
     * to the sync callback, not the generic GObject signal mechanism.
     *
     * @param <T>           listener type
     * @param signal        the name of the signal to connect to.
     * @param listenerClass the class of the {@code listener}
     * @param listener      the listener to associate with the {@code callback}
     * @param callback      the callback to call when the signal is emitted.
     */
    @Override
    public <T> void connect(String signal, Class<T> listenerClass, T listener,
                            final Callback callback) {
        if (listenerClass.getEnclosingClass() != Bus.class) {
            super.connect(signal, listenerClass, listener, callback);
        } else {
            MessageType type;
            if ("message".equals(signal)) {
                type = MessageType.ANY;
            } else {
                type = MessageType.valueOf(signal.toUpperCase(Locale.ROOT).replace('-', '_'));
            }
            addMessageProxy(type, listenerClass, listener, (BusCallback) callback);
        }
    }

    private synchronized <T> void addMessageProxy(MessageType type,
                                                  Class<T> listenerClass,
                                                  T listener,
                                                  BusCallback callback) {
        messageProxies.add(new MessageProxy(type, listenerClass, listener, callback));
        addWatch();
    }

    @Override
    public <T> void disconnect(Class<T> listenerClass, T listener) {
        if (listenerClass.getEnclosingClass() != Bus.class) {
            super.disconnect(listenerClass, listener);
        } else {
            removeMessageProxy(listenerClass, listener);
        }
    }

    private synchronized <T> void removeMessageProxy(Class<T> listenerClass, T listener) {
        messageProxies.removeIf(p -> p.listener == listener);
        if (messageProxies.isEmpty()) {
            removeWatch();
        }
    }

    /**
     * Dispatches a message to all interested listeners.
     * <p>
     * We do this here from a sync callback, because the default gstbus dispatch
     * uses the default main context to signal that there are messages waiting
     * on the bus. Since that is used by the GTK L&F under swing, we never get
     * those notifications, and the messages just queue up.
     */
    private void dispatchMessage(GstBusPtr busPtr, GstMessagePtr msgPtr) {
        messageProxies.forEach(p -> {
            try {
                p.busMessage(busPtr, msgPtr);
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Exception thrown by bus message handler", t);
            }
        });
        GSTMINIOBJECT_API.gst_mini_object_unref(msgPtr);
    }

    @Override
    public void dispose() {
        removeWatch();
        super.dispose();
    }

    /**
     * Adds the bus signal watch. This will reference the bus until the signal
     * watch is removed and so will stop the Bus being GC'd and disposed.
     */
    private void addWatch() {
        synchronized (lock) {
            if (!watchAdded) {
                LOG.fine("Add watch");
                GSTBUS_API.gst_bus_add_signal_watch(this);
                watchAdded = true;
            }
        }
    }

    /**
     * Removes the bus signal watch (which will remove the bus reference held by
     * the signal watch).
     */
    private void removeWatch() {
        synchronized (lock) {
            if (watchAdded) {
                LOG.fine("Remove watch");
                GSTBUS_API.gst_bus_remove_signal_watch(this);
                watchAdded = false;
            }
        }
    }

    /**
     * Signal emitted when end-of-stream is reached in a pipeline.
     * <p>
     * The application will only receive this message in the PLAYING state and
     * every time it sets a pipeline to PLAYING that is in the EOS state. The
     * application can perform a flushing seek in the pipeline, which will undo
     * the EOS state again.
     *
     * @see #connect(EOS)
     * @see #disconnect(EOS)
     */
    public interface EOS {

        /**
         * Called when a {@link Pipeline} element posts a end-of-stream message.
         *
         * @param source the element which posted the message.
         */
        void endOfStream(GstObject source);
    }

    /**
     * Signal emitted when an error occurs.
     * <p>
     * When the application receives an error message it should stop playback of
     * the pipeline and not assume that more data will be played.
     *
     * @see #connect(ERROR)
     * @see #disconnect(ERROR)
     */
    public interface ERROR {

        /**
         * Called when a {@link Pipeline} element posts an error message.
         *
         * @param source  the element which posted the message.
         * @param code    a numeric code representing the error.
         * @param message a string representation of the error.
         */
        void errorMessage(GstObject source, int code, String message);
    }

    /**
     * Signal emitted when a warning message is delivered.
     *
     * @see #connect(WARNING)
     * @see #disconnect(WARNING)
     */
    public interface WARNING {

        /**
         * Called when a {@link Pipeline} element posts an warning message.
         *
         * @param source  the element which posted the message.
         * @param code    a numeric code representing the warning.
         * @param message a string representation of the warning.
         */
        void warningMessage(GstObject source, int code, String message);
    }

    /**
     * Signal emitted when an informational message is delivered.
     *
     * @see #connect(INFO)
     * @see #disconnect(INFO)
     */
    public interface INFO {

        /**
         * Called when a {@link Pipeline} element posts an informational
         * message.
         *
         * @param source  the element which posted the message.
         * @param code    a numeric code representing the informational message.
         * @param message a string representation of the informational message.
         */
        void infoMessage(GstObject source, int code, String message);
    }

    /**
     * Signal emitted when a new tag is identified on the stream.
     *
     * @see #connect(TAG)
     * @see #disconnect(TAG)
     */
    public interface TAG {

        /**
         * Called when a {@link Pipeline} element finds media meta-data.
         *
         * @param source  the element which posted the message.
         * @param tagList a list of media meta-data.
         */
        void tagsFound(GstObject source, TagList tagList);
    }

    /**
     * Signal emitted when a state change happens.
     *
     * @see #connect(STATE_CHANGED)
     * @see #disconnect(STATE_CHANGED)
     */
    public interface STATE_CHANGED {

        /**
         * Called when a {@link Pipeline} element executes a {@link State}
         * change.
         *
         * @param source  the element which posted the message.
         * @param old     the old state that the element is changing from.
         * @param current the new (current) state the element is changing to.
         * @param pending the pending (target) state.
         */
        void stateChanged(GstObject source, State old, State current, State pending);
    }

    /**
     * Signal emitted when the pipeline is buffering data.
     *
     * @see #connect(BUFFERING)
     * @see #disconnect(BUFFERING)
     */
    public interface BUFFERING {

        /**
         * Called when a {@link Pipeline} element needs to buffer data before it
         * can continue processing.
         * <p>
         * {@code percent} is a value between 0 and 100. A value of 100 means
         * that the buffering completed.
         * <p>
         * When {@code percent} is < 100 the application should PAUSE a PLAYING
         * pipeline. When {@code percent} is 100, the application can set the
         * pipeline (back) to PLAYING. <p>
         * The application must be prepared to receive BUFFERING messages in the
         * PREROLLING state and may only set the pipeline to PLAYING after
         * receiving a message with {@code percent} set to 100, which can happen
         * after the pipeline completed prerolling.
         *
         * @param source  the element which posted the message.
         * @param percent the percentage of buffering that has completed.
         */
        void bufferingData(GstObject source, int percent);
    }

    /**
     * Signal sent when a new duration message is posted by an element that know
     * the duration of a stream in a specific format.
     * <p>
     * This message is received by bins and is used to calculate the total
     * duration of a pipeline.
     * <p>
     * Elements may post a duration message with a duration of
     * {@link ClockTime#NONE} to indicate that the duration has changed and the
     * cached duration should be discarded. The new duration can then be
     * retrieved via a query. The application can get the new duration with a
     * duration query.
     *
     * @see #connect(DURATION_CHANGED)
     * @see #disconnect(DURATION_CHANGED)
     */
    public interface DURATION_CHANGED {

        /**
         * Called when a new duration message is posted on the Bus.
         *
         * @param source the element which posted the message.
         */
        void durationChanged(GstObject source);
    }

    /**
     * This message is posted by elements that start playback of a segment as a
     * result of a segment seek.
     * <p>
     * This message is not received by the application but is used for
     * maintenance reasons in container elements.
     */
    public interface SEGMENT_START {

        void segmentStart(GstObject source, Format format, long position);
    }

    /**
     * Signal emitted when the pipeline has completed playback of a segment.
     * <p>
     * This message is posted by elements that finish playback of a segment as a
     * result of a segment seek. This message is received by the application
     * after all elements that posted a {@link SEGMENT_START} have posted
     * segment-done.
     *
     * @see #connect(SEGMENT_DONE)
     * @see #disconnect(SEGMENT_DONE)
     */
    public interface SEGMENT_DONE {

        /**
         * Called when a segment-done message has been posted.
         *
         * @param source   the element which posted the message.
         * @param format   the format of the position being done.
         * @param position the position of the segment being done.
         */
        void segmentDone(GstObject source, Format format, long position);
    }

    /**
     * Signal emitted by elements when they complete an ASYNC state change.
     * <p>
     * Applications will only receive this message from the top level pipeline.
     * </p>
     *
     * @see #connect(ASYNC_DONE)
     * @see #disconnect(ASYNC_DONE)
     */
    public interface ASYNC_DONE {

        /**
         * Called when a segment-done message has been posted.
         *
         * @param source the element which posted the message.
         */
        void asyncDone(GstObject source);
    }

    /**
     * Catch all signals emitted on the Bus.
     * <p>
     * The signal handler will be called asynchronously from the thread that
     * posted the message on the Bus.
     *
     * @see #connect(MESSAGE)
     * @see #disconnect(MESSAGE)
     */
    public interface MESSAGE {

        /**
         * Called when a {@link Element} posts a {@link Message} on the Bus.
         *
         * @param bus     the Bus the message was posted on.
         * @param message the message that was posted.
         */
        void busMessage(Bus bus, Message message);
    }

    private static class MessageProxy<T> {

        private final MessageType type;
        private final Class<T> listenerClass;
        private final Object listener;
        private final BusCallback callback;

        MessageProxy(MessageType type, Class<T> listenerClass, T listener, BusCallback callback) {
            this.type = type;
            this.listenerClass = listenerClass;
            this.listener = listener;
            this.callback = callback;
        }

        void busMessage(final GstBusPtr bus, final GstMessagePtr msg) {
            if (type == MessageType.ANY || type.intValue() == msg.getMessageType()) {
                callback.callback(bus, msg, null);
            }
        }
    }

    private static class SyncCallback implements GstBusAPI.BusSyncHandler {

        {
            Native.setCallbackThreadInitializer(this,
                    new CallbackThreadInitializer(true,
                            Boolean.getBoolean("glib.detachCallbackThreads"),
                            "GstBus"));
        }

        @Override
        public BusSyncReply callback(final GstBusPtr busPtr, final GstMessagePtr msgPtr, Pointer userData) {
            Bus bus = Natives.objectFor(busPtr, Bus.class, true, true);
            // volatile - use local reference
            BusSyncHandler syncHandler = bus.syncHandler;
            if (syncHandler != null) {
                Message msg = Natives.objectFor(msgPtr, Message.class, true, true);
                BusSyncReply reply = syncHandler.syncMessage(msg);
                if (reply != BusSyncReply.DROP) {
                    Gst.getExecutor().execute(() -> bus.dispatchMessage(busPtr, msgPtr));
                } else {
                    // not calling dispatch message so unref here
                    GSTMINIOBJECT_API.gst_mini_object_unref(msgPtr);
                }
            } else {
                Gst.getExecutor().execute(() -> bus.dispatchMessage(busPtr, msgPtr));
            }
            return BusSyncReply.DROP;
        }
    }

}
