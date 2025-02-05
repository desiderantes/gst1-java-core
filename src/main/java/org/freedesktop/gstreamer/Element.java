/*
 * Copyright (c) 2021 Neil C Smith
 * Copyright (c) 2019 Christophe Lafolet
 * Copyright (c) 2009 Levente Farkas
 * Copyright (C) 2007 Wayne Meissner
 * Copyright (C) 1999,2000 Erik Walthinsen <omega@cse.ogi.edu>
 *                    2004 Wim Taymans <wim@fluendo.com>
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

import org.freedesktop.gstreamer.event.Event;
import org.freedesktop.gstreamer.event.SeekEvent;
import org.freedesktop.gstreamer.event.SeekFlags;
import org.freedesktop.gstreamer.event.SeekType;
import org.freedesktop.gstreamer.glib.NativeFlags;
import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstAPI.GstCallback;
import org.freedesktop.gstreamer.lowlevel.GstContextPtr;
import org.freedesktop.gstreamer.lowlevel.GstIteratorPtr;
import org.freedesktop.gstreamer.lowlevel.GstObjectPtr;
import org.freedesktop.gstreamer.message.Message;
import org.freedesktop.gstreamer.query.Query;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.freedesktop.gstreamer.lowlevel.GObjectAPI.GOBJECT_API;
import static org.freedesktop.gstreamer.lowlevel.GstElementAPI.GSTELEMENT_API;

/**
 * Abstract base class for all pipeline elements.
 * <p>
 * See upstream documentation at
 * <a href="https://gstreamer.freedesktop.org/documentation/gstreamer/gstelement.html"
 * >https://gstreamer.freedesktop.org/documentation/gstreamer/gstelement.html</a>
 * <p>
 * Element is the abstract base class needed to construct an element that can be
 * used in a GStreamer pipeline. Please refer to the plugin writers guide for
 * more information on creating Element subclasses.
 * <p>
 * The name of a Element can be retrieved with {@link #getName} and set with
 * {@link #setName}.
 * <p>
 * All elements have pads (of the type {@link Pad}). These pads link to pads on
 * other elements. {@link Buffer}s flow between these linked pads. An Element
 * has a list of {@link Pad} structures for all their input (or sink) and output
 * (or source) pads. Core and plug-in writers can add and remove pads with
 * {@link #addPad} and {@link #removePad}.
 * <p>
 * A pad of an element can be retrieved by name with {@link #getPad}. An list of
 * all pads can be retrieved with {@link #getPads}.
 * <p>
 * Elements can be linked through their pads. If the link is straightforward,
 * use the {@link #link} convenience function to link two elements, or
 * {@link #linkMany} for more elements in a row.
 * <p>
 * For finer control, use {@link #linkPads} and {@link #linkPadsFiltered} to
 * specify the pads to link on each element by name.
 * <p>
 * Each element has a state (see {@link State}). You can get and set the state
 * of an element with {@link #getState} and {@link #setState}.
 */
public class Element extends GstObject {

    public static final String GTYPE_NAME = "GstElement";

    /**
     * Creates a new instance of Element. This constructor is used internally.
     *
     * @param init internal initialization data.
     */
    protected Element(Initializer init) {
        super(init);
    }

    Element(Handle handle, boolean needRef) {
        super(handle, needRef);
    }

    /**
     * Creates an instance of the required element type, but does not wrap it in
     * a proxy.
     *
     * @param factoryName The name of the factory to use to produce the Element
     * @param elementName The name to assign to the created Element
     * @return a raw element.
     */
    protected static Initializer makeRawElement(String factoryName, String elementName) {
        return Natives.initializer(ElementFactory.makeRawElement(factoryName, elementName));
    }

    /**
     * Link together a list of elements.
     * <p>
     * Make sure you have added your elements to a bin or pipeline with
     * {@link Bin#add} or {@link Bin#addMany} before trying to link them.
     *
     * @param elements The list of elements to link together.
     * @return true if all elements successfully linked.
     */
    public static boolean linkMany(Element... elements) {
        return GSTELEMENT_API.gst_element_link_many(elements);
    }

