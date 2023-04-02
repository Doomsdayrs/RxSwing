/**
 * Copyright 2015 Netflix, Inc.
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
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

public final class HierarchyEventSource {
    private HierarchyEventSource() {
    }

    /**
     * @see rx.observables.SwingObservable#fromHierarchyEvents
     */
    public static Observable<HierarchyEvent> fromHierarchyEventsOf(final Component component) {
        return Observable.create((ObservableOnSubscribe<HierarchyEvent>) emitter -> {
                    final HierarchyListener hierarchyListener = emitter::onNext;
                    component.addHierarchyListener(hierarchyListener);
                    emitter.setDisposable(Disposable.fromAction(() -> component.removeHierarchyListener(hierarchyListener)));
                }).subscribeOn(SwingScheduler.getInstance())
                .unsubscribeOn(SwingScheduler.getInstance());
    }

    /**
     * @see rx.observables.SwingObservable#fromHierarchyBoundsEvents
     */
    public static Observable<HierarchyEvent> fromHierarchyBoundsEventsOf(final Component component) {
        return Observable.create((ObservableOnSubscribe<HierarchyEvent>) emitter -> {
            final HierarchyBoundsListener hierarchyBoundsListener = new HierarchyBoundsListener() {
                @Override
                public void ancestorMoved(HierarchyEvent e) {
                    emitter.onNext(e);
                }

                @Override
                public void ancestorResized(HierarchyEvent e) {
                    emitter.onNext(e);
                }
            };
            component.addHierarchyBoundsListener(hierarchyBoundsListener);
            emitter.setDisposable(Disposable.fromAction(() -> component.removeHierarchyBoundsListener(hierarchyBoundsListener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    public enum Predicate implements io.reactivex.rxjava3.functions.Predicate<HierarchyEvent> {
        ANCESTOR_RESIZED(HierarchyEvent.ANCESTOR_RESIZED),
        ANCESTOR_MOVED(HierarchyEvent.ANCESTOR_MOVED);

        private final int id;

        Predicate(int id) {
            this.id = id;
        }

        @Override
        public boolean test(HierarchyEvent event) {
            return event.getID() == id;
        }
    }
}
