/*
 * Copyright (c) 2020 Neil C Smith
 * Copyright (c) 2019 Kezhu Wang
 * Copyright (c) 2018 Antonio Morales
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

import org.freedesktop.gstreamer.glib.Natives;
import org.freedesktop.gstreamer.lowlevel.GPointer;
import org.freedesktop.gstreamer.lowlevel.GType;
import org.freedesktop.gstreamer.util.TestAssumptions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class PromiseTest {

    public PromiseTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init(Gst.getVersion(), "PromiseTest");
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        Gst.deinit();
    }

    @Test
    public void testReply() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();

        promise.reply(null);

        PromiseResult promiseStatus = promise.waitResult();

        assertEquals(promiseStatus, PromiseResult.REPLIED, "promise reply state not correct");
    }

    @Test
    public void testInterrupt() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();
        promise.interrupt();

        PromiseResult promiseStatus = promise.waitResult();

        assertEquals(promiseStatus, PromiseResult.INTERRUPTED, "promise reply state not correct");
    }

    @Test
    public void testExpire() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();
        promise.expire();

        PromiseResult promiseStatus = promise.waitResult();

        assertEquals(promiseStatus, PromiseResult.EXPIRED, "promise reply state not correct");
    }

    @Test
    public void testInvalidateReply() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();
        Structure data = new Structure("data");

        assertTrue(Natives.ownsReference(data));
        promise.reply(data);
        assertFalse(Natives.ownsReference(data));
        assertFalse(Natives.validReference(data));
    }

    @Test
    public void testReplyData() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();
        Structure data = new Structure("data", "test", GType.UINT, 1);
        GPointer pointer = Natives.getPointer(data);

        promise.reply(data);
        assertEquals(promise.waitResult(), PromiseResult.REPLIED, "promise state not in replied");

        Structure result = promise.getReply();
        assertEquals(pointer, Natives.getPointer(result), "result of promise does not match reply");
    }

    @Test
    public void testDispose() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise();
        promise.interrupt();
        promise.dispose();
    }

    @Test
    public void testDisposeWithChangeFunc() {
        TestAssumptions.requireGstVersion(1, 14);

        Promise promise = new Promise(new Promise.PROMISE_CHANGE() {
            @Override
            public void onChange(Promise promise) {
            }
        });
        promise.interrupt();
        promise.dispose();
    }

    @Test
    public void testChangeFunctionGC() {
        TestAssumptions.requireGstVersion(1, 14);

        final AtomicBoolean onChangeFired = new AtomicBoolean(false);

        Promise promise = new Promise(new Promise.PROMISE_CHANGE() {
            @Override
            public void onChange(Promise promise) {
                onChangeFired.set(true);
            }
        });
        System.gc();
        System.gc();
        promise.interrupt();
        assertTrue(onChangeFired.get(), "Promise Change callback GC'd");
        promise.dispose();
    }
}
