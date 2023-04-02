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

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import static org.mockito.Mockito.*;

public class WindowEventSourceTest {

    @Test
    public void testObservingWindowEvents() throws Throwable {
        if (GraphicsEnvironment.isHeadless())
            return;
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            JFrame owner = new JFrame();
            Window window = new Window(owner);

            @SuppressWarnings("unchecked")
            Consumer<WindowEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            final WindowEvent event = mock(WindowEvent.class);

            Disposable sub = WindowEventSource.fromWindowEventsOf(window)
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            fireWindowEvent(window, event);
            verify(action, times(1)).accept(ArgumentMatchers.any());

            fireWindowEvent(window, event);
            verify(action, times(2)).accept(ArgumentMatchers.any());

            sub.dispose();
            fireWindowEvent(window, event);
            verify(action, times(2)).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }

    private void fireWindowEvent(Window window, WindowEvent event) {
        for (WindowListener listener : window.getWindowListeners()) {
            listener.windowClosed(event);
        }
    }
}

