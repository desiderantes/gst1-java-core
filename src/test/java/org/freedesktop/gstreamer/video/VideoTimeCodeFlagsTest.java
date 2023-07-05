/*
 * Copyright (c) 2020 Petr Lastovka
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
 *
 */
package org.freedesktop.gstreamer.video;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class VideoTimeCodeFlagsTest {
    static Stream<Arguments> data() {
        return Stream.of(
                //Arguments.of(VideoTimeCodeFlags.GST_VIDEO_TIME_CODE_FLAGS_NONE, 0),
                Arguments.of(VideoTimeCodeFlags.GST_VIDEO_TIME_CODE_FLAGS_DROP_FRAME, 1),
                Arguments.of(VideoTimeCodeFlags.GST_VIDEO_TIME_CODE_FLAGS_INTERLACED, 2)
        );
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testIntValue(VideoTimeCodeFlags flags, int intValue) {
        assertEquals(intValue, flags.intValue());
    }
}