    /**
     * Unlink a list of elements.
     *
     * @param elements The list of elements to link together
     */
    public static void unlinkMany(Element... elements) {
        GSTELEMENT_API.gst_element_unlink_many(elements);
    }


//    /**
//     * Chain together a series of elements, with this element as the first in the list. 
//     * <p>
//     * Make sure you have added your elements to a bin or pipeline with
//     * {@link Bin#add} or {@link Bin#addMany} before trying to link them.
//     *
//     * @param elems The list of elements to be linked.
//     * @return true if the elements could be linked, false otherwise.
//     */
//    public boolean link(Element... elems) {
//        // Its much more efficient to copy the array and let the native code do the linking
//        Element[] list = new Element[elems.length + 1];
//        list[0] = this;
//        System.arraycopy(elems, 0, list, 1, elems.length);
//        return linkMany(list);
//    }

    /**
     * Link together source and destination pads of two elements.
     * <p>
     * A side effect is that if one of the pads has no parent, it becomes a
     * child of the parent of the other element. If they have different parents,
     * the link fails.
     *
     * @param src         The {@link Element} containing the source {@link Pad}.
     * @param srcPadName  The name of the source {@link Pad}. Can be null for any
     *                    pad.
     * @param dest        The {@link Element} containing the destination {@link Pad}.
     * @param destPadName The name of the destination {@link Pad}. Can be null
     *                    for any pad.
     * @return true if the pads were successfully linked.
     */
    public static boolean linkPads(Element src, String srcPadName, Element dest, String destPadName) {
        return GSTELEMENT_API.gst_element_link_pads(src, srcPadName, dest, destPadName);
    }

    /**
     * Link together source and destination pads of two elements. A side effect
     * is that if one of the pads has no parent, it becomes a child of the
     * parent of the other element. If they have different parents, the link
     * fails. If caps is not null, makes sure that the caps of the link is a
     * subset of caps.
     *
     * @param src         The {@link Element} containing the source {@link Pad}.
     * @param srcPadName  The name of the source {@link Pad}. Can be null for any
     *                    pad.
     * @param dest        The {@link Element} containing the destination {@link Pad}.
     * @param destPadName The name of the destination {@link Pad}. Can be null
     *                    for any pad.
     * @param caps        The {@link Caps} to use to filter the link.
     * @return true if the pads were successfully linked.
     */
    public static boolean linkPadsFiltered(Element src, String srcPadName,
                                           Element dest, String destPadName, Caps caps) {
        return GSTELEMENT_API.gst_element_link_pads_filtered(src, srcPadName, dest, destPadName, caps);
    }

    /**
     * Unlink source and destination pads of two elements.
     *
     * @param src         The {@link Element} containing the source {@link Pad}.
     * @param srcPadName  The name of the source {@link Pad}.
     * @param dest        The {@link Element} containing the destination {@link Pad}.
     * @param destPadName The name of the destination {@link Pad}.
     */
    public static void unlinkPads(Element src, String srcPadName, Element dest, String destPadName) {
        GSTELEMENT_API.gst_element_unlink_pads(src, srcPadName, dest, destPadName);
    }

    /**
     * Links this element to another element. The link must be from source to
     * destination; the other direction will not be tried.
     * <p>
     * The function looks for existing pads that aren't linked yet. It will
     * request new pads if necessary. Such pads need to be released manualy when
     * unlinking. If multiple links are possible, only one is established.
     * <p>
     * Make sure you have added your elements to a bin or pipeline with
     * {@link Bin#add} or {@link Bin#addMany} before trying to link them.
     * <p>
     * See {@link Element#linkPadsFiltered} if you need more control about
     * which pads are selected for linking.
     *
     * @param dest The {@link Element} containing the destination pad.
     * @return true if the elements could be linked, false otherwise.
     */
    public boolean link(Element dest) {
        return GSTELEMENT_API.gst_element_link(this, dest);
    }

    /**
     * Links this element to another element using the given caps as filtercaps.
     * The link must be from source to destination; the other direction will
     * not be tried.
     * <p>
     * The function looks for existing pads that aren't linked yet. It will
     * request new pads if necessary. Such pads need to be released manualy when
     * unlinking. If multiple links are possible, only one is established.
     * <p>
     * Make sure you have added your elements to a bin or pipeline with
     * {@link Bin#add} or {@link Bin#addMany} before trying to link them.
     * <p>
     * See {@link Element#linkPadsFiltered} if you need more control about
     * which pads are selected for linking.
     *
     * @param dest   The {@link Element} containing the destination pad.
     * @param filter The {@link Caps} to filter the link, or Null for no filter.
     * @return true if the elements could be linked, false otherwise.
     */
    public boolean linkFiltered(Element dest, Caps filter) {
        return GSTELEMENT_API.gst_element_link_filtered(this, dest, filter);
    }

