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
import org.mockito.ArgumentMatchers;
import org.mockito.InOrder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class KeyEventSourceTest {
    private final Component comp = new JPanel();

    @Test
    public void testObservingKeyEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<KeyEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            final KeyEvent event = mock(KeyEvent.class);

            Disposable sub = KeyEventSource.fromKeyEventsOf(comp)
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            fireKeyEvent(event);
            verify(action, times(1)).accept(ArgumentMatchers.any());

            fireKeyEvent(event);
            verify(action, times(2)).accept(ArgumentMatchers.any());

            sub.dispose();
            fireKeyEvent(event);
            verify(action, times(2)).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    @Test
    public void testObservingPressedKeys() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<Set<Integer>> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            Disposable sub = KeyEventSource.currentlyPressedKeysOf(comp)
                    .subscribe(action, error, complete);

            InOrder inOrder = inOrder(action);
            inOrder.verify(action).accept(
                    Collections.emptySet());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
            inOrder.verify(action, times(1)).accept(
                    new HashSet<>(Collections.singletonList(1)));
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            fireKeyEvent(keyEvent(2, KeyEvent.KEY_PRESSED));
            fireKeyEvent(keyEvent(KeyEvent.VK_UNDEFINED, KeyEvent.KEY_TYPED));
            inOrder.verify(action, times(1)).accept(
                    new HashSet<>(asList(1, 2)));

            fireKeyEvent(keyEvent(2, KeyEvent.KEY_RELEASED));
            inOrder.verify(action, times(1)).accept(
                    new HashSet<>(Collections.singletonList(1)));

            fireKeyEvent(keyEvent(3, KeyEvent.KEY_RELEASED));
            inOrder.verify(action, times(1)).accept(
                    new HashSet<>(Collections.singletonList(1)));

            fireKeyEvent(keyEvent(1, KeyEvent.KEY_RELEASED));
            inOrder.verify(action, times(1)).accept(
                    Collections.emptySet());

            sub.dispose();

            fireKeyEvent(keyEvent(1, KeyEvent.KEY_PRESSED));
            inOrder.verify(action, never()).accept(
                    ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    private KeyEvent keyEvent(int keyCode, int id) {
        return new KeyEvent(comp, id, -1L, 0, keyCode, ' ');
    }

    private void fireKeyEvent(KeyEvent event) {
        for (KeyListener listener : comp.getKeyListeners()) {
            listener.keyTyped(event);
        }
    }
}
