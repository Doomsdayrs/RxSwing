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

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

public final class ListSelectionEventSource {
    private ListSelectionEventSource() {
    }

    /**
     * @see rx.observables.SwingObservable#fromListSelectionEvents(ListSelectionModel)
     */
    public static Observable<ListSelectionEvent> fromListSelectionEventsOf(final ListSelectionModel listSelectionModel) {
        return Observable.create((ObservableOnSubscribe<ListSelectionEvent>) emitter -> {
            final ListSelectionListener listener = emitter::onNext;
            listSelectionModel.addListSelectionListener(listener);
            emitter.setDisposable(Disposable.fromAction(() -> listSelectionModel.removeListSelectionListener(listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }
}
