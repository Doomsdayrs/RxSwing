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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.observables.SwingObservable;

import javax.swing.*;
import java.awt.event.ItemEvent;

import static java.awt.event.ItemEvent.DESELECTED;
import static java.awt.event.ItemEvent.SELECTED;
import static org.mockito.Mockito.*;

public class ItemEventSourceTest {
    @Test
    public void testObservingItemEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked") Consumer<ItemEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked") Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            class TestButton extends AbstractButton {

                void testSelection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.SELECTED));
                }

                void testDeselection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.DESELECTED));
                }
            }

            TestButton button = new TestButton();
            Disposable sub = ItemEventSource.fromItemEventsOf(button).subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            button.testSelection();
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));

            button.testSelection();
            verify(action, times(2)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));

            button.testDeselection();
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));


            sub.dispose();
            button.testSelection();
            verify(action, times(2)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    @Test
    public void testObservingItemEventsFilteredBySelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked") Consumer<ItemEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked") Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            class TestButton extends AbstractButton {
                void testSelection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.SELECTED));
                }

                void testDeselection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.DESELECTED));
                }
            }

            TestButton button = new TestButton();
            Disposable sub = SwingObservable.fromItemSelectionEvents(button).subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            button.testSelection();
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));

            button.testDeselection();
            verify(action, never()).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));


            sub.dispose();
            button.testSelection();
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
            verify(action, never()).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    @Test
    public void testObservingItemEventsFilteredByDeSelected() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked") Consumer<ItemEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked") Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            class TestButton extends AbstractButton {
                void testSelection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.SELECTED));
                }

                void testDeselection() {
                    fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, ItemEvent.DESELECTED));
                }
            }

            TestButton button = new TestButton();
            Disposable sub = SwingObservable.fromItemDeselectionEvents(button).subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            button.testSelection();
            verify(action, never()).accept(Mockito.argThat(itemEventMatcher(SELECTED)));

            button.testDeselection();
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));


            sub.dispose();
            button.testSelection();
            verify(action, never()).accept(Mockito.argThat(itemEventMatcher(SELECTED)));
            verify(action, times(1)).accept(Mockito.argThat(itemEventMatcher(DESELECTED)));
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    private ArgumentMatcher<ItemEvent> itemEventMatcher(final int eventType) {
        return argument -> argument.getStateChange() == eventType;
    }
}
