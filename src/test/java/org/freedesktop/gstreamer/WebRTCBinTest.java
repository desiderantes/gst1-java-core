package org.freedesktop.gstreamer;

import org.freedesktop.gstreamer.glib.NativeEnum;
import org.freedesktop.gstreamer.webrtc.WebRTCICEGatheringState;
import org.freedesktop.gstreamer.webrtc.WebRTCPeerConnectionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WebRTCBinTest {
    @Test
    public void connectionStateTest() {
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 0), WebRTCPeerConnectionState.NEW);
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 1), WebRTCPeerConnectionState.CONNECTING);
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 2), WebRTCPeerConnectionState.CONNECTED);
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 3), WebRTCPeerConnectionState.DISCONNECTED);
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 4), WebRTCPeerConnectionState.FAILED);
        assertEquals(NativeEnum.fromInt(WebRTCPeerConnectionState.class, 5), WebRTCPeerConnectionState.CLOSED);
    }

    @Test
    public void iceGatheringStateTest() {
        assertEquals(NativeEnum.fromInt(WebRTCICEGatheringState.class, 0), WebRTCICEGatheringState.NEW);
        assertEquals(NativeEnum.fromInt(WebRTCICEGatheringState.class, 1), WebRTCICEGatheringState.GATHERING);
        assertEquals(NativeEnum.fromInt(WebRTCICEGatheringState.class, 2), WebRTCICEGatheringState.COMPLETE);
    }
}
