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
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;
import org.mockito.Mockito;
import rx.observables.SwingObservable;
import rx.swing.sources.HierarchyEventSource.Predicate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

public class HierarchyBoundsEventSourceTest {

    private final Function<Component, Observable<HierarchyEvent>> observableFactory;
    private JPanel rootPanel;
    private JPanel parentPanel;
    private Consumer<HierarchyEvent> action;
    private Consumer<Throwable> error;
    private Action complete;
    private JPanel childPanel;

    public HierarchyBoundsEventSourceTest(Function<Component, Observable<HierarchyEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{observableFromEventSource()},
                {observableFromSwingObservable()}});
    }

    private static Function<Component, Observable<HierarchyEvent>> observableFromEventSource() {
        return HierarchyEventSource::fromHierarchyBoundsEventsOf;
    }

    private static Function<Component, Observable<HierarchyEvent>> observableFromSwingObservable() {
        return SwingObservable::fromHierarchyBoundsEvents;
    }

    @SuppressWarnings("unchecked")
    @BeforeAll
    public void setup() {
        rootPanel = new JPanel();

        parentPanel = new JPanel();
        rootPanel.add(parentPanel);

        childPanel = Mockito.spy(new JPanel());
        parentPanel.add(childPanel);

        action = mock(Consumer.class);
        error = mock(Consumer.class);
        complete = mock(Action.class);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingAncestorResizedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            Disposable subscription = observableFactory.apply(childPanel)
                    .filter(Predicate.ANCESTOR_RESIZED)
                    .subscribe(action, error, complete);

            parentPanel.setSize(10, 10);
            parentPanel.setLocation(10, 10); // verifies that ancestor moved events are ignored.

            Mockito.verify(action).accept(ArgumentMatchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
            assertEquals(0, childPanel.getHierarchyListeners().length);

            // Sanity check to verify that no more events are emitted after unsubscribing.
            parentPanel.setSize(20, 20);
            parentPanel.setLocation(20, 20);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingAncestorMovedHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            Disposable subscription = observableFactory.apply(childPanel)
                    .filter(Predicate.ANCESTOR_MOVED)
                    .subscribe(action, error, complete);

            parentPanel.setSize(10, 10); // verifies that ancestor resized events are ignored.
            parentPanel.setLocation(10, 10);

            Mockito.verify(action).accept(ArgumentMatchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
            assertEquals(0, childPanel.getHierarchyListeners().length);

            // Sanity check to verify that no more events are emitted after unsubscribing.
            parentPanel.setSize(20, 20);
            parentPanel.setLocation(20, 20);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingAllHierarchyBoundsEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            Disposable subscription = observableFactory.apply(childPanel)
                    .subscribe(action, error, complete);

            InOrder inOrder = inOrder(action);

            parentPanel.setSize(10, 10);
            parentPanel.setLocation(10, 10);

            inOrder.verify(action).accept(ArgumentMatchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_RESIZED, parentPanel, rootPanel)));
            inOrder.verify(action).accept(ArgumentMatchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.ANCESTOR_MOVED, parentPanel, rootPanel)));
            inOrder.verifyNoMoreInteractions();
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(childPanel).removeHierarchyBoundsListener(Mockito.any(HierarchyBoundsListener.class));
            assertEquals(0, childPanel.getHierarchyListeners().length);

            // Sanity check to verify that no more events are emitted after unsubscribing.
            parentPanel.setSize(20, 20);
            parentPanel.setLocation(20, 20);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    private ArgumentMatcher<HierarchyEvent> hierarchyEventMatcher(final Component source, final int id, final Container changed, final Container changedParent) {
        return argument -> {
            if (argument.getClass() != HierarchyEvent.class)
                return false;

            if (source != argument.getComponent())
                return false;

            if (changed != argument.getChanged())
                return false;

            if (changedParent != argument.getChangedParent())
                return false;

            return id == argument.getID();
        };
    }
}
