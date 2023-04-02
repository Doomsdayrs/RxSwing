/**
 * Copyright 2015 Netflix
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
package rx.swing.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import rx.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;


public final class WindowEventSource {
    private WindowEventSource() {
    }

    /**
     * @see rx.observables.SwingObservable#fromWindowEventsOf(Window)
     */
    public static Observable<WindowEvent> fromWindowEventsOf(final Window window) {
        return Observable.create((ObservableOnSubscribe<WindowEvent>) emitter -> {
            final WindowListener windowListener = new WindowListener() {
                @Override
                public void windowOpened(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowClosing(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowClosed(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowIconified(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowDeiconified(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowActivated(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }

                @Override
                public void windowDeactivated(WindowEvent windowEvent) {
                    emitter.onNext(windowEvent);
                }
            };

            window.addWindowListener(windowListener);
            emitter.setDisposable(Disposable.fromAction(() -> window.removeWindowListener(windowListener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }
}
