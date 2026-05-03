/*
 * Copyright (c) 2019 Neil C Smith
 * Copyright (c) 2007, 2008 Wayne Meissner
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
package org.freedesktop.gstreamer.event;

import org.freedesktop.gstreamer.MiniObject;
import org.freedesktop.gstreamer.Structure;
import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstEventAPI;
import org.freedesktop.gstreamer.lowlevel.ReferenceManager;
import org.freedesktop.gstreamer.lowlevel.annotations.HasSubtype;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.freedesktop.gstreamer.lowlevel.GstEventAPI.GSTEVENT_API;

/**
 * Base type of all events.
 * <p>
 * See upstream documentation at
 * <a href="https://gstreamer.freedesktop.org/data/doc/gstreamer/stable/gstreamer/html/GstEvent.html"
 * >https://gstreamer.freedesktop.org/data/doc/gstreamer/stable/gstreamer/html/GstEvent.html</a>
 * <p>
 * Events are passed between elements in parallel to the data stream. Some
 * events are serialized with buffers, others are not. Some events only travel
 * downstream, others only upstream. Some events can travel both upstream and
 * downstream.
 * <p>
 * The events are used to signal special conditions in the datastream such as
 * EOS (end of stream) or the start of a new stream-segment.
 * <p>
 * Events are also used to flush the pipeline of any pending data.
 *
 * @see org.freedesktop.gstreamer.Pad#pushEvent(Event)
 * @see org.freedesktop.gstreamer.Pad#sendEvent(Event)
 * @see org.freedesktop.gstreamer.Element#sendEvent(Event)
 */
@HasSubtype
public class Event extends MiniObject {

    public static final String GTYPE_NAME = "GstEvent";

    private static final Map<EventType, Function<Initializer, Event>> TYPE_MAP = new EnumMap<>(Map.ofEntries(
        Map.entry(EventType.BUFFERSIZE, BufferSizeEvent::new),
        Map.entry(EventType.EOS, EOSEvent::new),
        Map.entry(EventType.CAPS, CapsEvent::new),
        Map.entry(EventType.RECONFIGURE, ReconfigureEvent::new),
        Map.entry(EventType.STREAM_START, StreamStartEvent::new),
        Map.entry(EventType.LATENCY, LatencyEvent::new),
        Map.entry(EventType.FLUSH_START, FlushStartEvent::new),
        Map.entry(EventType.FLUSH_STOP, FlushStopEvent::new),
        Map.entry(EventType.NAVIGATION, NavigationEvent::new),
        Map.entry(EventType.SEGMENT, SegmentEvent::new),
        Map.entry(EventType.SEEK, SeekEvent::new),
        Map.entry(EventType.TAG, TagEvent::new),
        Map.entry(EventType.QOS, QOSEvent::new),
        Map.entry(EventType.STEP, StepEvent::new)));

    /**
     * This constructor is for internal use only.
     *
     * @param init initialization data.
     */
    Event(Initializer init) {
        super(init);
    }

    private static Event create(Initializer init) {
        GstEventAPI.EventStruct struct = new GstEventAPI.EventStruct(init.ptr.getPointer());
        EventType type = (EventType) struct.readField("type");
        return TYPE_MAP.getOrDefault(type, Event::new).apply(init);
    }

    /**
     * Gets the structure containing the data in this event.
     *
     * @return a structure.
     */
    public Structure getStructure() {
        return ReferenceManager.addKeepAliveReference(GSTEVENT_API.gst_event_get_structure(this), this);
    }

    public static class Types implements TypeProvider {

        @Override
        public Stream<TypeRegistration<?>> types() {
            return Stream.of(
                    Natives.registration(Event.class, GTYPE_NAME, Event::create)
            );
        }

    }

}
