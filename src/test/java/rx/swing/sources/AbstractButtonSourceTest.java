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
import java.awt.event.ActionEvent;

import static org.mockito.Mockito.*;


public class AbstractButtonSourceTest {
    @Test
    public void testObservingActionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<ActionEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            final ActionEvent event = new ActionEvent(this, 1, "command");

            class TestButton extends AbstractButton {
                void testAction() {
                    fireActionPerformed(event);
                }
            }

            TestButton button = new TestButton();
            Disposable sub = AbstractButtonSource.fromActionOf(button).subscribe(action,
                    error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            button.testAction();

            verify(action, times(1)).accept(ArgumentMatchers.any());

            button.testAction();
            verify(action, times(2)).accept(ArgumentMatchers.any());

            sub.dispose();
            button.testAction();
            verify(action, times(2)).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();
        }).awaitTerminal();
    }
}