    /**
     * Unlinks all source pads of this source element with all sink pads of the
     * sink element to which they are linked.
     * <p>
     * If the link has been made using {@link #link}, it could have created an
     * requestpad, which has to be released using
     * gst_element_release_request_pad().
     *
     * @param dest The sink Element to unlink.
     */
    public void unlink(Element dest) {
        GSTELEMENT_API.gst_element_unlink(this, dest);
    }

    /**
     * Tests if the Element is currently playing.
     *
     * @return true if the Element is currently playing
     */
    public boolean isPlaying() {
        return getState() == State.PLAYING;
    }

    /**
     * Tells the Element to start playing the media stream. Equivalent to
     * calling {@link #setState(org.freedesktop.gstreamer.State)} with
     * {@link State#PLAYING}.
     *
     * @return the status of the element's state change.
     */
    public StateChangeReturn play() {
        return setState(State.PLAYING);
    }

    /**
     * Tells the Element to set ready the media stream. Equivalent to calling
     * {@link #setState(org.freedesktop.gstreamer.State)} with
     * {@link State#READY}.
     *
     * @return the status of the element's state change.
     */
    public StateChangeReturn ready() {
        return setState(State.READY);
    }

    /**
     * Tells the Element to pause playing the media stream. Equivalent to
     * calling {@link #setState(org.freedesktop.gstreamer.State)} with
     * {@link State#PAUSED}.
     *
     * @return the status of the element's state change.
     */
    public StateChangeReturn pause() {
        return setState(State.PAUSED);
    }

    /**
     * Tells the Element to pause playing the media stream. Equivalent to
     * calling {@link #setState(org.freedesktop.gstreamer.State)} with
     * {@link State#NULL}.
     *
     * @return the status of the element's state change.
     */
    public StateChangeReturn stop() {
        return setState(State.NULL);
    }

    /**
     * Sets the state of the element.
     * <p>
     * This method will try to set the requested state by going through all the
     * intermediary states.
     * <p>
     * This function can return {@link StateChangeReturn#ASYNC}, in which case
     * the element will perform the remainder of the state change asynchronously
     * in another thread.
     * <p>
     * An application can use {@link #getState} to wait for the completion of
     * the state change or it can wait for a state change message on the bus.
     *
     * @param state the element's new {@link State}.
     * @return the status of the element's state change.
     */
    public StateChangeReturn setState(State state) {
        return GSTELEMENT_API.gst_element_set_state(this, state);
    }

    /**
     * Locks the state of an element, so state changes of the parent don't
     * affect this element anymore.
     *
     * @param locked_state true to lock the element's {@link State}.
     * @return true if the state was changed, false if bad parameters were given
     * or the elements state-locking needed no change.
     */
    public boolean setLockedState(boolean locked_state) {
        return GSTELEMENT_API.gst_element_set_locked_state(this, locked_state);
    }

    /**
     * Gets the state of the element.
     * <p>
     * This method will wait until any async state change has completed.
     *
     * @return The {@link State} the Element is currently in.
     */
    public State getState() {
        return getState(-1);
    }

    /**
     * Gets the state of the element.
     * <p>
     * For elements that performed an ASYNC state change, as reported by
     * {@link #setState}, this function will block up to the specified timeout
     * value for the state change to complete.
     *
     * @param timeout the amount of time to wait.
     * @param units   the units of the <tt>timeout</tt>.
     * @return The {@link State} the Element is currently in.
     */
    public State getState(long timeout, TimeUnit units) {
        State[] state = new State[1];
        GSTELEMENT_API.gst_element_get_state(this, state, null, units.toNanos(timeout));
        return state[0];
    }

