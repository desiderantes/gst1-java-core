/*
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


import org.freedesktop.gstreamer.glib.MainContextExecutorService;
import org.freedesktop.gstreamer.lowlevel.MainLoop;
import org.junit.jupiter.api.*;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author wayne
 */
public class ExecutorServiceTest {

    private static MainLoop loop;

    public ExecutorServiceTest() {
    }

    @BeforeAll
    public static void setUpClass() throws Exception {
        Gst.init("ExecutorServiceTest");
        (loop = new MainLoop()).startInBackground();
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
        loop.quit();
        Gst.deinit();
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    @Test
    public void execute() {
        final TestExec exec = new TestExec();
        exec.execute(new Runnable() {

            public void run() {
                exec.fired();
            }
        });
        exec.run();
        assertTrue(exec.hasFired(), "Runnable not called");
    }

    @Test
    public void submit() throws Exception {
        final TestExec exec = new TestExec();
        final Integer MAGIC = 0xdeadbeef;
        Callable<Integer> callable = new Callable<Integer>() {

            public Integer call() throws Exception {
                exec.fired();
                return MAGIC;
            }
        };
        Future<Integer> f = exec.exec.submit(callable);
        exec.run();
        assertTrue(exec.hasFired(), "Callable not called");
        assertEquals(MAGIC, f.get(), "Wrong value returned from Callable");

    }

    @Test
    public void oneShotTimeout() {
        final TestExec exec = new TestExec();
        exec.exec.schedule(new Runnable() {

            public void run() {
                exec.fired();
            }
        }, 100, TimeUnit.MILLISECONDS);

        exec.run();
        assertTrue(exec.hasFired(), "Runnable not called");
    }

    @Test
    public void timeoutWithReturnValue() throws Exception {
        final TestExec exec = new TestExec();
        final Integer MAGIC = 0xdeadbeef;
        Callable<Integer> callable = () -> {
            exec.fired();
            return MAGIC;
        };
        Future<Integer> f = exec.exec.schedule(callable, 100, TimeUnit.MILLISECONDS);

        exec.run();
        assertTrue(exec.hasFired(), "Runnable not called");
        assertEquals(MAGIC, f.get(), "Wrong value returned from Callable");
    }

    @Test
    public void periodicTimeout() {
        final TestExec exec = new TestExec();
        final AtomicBoolean called = new AtomicBoolean(false);
        exec.exec.scheduleAtFixedRate(() -> {
            if (called.getAndSet(true)) {
                exec.fired();
            }
        }, 10, 10, TimeUnit.MILLISECONDS);

        exec.run();
        assertTrue(exec.hasFired(), "Runnable not called");
    }

    private static class TestExec {
        final MainContextExecutorService exec = new MainContextExecutorService(Gst.getMainContext());
        final AtomicBoolean fired = new AtomicBoolean(false);
        private final CountDownLatch latch = new CountDownLatch(1);

        public TestExec run() {
            // Create a timer to quit out of the test so it does not hang
            try {
                latch.await(250, TimeUnit.MILLISECONDS);
            } catch (Exception ex) {
            }
            return this;
        }

        public void execute(Runnable run) {
            exec.execute(run);
        }

        public void quit() {
            latch.countDown();
        }

        public void fired() {
            fired.set(true);
            quit();
        }

        public boolean hasFired() {
            return fired.get();
        }
    }
}