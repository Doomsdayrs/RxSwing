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
package rx.swing.sources;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import rx.schedulers.SwingScheduler;

import java.awt.*;
import java.awt.event.*;

public final class MouseEventSource {
    private MouseEventSource() {
    }

    /**
     * @see rx.observables.SwingObservable#fromMouseEvents
     */
    public static Observable<MouseEvent> fromMouseEventsOf(final Component component) {
        return Observable.create((ObservableOnSubscribe<MouseEvent>) emitter -> {
            final MouseListener listener = new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent event) {
                    emitter.onNext(event);
                }

                @Override
                public void mousePressed(MouseEvent event) {
                    emitter.onNext(event);
                }

                @Override
                public void mouseReleased(MouseEvent event) {
                    emitter.onNext(event);
                }

                @Override
                public void mouseEntered(MouseEvent event) {
                    emitter.onNext(event);
                }

                @Override
                public void mouseExited(MouseEvent event) {
                    emitter.onNext(event);
                }
            };
            component.addMouseListener(listener);
            emitter.setDisposable(Disposable.fromAction(() -> component.removeMouseListener(listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see rx.observables.SwingObservable#fromMouseMotionEvents
     */
    public static Observable<MouseEvent> fromMouseMotionEventsOf(final Component component) {
        return Observable.create((ObservableOnSubscribe<MouseEvent>) emitter -> {
            final MouseMotionListener listener = new MouseMotionListener() {
                @Override
                public void mouseDragged(MouseEvent event) {
                    emitter.onNext(event);
                }

                @Override
                public void mouseMoved(MouseEvent event) {
                    emitter.onNext(event);
                }
            };
            component.addMouseMotionListener(listener);
            emitter.setDisposable(Disposable.fromAction(() -> component.removeMouseMotionListener(listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    public static Observable<MouseWheelEvent> fromMouseWheelEvents(final Component component) {
        return Observable.create((ObservableOnSubscribe<MouseWheelEvent>) emitter -> {
            final MouseWheelListener listener = emitter::onNext;
            component.addMouseWheelListener(listener);
            emitter.setDisposable(Disposable.fromAction(() -> component.removeMouseWheelListener(listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see rx.observables.SwingObservable#fromRelativeMouseMotion
     */
    public static Observable<Point> fromRelativeMouseMotion(final Component component) {
        final Observable<MouseEvent> events = fromMouseMotionEventsOf(component);
        return Observable.zip(events, events.skip(1), (ev1, ev2) -> new Point(ev2.getX() - ev1.getX(), ev2.getY() - ev1.getY())).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

}
