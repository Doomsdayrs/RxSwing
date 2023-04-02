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
import rx.swing.sources.ContainerEventSource.Predicate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContainerEventSourceTest {

    private final Function<Container, Observable<ContainerEvent>> observableFactory;

    private JPanel panel;
    private Consumer<ContainerEvent> action;
    private Consumer<Throwable> error;
    private Action complete;

    public ContainerEventSourceTest(Function<Container, Observable<ContainerEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{observableFromContainerEventSource()},
                {observableFromSwingObservable()}});
    }

    private static ArgumentMatcher<ContainerEvent> containerEventMatcher(final Container container, final Component child, final int id) {
        return argument -> {
            if (argument.getClass() != ContainerEvent.class)
                return false;

            if (container != argument.getContainer())
                return false;

            if (container != argument.getSource())
                return false;

            if (child != argument.getChild())
                return false;

            return argument.getID() == id;
        };
    }

    private static Function<Container, Observable<ContainerEvent>> observableFromContainerEventSource() {
        return ContainerEventSource::fromContainerEventsOf;
    }

    private static Function<Container, Observable<ContainerEvent>> observableFromSwingObservable() {
        return SwingObservable::fromContainerEvents;
    }

    @SuppressWarnings("unchecked")
    @BeforeAll
    public void setup() {
        panel = Mockito.spy(new JPanel());
        action = Mockito.mock(Consumer.class);
        error = Mockito.mock(Consumer.class);
        complete = Mockito.mock(Action.class);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            Disposable subscription = observableFactory.apply(panel)
                    .subscribe(action, error, complete);

            JPanel child = new JPanel();
            panel.add(child);
            panel.removeAll();

            InOrder inOrder = Mockito.inOrder(action);

            inOrder.verify(action).accept(ArgumentMatchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
            inOrder.verify(action).accept(ArgumentMatchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_REMOVED)));
            inOrder.verifyNoMoreInteractions();
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
            assertEquals(0, panel.getHierarchyListeners().length);

            // Verifies that after unsubscribing events are not emitted.
            panel.add(child);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingFilteredContainerEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            Disposable subscription = observableFactory.apply(panel)
                    .filter(Predicate.COMPONENT_ADDED)
                    .subscribe(action, error, complete);

            JPanel child = new JPanel();
            panel.add(child);
            panel.remove(child); // sanity check to verify that the filtering works.

            Mockito.verify(action).accept(ArgumentMatchers.argThat(containerEventMatcher(panel, child, ContainerEvent.COMPONENT_ADDED)));
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(panel).removeContainerListener(Mockito.any(ContainerListener.class));
            assertEquals(0, panel.getHierarchyListeners().length);

            // Verifies that after unsubscribing events are not emitted.
            panel.add(child);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }
}
