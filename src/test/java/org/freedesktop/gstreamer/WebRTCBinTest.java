package org.freedesktop.gstreamer;

import org.freedesktop.gstreamer.glib.NativeEnum;
import org.freedesktop.gstreamer.webrtc.WebRTCICEGatheringState;
import org.freedesktop.gstreamer.webrtc.WebRTCPeerConnectionState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WebRTCBinTest {
    @Test
    public void connectionStateTest() {
        assertEquals(WebRTCPeerConnectionState.NEW, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 0));
        assertEquals(WebRTCPeerConnectionState.CONNECTING, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 1));
        assertEquals(WebRTCPeerConnectionState.CONNECTED, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 2));
        assertEquals(WebRTCPeerConnectionState.DISCONNECTED, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 3));
        assertEquals(WebRTCPeerConnectionState.FAILED, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 4));
        assertEquals(WebRTCPeerConnectionState.CLOSED, NativeEnum.fromInt(WebRTCPeerConnectionState.class, 5));
    }

    @Test
    public void iceGatheringStateTest() {
        assertEquals(WebRTCICEGatheringState.NEW, NativeEnum.fromInt(WebRTCICEGatheringState.class, 0));
        assertEquals(WebRTCICEGatheringState.GATHERING, NativeEnum.fromInt(WebRTCICEGatheringState.class, 1));
        assertEquals(WebRTCICEGatheringState.COMPLETE, NativeEnum.fromInt(WebRTCICEGatheringState.class, 2));
    }
}
