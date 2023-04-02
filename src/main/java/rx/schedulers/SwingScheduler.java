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

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

/**
 * Executes work on the Swing UI thread.
 * This scheduler should only be used with actions that execute quickly.
 * <p>
 * If the calling thread is the Swing UI thread, and no delay parameter is
 * provided, the action will run immediately. Otherwise, if the calling
 * thread is NOT the Swing UI thread, the action will be deferred until
 * all pending UI events have been processed.
 */
public final class SwingScheduler extends Scheduler {
    private static final SwingScheduler INSTANCE = new SwingScheduler();

    /* package for unit test */SwingScheduler() {
    }

    public static @NonNull Scheduler getInstance() {
        return INSTANCE;
    }

    private static void assertThatTheDelayIsValidForTheSwingTimer(long delay) throws IllegalArgumentException {
        if (delay < 0 || delay > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(String.format("The swing timer only accepts non-negative delays up to %d milliseconds.", Integer.MAX_VALUE));
        }
    }

    @Override
    public Worker createWorker() {
        return new SwingWorker();
    }

    private static class SwingWorker extends Worker {

        private final CompositeDisposable innerSubscription = new CompositeDisposable();

        @Override
        public Disposable schedule(final Runnable action, long delayTime, TimeUnit unit) {
            long delay = Math.max(0, unit.toMillis(delayTime));
            assertThatTheDelayIsValidForTheSwingTimer(delay);

            if(delayTime == 0){
                return scheduleNow(action);
            }

            class ExecuteOnceAction implements ActionListener {
                private Timer timer;

                private void setTimer(Timer timer) {
                    this.timer = timer;
                }

                @Override
                public void actionPerformed(ActionEvent e) {
                    timer.stop();
                    if (innerSubscription.isDisposed()) {
                        return;
                    }
                    action.run();
                }
            }

            ExecuteOnceAction executeOnce = new ExecuteOnceAction();
            final Timer timer = new Timer((int) delay, executeOnce);
            executeOnce.setTimer(timer);
            timer.start();

            return innerSubscription;
        }

        @Override
        public Disposable schedule(final Runnable action) {
            return scheduleNow(action);
        }

        private Disposable scheduleNow(final Runnable action) {
            final Runnable runnable = () -> {
                if (innerSubscription.isDisposed()) {
                    return;
                }
                action.run();
            };

            if (SwingUtilities.isEventDispatchThread()) {
                runnable.run();
            } else {
                EventQueue.invokeLater(runnable);
            }

            return innerSubscription;
        }

        @Override
        public void dispose() {
            innerSubscription.dispose();

        }

        @Override
        public boolean isDisposed() {
            return innerSubscription.isDisposed();
        }

    }
}