    /**
     * Gets the state of the element.
     * <p>
     * For elements that performed an ASYNC state change, as reported by
     * {@link #setState}, this function will block up to the specified timeout
     * value for the state change to complete.
     *
     * @param timeout The amount of time in nanoseconds to wait.
     * @return The {@link State} the Element is currently in.
     */
    public State getState(long timeout) {
        State[] state = new State[1];
        GSTELEMENT_API.gst_element_get_state(this, state, null, timeout);
        return state[0];
    }

    /**
     * Gets the state of the element.
     * <p>
     * For elements that performed an ASYNC state change, as reported by
     * {@link #setState}, this function will block up to the specified timeout
     * value for the state change to complete.
     *
     * @param timeout The amount of time in nanoseconds to wait.
     * @param states  an array to store the states in. Must be of sufficient size
     *                to hold two elements.
     */
    public void getState(long timeout, State[] states) {
        State[] state = new State[1];
        State[] pending = new State[1];
        GSTELEMENT_API.gst_element_get_state(this, state, pending, timeout);
        states[0] = state[0];
        states[1] = pending[0];
    }

    /**
     * Tries to change the state of the element to the same as its parent. If
     * this function returns false, the state of element is undefined.
     *
     * @return true, if the element's state could be synced to the parent's
     * state. MT safe.
     */
    public boolean syncStateWithParent() {
        return GSTELEMENT_API.gst_element_sync_state_with_parent(this);
    }

    /**
     * Sets the {@link Caps} on this Element.
     *
     * @param caps the new Caps to set.
     */
    public void setCaps(Caps caps) {
        GOBJECT_API.g_object_set(this, "caps", caps);
    }

    /**
     * Retrieves a pad from the element by name. This version only retrieves
     * already-existing (i.e. 'static') pads.
     *
     * @param padname The name of the {@link Pad} to get.
     * @return The requested {@link Pad} if found, otherwise null.
     */
    public Pad getStaticPad(String padname) {
        return GSTELEMENT_API.gst_element_get_static_pad(this, padname);
    }

    /**
     * Retrieves a list of the element's pads.
     *
     * @return the List of {@link Pad}s.
     */
    public List<Pad> getPads() {
        return padList(GSTELEMENT_API.gst_element_iterate_pads(this));
    }

    /**
     * Retrieves a list of the element's source pads.
     *
     * @return the List of {@link Pad}s.
     */
    public List<Pad> getSrcPads() {
        return padList(GSTELEMENT_API.gst_element_iterate_src_pads(this));
    }

    /**
     * Retrieves a list of the element's sink pads.
     *
     * @return the List of {@link Pad}s.
     */
    public List<Pad> getSinkPads() {
        return padList(GSTELEMENT_API.gst_element_iterate_sink_pads(this));
    }

    private List<Pad> padList(GstIteratorPtr iter) {
        return GstIterator.asList(iter, Pad.class);
    }

    /**
     * Adds a {@link Pad} (link point) to the Element. The Pad's parent will be
     * set to this element.
     * <p>
     * Pads are not automatically activated so elements should perform the
     * needed steps to activate the pad in case this pad is added in the PAUSED
     * or PLAYING state. See {@link Pad#setActive} for more information about
     * activating pads.
     * <p>
     * This function will emit the {@link PAD_ADDED} signal on the element.
     *
     * @param pad The {@link Pad} to add.
     * @return true if the pad could be added. This function can fail when a pad
     * with the same name already existed or the pad already had another parent.
     */
    public boolean addPad(Pad pad) {
        return GSTELEMENT_API.gst_element_add_pad(this, pad);
    }

    /**
     * Retrieves a pad from the element by name. This version only retrieves
     * request pads. The pad must be released with {@link #releaseRequestPad}.
     *
     * @param name the name of the request {@link Pad} to retrieve.
     * @return the requested Pad if found, otherwise <tt>null</tt>. Release
     * using {@link #releaseRequestPad} after usage.
     */
    public Pad getRequestPad(String name) {
        return GSTELEMENT_API.gst_element_get_request_pad(this, name);
    }

    /**
     * Frees the previously requested pad obtained via {@link #getRequestPad}.
     *
     * @param pad the pad to release.
     */
    public void releaseRequestPad(Pad pad) {
        GSTELEMENT_API.gst_element_release_request_pad(this, pad);
    }

