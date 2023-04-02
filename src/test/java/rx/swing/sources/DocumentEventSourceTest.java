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

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatcher;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import rx.observables.SwingObservable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleContext;
import javax.swing.text.html.HTMLDocument;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

public class DocumentEventSourceTest {

    private static ArgumentMatcher<DocumentEvent> documentEventMatcher(final DocumentEvent.EventType eventType) {
        return argument -> argument.getType().equals(eventType);
    }

    private static void insertStringToDocument(Document doc, @SuppressWarnings("SameParameterValue") int offset, String text) {
        try {
            doc.insertString(offset, text, null);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void removeFromDocument(Document doc, @SuppressWarnings("SameParameterValue") int offset, @SuppressWarnings("SameParameterValue") int length) {
        try {
            doc.remove(offset, length);
        } catch (BadLocationException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Test
    public void testObservingDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<DocumentEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            final JEditorPane pane = new JEditorPane();
            // Document must be StyledDocument to test changeUpdate
            pane.setContentType("text/html");
            final HTMLDocument doc = (HTMLDocument) pane.getDocument();

            final Disposable subscription = DocumentEventSource.fromDocumentEventsOf(doc)
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            // test insertUpdate
            insertStringToDocument(doc, 0, "test text");
            verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
            verifyNoMoreInteractions(action, error, complete);

            // test removeUpdate
            removeFromDocument(doc, 0, 5);
            verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.REMOVE)));
            verifyNoMoreInteractions(action, error, complete);

            // test changeUpdate
            Style defaultStyle = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
            doc.setCharacterAttributes(0, doc.getLength(), defaultStyle, true);
            verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.CHANGE)));
            verifyNoMoreInteractions(action, error, complete);

            // test unsubscribe
            subscription.dispose();
            insertStringToDocument(doc, 0, "this should be ignored");
            verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

    @Test
    public void testObservingFilteredDocumentEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            @SuppressWarnings("unchecked")
            Consumer<DocumentEvent> action = mock(Consumer.class);
            @SuppressWarnings("unchecked")
            Consumer<Throwable> error = mock(Consumer.class);
            Action complete = mock(Action.class);

            final Document doc = new JEditorPane().getDocument();

            // filter only INSERT, others will be ignored
            final Set<DocumentEvent.EventType> filteredTypes
                    = new HashSet<>(Collections.singletonList(DocumentEvent.EventType.INSERT));
            final Disposable subscription = SwingObservable.fromDocumentEvents(doc, filteredTypes)
                    .subscribe(action, error, complete);

            verify(action, never()).accept(ArgumentMatchers.any());
            verify(error, never()).accept(ArgumentMatchers.any());
            verify(complete, never()).run();

            // test insertUpdate
            insertStringToDocument(doc, 0, "test text");
            verify(action).accept(Mockito.argThat(documentEventMatcher(DocumentEvent.EventType.INSERT)));
            verifyNoMoreInteractions(action, error, complete);

            // test removeUpdate
            removeFromDocument(doc, 0, 5);
            // removeUpdate should be ignored
            verifyNoMoreInteractions(action, error, complete);

            // test unsubscribe
            subscription.dispose();
            insertStringToDocument(doc, 0, "this should be ignored");
            verifyNoMoreInteractions(action, error, complete);
        }).awaitTerminal();
    }

}
