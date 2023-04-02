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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.observables.SwingObservable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class HierarchyEventSourceTest {

    private final Function<Component, Observable<HierarchyEvent>> observableFactory;
    private JPanel rootPanel;
    private JPanel parentPanel;
    private Consumer<HierarchyEvent> action;
    private Consumer<Throwable> error;
    private Action complete;

    public HierarchyEventSourceTest(Function<Component, Observable<HierarchyEvent>> observableFactory) {
        this.observableFactory = observableFactory;
    }

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{ObservableFromEventSource()},
                {ObservableFromSwingObservable()}});
    }

    private static Function<Component, Observable<HierarchyEvent>> ObservableFromEventSource() {
        return HierarchyEventSource::fromHierarchyEventsOf;
    }

    private static Function<Component, Observable<HierarchyEvent>> ObservableFromSwingObservable() {
        return SwingObservable::fromHierarchyEvents;
    }

    @SuppressWarnings("unchecked")
    @BeforeAll
    public void setup() {
        rootPanel = new JPanel();
        parentPanel = new JPanel();

        action = mock(Consumer.class);
        error = mock(Consumer.class);
        complete = mock(Action.class);
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testObservingHierarchyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            JPanel childPanel = Mockito.spy(new JPanel());
            parentPanel.add(childPanel);

            Disposable subscription = observableFactory.apply(childPanel)
                    .subscribe(action, error, complete);

            rootPanel.add(parentPanel);

            Mockito.verify(action).accept(ArgumentMatchers.argThat(hierarchyEventMatcher(childPanel, HierarchyEvent.PARENT_CHANGED, parentPanel, rootPanel)));
            Mockito.verify(error, Mockito.never()).accept(Mockito.any(Throwable.class));
            Mockito.verify(complete, Mockito.never()).run();

            // Verifies that the underlying listener has been removed.
            subscription.dispose();
            Mockito.verify(childPanel).removeHierarchyListener(Mockito.any(HierarchyListener.class));
            assertEquals(0, childPanel.getHierarchyListeners().length);

            // Sanity check to verify that no more events are emitted after unsubscribing.
            rootPanel.remove(parentPanel);
            Mockito.verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    private ArgumentMatcher<HierarchyEvent> hierarchyEventMatcher(final Component source, @SuppressWarnings("SameParameterValue") final int changeFlags, final Container changed, final Container changedParent) {
        return argument -> {
            if (source != argument.getComponent())
                return false;

            if (changed != argument.getChanged())
                return false;

            if (changedParent != argument.getChangedParent())
                return false;

            return changeFlags == argument.getChangeFlags();
        };
    }
}
