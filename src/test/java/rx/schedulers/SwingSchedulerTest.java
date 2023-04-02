/**
 * Copyright 2014 Netflix, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.schedulers;

import io.reactivex.rxjava3.core.Scheduler.Worker;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


/**
 * Executes work on the Swing UI thread.
 * This scheduler should only be used with actions that execute quickly.
 */
public final class SwingSchedulerTest {


    private static void waitForEmptyEventQueue() throws Exception {
        EventQueue.invokeAndWait(() -> {
            // nothing to do, we're just waiting here for the event queue to be emptied
        });
    }

    @Test
    public void testInvalidDelayValues() {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();
        final Runnable action = mock(Runnable.class);

        inner.schedulePeriodically(action, -1L, 100L, TimeUnit.SECONDS);

        inner.schedulePeriodically(action, 100L, -1L, TimeUnit.SECONDS);

        assertThrows(IllegalArgumentException.class, () ->
                inner.schedulePeriodically(action, 1L + Integer.MAX_VALUE, 100L, TimeUnit.MILLISECONDS));

        // TODO why does this exist?
        //assertThrows(IllegalArgumentException.class, () -> inner.schedulePeriodically(action, 100L, 1L + Integer.MAX_VALUE / 1000, TimeUnit.SECONDS));
    }

    @Test
    public void testPeriodicScheduling() throws Exception {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();

        final CountDownLatch latch = new CountDownLatch(4);

        final Runnable innerAction = mock(Runnable.class);
        final Runnable action = () -> {
            try {
                innerAction.run();
                assertTrue(SwingUtilities.isEventDispatchThread());
            } finally {
                latch.countDown();
            }
        };

        inner.schedulePeriodically(action, 50, 200, TimeUnit.MILLISECONDS);

        if (!latch.await(5000, TimeUnit.MILLISECONDS)) {
            fail("timed out waiting for tasks to execute");
        }

        inner.dispose();
        waitForEmptyEventQueue();
        verify(innerAction, times(4)).run();
    }

    @Test
    public void testNestedActions() throws Exception {
        final SwingScheduler scheduler = new SwingScheduler();
        final Worker inner = scheduler.createWorker();

        final Runnable firstStepStart = mock(Runnable.class);
        final Runnable firstStepEnd = mock(Runnable.class);

        final Runnable secondStepStart = mock(Runnable.class);
        final Runnable secondStepEnd = mock(Runnable.class);

        final Runnable thirdStepStart = mock(Runnable.class);
        final Runnable thirdStepEnd = mock(Runnable.class);

        final Runnable firstAction = () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            firstStepStart.run();
            firstStepEnd.run();
        };
        final Runnable secondAction = () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            secondStepStart.run();
            inner.schedule(firstAction);
            secondStepEnd.run();
        };
        final Runnable thirdAction = () -> {
            assertTrue(SwingUtilities.isEventDispatchThread());
            thirdStepStart.run();
            inner.schedule(secondAction);
            thirdStepEnd.run();
        };

        InOrder inOrder = inOrder(firstStepStart, firstStepEnd, secondStepStart, secondStepEnd, thirdStepStart, thirdStepEnd);

        inner.schedule(thirdAction);
        waitForEmptyEventQueue();

        inOrder.verify(thirdStepStart, times(1)).run();
        inOrder.verify(secondStepStart, times(1)).run();
        inOrder.verify(firstStepStart, times(1)).run();
        inOrder.verify(firstStepEnd, times(1)).run();
        inOrder.verify(secondStepEnd, times(1)).run();
        inOrder.verify(thirdStepEnd, times(1)).run();
    }

}
