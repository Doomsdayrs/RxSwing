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
import java.awt.*;
import java.beans.PropertyChangeEvent;

import static org.mockito.Mockito.*;

public class PropertyChangeEventSourceTest {
    private static ArgumentMatcher<PropertyChangeEvent> propertyChangeEventMatcher(final String propertyName, final Object oldValue, final Object newValue) {
        return argument -> {
            if (!propertyName.equals(argument.getPropertyName())) {
                return false;
            }

            if (!oldValue.equals(argument.getOldValue())) {
                return false;
            }

            return newValue.equals(argument.getNewValue());
        };
    }

    @Test
    public void testObservingPropertyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<PropertyChangeEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            Component component = new JPanel();

            Disposable subscription = PropertyChangeEventSource.fromPropertyChangeEventsOf(component)
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            component.setEnabled(false);
            verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
            verifyNoMoreInteractions(action, error, complete);

            // check that an event is only fired if the value really changes
            component.setEnabled(false);
            verifyNoMoreInteractions(action, error, complete);

            component.setEnabled(true);
            verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
            verifyNoMoreInteractions(action, error, complete);

            // check some arbitrary property
            component.firePropertyChange("width", 200, 300);
            verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("width", 200L, 300L)));
            verifyNoMoreInteractions(action, error, complete);

            // verify no events sent after unsubscribing
            subscription.dispose();
            component.setEnabled(false);
            verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    @Test
    public void testObservingFilteredPropertyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<PropertyChangeEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            Component component = new JPanel();

            Disposable subscription = SwingObservable.fromPropertyChangeEvents(component, "enabled")
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            // trigger a bunch of property change events and verify that only the enabled ones are observed
            component.setEnabled(false);
            component.setEnabled(false);
            component.setEnabled(true);
            component.firePropertyChange("width", 200, 300);
            component.firePropertyChange("height", 400, 200);
            component.firePropertyChange("depth", 100, 300);
            verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", true, false)));
            verify(action, times(1)).accept(Mockito.argThat(propertyChangeEventMatcher("enabled", false, true)));
            verifyNoMoreInteractions(action, error, complete);

            subscription.dispose();
        }).awaitTerminal();
    }
}
