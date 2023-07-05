package org.freedesktop.gstreamer.lowlevel;

import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;

/**
 *
 */
public interface GstMetaAPI extends Library {
    GstMetaAPI GST_META_API = GstNative.load(GstMetaAPI.class);


    @Structure.FieldOrder({"flags", "info"})
    class GstMetaStruct extends Structure {
        public int flags;
        public GstMetaInfoStruct.ByReference info;

        int infoOffset() {
            return fieldOffset("info");
        }

        public static final class ByValue extends GstMetaStruct implements Structure.ByValue {
        }

    }

    @Structure.FieldOrder({"api", "type", "size"})
    class GstMetaInfoStruct extends Structure {
        public GType api;
        public GType type;
        public long size;

        public GstMetaInfoStruct() {
        }
        public GstMetaInfoStruct(Pointer p) {
            super(p);
            read();
        }

        int typeOffset() {
            return fieldOffset("type");
        }

        public static class ByReference extends GstMetaInfoStruct implements Structure.ByReference {
        }

    }


}