    /**
     * Remove a {@link Pad} from the element.
     * <p>
     * This method is used by plugin developers and should not be used by
     * applications. Pads that were dynamically requested from elements with
     * {@link #getRequestPad} should be released with the
     * {@link #releaseRequestPad} function instead.
     * <p>
     * Pads are not automatically deactivated so elements should perform the
     * needed steps to deactivate the pad in case this pad is removed in the
     * PAUSED or PLAYING state. See {@link Pad#setActive} for more information
     * about deactivating pads.
     * <p>
     * This function will emit the {@link PAD_REMOVED} signal on the element.
     *
     * @param pad The {@link Pad} to remove.
     * @return true if the pad could be removed. Can return false if the pad
     * does not belong to the provided element.
     */
    public boolean removePad(Pad pad) {
        return GSTELEMENT_API.gst_element_remove_pad(this, pad);
    }

    /**
     * Retrieves the factory that was used to create this element.
     *
     * @return the {@link ElementFactory} used for creating this element.
     */
    public ElementFactory getFactory() {
        return GSTELEMENT_API.gst_element_get_factory(this);
    }

    /**
     * Get the bus of the element. Note that only a {@link Pipeline} will
     * provide a bus for the application.
     *
     * @return the element's {@link Bus}
     */
    public Bus getBus() {
        return GSTELEMENT_API.gst_element_get_bus(this);
    }

    /**
     * Sends an event to an element.
     * <p>
     * If the element doesn't implement an event handler, the event will be
     * pushed on a random linked sink pad for upstream events or a random linked
     * source pad for downstream events.
     *
     * @param ev The {@link Event} to send.
     * @return true if the event was handled.
     */
    public boolean sendEvent(Event ev) {
        return GSTELEMENT_API.gst_element_send_event(this, ev);
    }

