package org.freedesktop.gstreamer;

import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GstContextAPI;
import org.freedesktop.gstreamer.lowlevel.GstContextPtr;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ContextTest {

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("test");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void testConstruction() {
        GstContextAPI contextApi = GstContextAPI.GSTCONTEXT_API;
        String contextType = "whatever";
        try (Context context = new Context(contextType)) {
            GstContextPtr gstContextPtr = Natives.getPointer(context).as(GstContextPtr.class, GstContextPtr::new);

            // Context type.
            assertEquals(contextType, context.getContextType());
            assertTrue(contextApi.gst_context_has_context_type(gstContextPtr, contextType));
            assertFalse(contextApi.gst_context_has_context_type(gstContextPtr, contextType + ".something-else"));

            // Default is persistent.
            assertTrue(contextApi.gst_context_is_persistent(gstContextPtr));

            assertNotNull(context.getWritableStructure());
        }
    }

}