    /**
     * Add a listener for the <code>pad-added</code> signal
     *
     * @param listener Listener to be called when a {@link Pad} is added to the
     *                 {@link Element}.
     */
    public void connect(final PAD_ADDED listener) {
        connect(PAD_ADDED.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Element elem, Pad pad) {
                listener.padAdded(elem, pad);
            }
        });
    }

    /**
     * Remove a listener for the <code>pad-added</code> signal
     *
     * @param listener The listener that was previously added.
     */
    public void disconnect(PAD_ADDED listener) {
        disconnect(PAD_ADDED.class, listener);
    }

    /**
     * Add a listener for the <code>pad-added</code> signal
     *
     * @param listener Listener to be called when a {@link Pad} is removed from
     *                 the {@link Element}.
     */
    public void connect(final PAD_REMOVED listener) {
        connect(PAD_REMOVED.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Element elem, Pad pad) {
                listener.padRemoved(elem, pad);
            }
        });
    }

    /**
     * Remove a listener for the <code>pad-removed</code> signal
     *
     * @param listener The listener that was previously added.
     */
    public void disconnect(PAD_REMOVED listener) {
        disconnect(PAD_REMOVED.class, listener);
    }

    /**
     * Add a listener for the <code>no-more-pads</code> signal
     *
     * @param listener Listener to be called when the {@link Element} will has
     *                 finished generating dynamic pads.
     */
    public void connect(final NO_MORE_PADS listener) {
        connect(NO_MORE_PADS.class, listener, new GstCallback() {
            @SuppressWarnings("unused")
            public void callback(Element elem) {
                listener.noMorePads(elem);
            }
        });
    }

    /**
     * Remove a listener for the <code>no-more-pads</code> signal
     *
     * @param listener The listener that was previously added.
     */
    public void disconnect(NO_MORE_PADS listener) {
        disconnect(NO_MORE_PADS.class, listener);
    }

    /**
     * Posts a {@link Message} on the element's {@link Bus}.
     *
     * @param message the Message to post.
     * @return <tt>true</tt> if the message was posted, <tt>false</tt> if the
     * element does not have a {@link Bus}.
     */
    public boolean postMessage(Message message) {
        return GSTELEMENT_API.gst_element_post_message(this, message);
    }

    /**
     * Gets the currently configured clock of the element.
     *
     * @return the clock of the element.
     */
    public Clock getClock() {
        return GSTELEMENT_API.gst_element_get_clock(this);
    }

    /**
     * Returns the base time of the element. The base time is the absolute time
     * of the clock when this element was last put to PLAYING. Subtracting the
     * base time from the clock time gives the stream time of the element.
     *
     * @return the base time of the element
     */
    public long getBaseTime() {
        return GSTELEMENT_API.gst_element_get_base_time(this);
    }

    /**
     * Set the base time of an element.
     *
     * @param time the base time to set
     * @see #getBaseTime()
     */
    public void setBaseTime(long time) {
        GSTELEMENT_API.gst_element_set_base_time(this, time);
    }

    /**
     * Returns the start time of this element.
     * <p>
     * The start time is the running time of the clock when this element was
     * last put to {@link State#PAUSED}. Usually the start_time is managed by a
     * toplevel element such as {@link Pipeline}.
     * <p>
     * MT safe.
     *
     * @return the start time of this element.
     */
    public long getStartTime() {
        return GSTELEMENT_API.gst_element_get_start_time(this);
    }

    /**
     * Set the start time of an element. The start time of the element is the
     * running time of the element when it last went to the {@link State#PAUSED}
     * state. In {@link State#READY} or after a flushing seek, it is set to 0.
     * <p>
     * Toplevel elements like GstPipeline will manage the start_time and
     * base_time on its children. Setting the start_time to
     * {@link long#NONE} on such a toplevel element will disable the
     * distribution of the base_time to the children and can be useful if the
     * application manages the base_time itself, for example if you want to
     * synchronize capture from multiple pipelines, and you can also ensure that
     * the pipelines have the same clock.
     * <p>
     * MT safe.
     *
     * @param time the start time to set
     * @see #getStartTime()
     */
    public void setStartTime(long time) {
        GSTELEMENT_API.gst_element_set_start_time(this, time);
    }

    /**
     * Performs a query on the element.
     * <p>
     * For elements that don't implement a query handler, this function forwards
     * the query to a random srcpad or to the peer of a random linked sinkpad of
     * this element.
     * <p>
     * Please note that some queries might need a running pipeline to work.
     *
     * @param query the Query to perform
     * @return true if the query could be performed
     */
    public boolean query(Query query) {
        return GSTELEMENT_API.gst_element_query(this, query);
    }

    /**
     * Sets the context of the element.
     *
     * @param context the Context to set.
     */
    public void setContext(Context context) {
        GstContextPtr gstContextPtr = Natives.getPointer(context).as(GstContextPtr.class, GstContextPtr::new);
        GSTELEMENT_API.gst_element_set_context(this, gstContextPtr);
    }

    /**
     * Gets the context with the context_type set on the element or NULL.
     *
     * @param context_type
     * @return a context or NULL
     */
    public Context getContext(String context_type) {
        GstContextPtr gstContextPtr = GSTELEMENT_API.gst_element_get_context(this, context_type);
        return gstContextPtr != null ? Natives.callerOwnsReturn(gstContextPtr, Context.class) : null;
    }

    /**
     * Queries an element (usually top-level pipeline or playbin element) for
     * the total stream duration in nanoseconds. This query will only work once
     * the pipeline is prerolled (i.e. reached PAUSED or PLAYING state). The
     * application will receive an ASYNC_DONE message on the pipeline bus when
     * that is the case.
     * <p>
     * If the duration changes for some reason, you will get a DURATION_CHANGED
     * message on the pipeline bus, in which case you should re-query the
     * duration using this function.
     *
     * @param format the {@code Format} to return the duration in
     * @return the total duration of the current media stream, or -1 if the
     * query failed
     */
    public long queryDuration(Format format) {
        long[] dur = {0};
        return GSTELEMENT_API.gst_element_query_duration(this, format, dur) ? dur[0] : -1L;
    }

    /**
     * Queries an element (usually top-level pipeline or playbin element) for
     * the stream position in nanoseconds. This will be a value between 0 and
     * the stream duration (if the stream duration is known). This query will
     * usually only work once the pipeline is prerolled (i.e. reached PAUSED or
     * PLAYING state). The application will receive an ASYNC_DONE message on the
     * pipeline bus when that is the case.
     *
     * @param format The {@link Format} to return the position in
     * @return the current position or -1 if the query failed.
     */
    public long queryPosition(Format format) {
        long[] pos = {0};
        return GSTELEMENT_API.gst_element_query_position(this, format, pos) ? pos[0] : -1L;
    }

    /**
     * Sends a seek event to an element. See {@link SeekEvent} for the details
     * of the parameters. The seek event is sent to the element using the native
     * equivalent of {@link #sendEvent(org.freedesktop.gstreamer.event.Event)}.
     *
     * @param rate      the new playback rate
     * @param format    the format of the seek values
     * @param seekFlags the seek flags
     * @param startType the type and flags for the new start position
     * @param start     the value of the new start position
     * @param stopType  the type and flags for the new stop position
     * @param stop      the value of the new stop position
     * @return true if seek operation succeeded. Flushing seeks will trigger a
     * preroll, which will emit an ASYNC_DONE message
     */
    public boolean seek(double rate, Format format, Set<SeekFlags> seekFlags,
                        SeekType startType, long start, SeekType stopType, long stop) {

        return GSTELEMENT_API.gst_element_seek(this, rate, format,
                NativeFlags.toInt(seekFlags), startType, start, stopType, stop);
    }

    /**
     * Simple API to perform a seek on the given element, meaning it just seeks
     * to the given position relative to the start of the stream. For more
     * complex operations like segment seeks (e.g. for looping) or changing the
     * playback rate or seeking relative to the last configured playback segment
     * you should use
     * {@link #seek(double, org.freedesktop.gstreamer.Format, java.util.Set, org.freedesktop.gstreamer.event.SeekType, long, org.freedesktop.gstreamer.event.SeekType, long)}.
     * <p>
     * In a completely prerolled PAUSED or PLAYING pipeline, seeking is always
     * guaranteed to return TRUE on a seekable media type or FALSE when the
     * media type is certainly not seekable (such as a live stream).
     * <p>
     * Some elements allow for seeking in the READY state, in this case they
     * will store the seek event and execute it when they are put to PAUSED. If
     * the element supports seek in READY, it will always return TRUE when it
     * receives the event in the READY state.
     *
     * @param format       a {@link Format} to seek in, such as {@link Format#TIME}
     * @param seekFlags    seek options; playback applications will usually want to
     *                     use {@link SeekFlags#FLUSH} and {@link SeekFlags#KEY_UNIT} here
     * @param seekPosition position to seek to (relative to the start); if you
     *                     are doing a seek in format TIME this value is in nanoseconds
     * @return true if seek operation succeeded. Flushing seeks will trigger a
     * preroll, which will emit an ASYNC_DONE message
     */
    public boolean seekSimple(Format format, Set<SeekFlags> seekFlags, long seekPosition) {
        return GSTELEMENT_API.gst_element_seek_simple(this, format,
                NativeFlags.toInt(seekFlags), seekPosition);
    }

    /**
     * Signal emitted when an {@link Pad} is added to this {@link Element}
     *
     * @see #connect(PAD_ADDED)
     * @see #disconnect(PAD_ADDED)
     */
    public interface PAD_ADDED {

        /**
         * Called when a new {@link Pad} is added to an Element.
         *
         * @param element the element the pad was added to.
         * @param pad     the pad which was added.
         */
        void padAdded(Element element, Pad pad);
    }

    /**
     * Signal emitted when an {@link Pad} is removed from this {@link Element}
     *
     * @see #connect(PAD_REMOVED)
     * @see #disconnect(PAD_REMOVED)
     */
    public interface PAD_REMOVED {

        /**
         * Called when a new {@link Pad} is removed from an Element.
         *
         * @param element the element the pad was removed from.
         * @param pad     the pad which was removed.
         */
        void padRemoved(Element element, Pad pad);
    }

    /**
     * Signal emitted when this {@link Element} ceases to generated dynamic
     * pads.
     *
     * @see #connect(NO_MORE_PADS)
     * @see #disconnect(NO_MORE_PADS)
     */
    public interface NO_MORE_PADS {

        /**
         * Called when an {@link Element} ceases to generated dynamic pads.
         *
         * @param element the element which posted this message.
         */
        void noMorePads(Element element);
    }

    static class Handle extends GstObject.Handle {

        public Handle(GstObjectPtr ptr, boolean ownsHandle) {
            super(ptr, ownsHandle);
        }

    }

}
