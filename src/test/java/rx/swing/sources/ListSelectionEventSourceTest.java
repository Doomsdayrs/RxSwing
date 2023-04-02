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

import io.reactivex.rxjava3.observers.TestObserver;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ListSelectionEventSourceTest {

    private static int getNumberOfRowListSelectionListeners(final JTable table) {
        return ((DefaultListSelectionModel) table.getSelectionModel()).getListSelectionListeners().length;
    }

    private static JTable createJTable() {
        return new JTable(new Object[][]{
                {"A1", "B1", "C1"},
                {"A2", "B2", "C2"},
                {"A3", "B3", "C3"},
        },
                new String[]{
                        "A", "B", "C"
                });
    }

    @SuppressWarnings("SameReturnValue")
    private static boolean assertListSelectionEventEquals(ListSelectionEvent expected, ListSelectionEvent actual) {
        if (expected == null) {
            throw new IllegalArgumentException("missing expected");
        }

        if (actual == null) {
            throw new AssertionError("Expected " + expected + ", but was: null");
        }

        if (!expected.getSource().equals(actual.getSource())) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different source.");
        }
        if (expected.getFirstIndex() != actual.getFirstIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different first index.");
        }
        if (expected.getLastIndex() != actual.getLastIndex()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different last index.");
        }
        if (expected.getValueIsAdjusting() != actual.getValueIsAdjusting()) {
            throw new AssertionError("Expected " + expected + ", but was: " + actual + ". Different ValueIsAdjusting.");
        }
        return true;
    }

    @Test
    public void jTableRowSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

            JTable table = createJTable();
            ListSelectionEventSource
                    .fromListSelectionEventsOf(table.getSelectionModel())
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            table.getSelectionModel().setSelectionInterval(0, 0);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (value) ->
                    assertListSelectionEventEquals(
                            new ListSelectionEvent(
                                    table.getSelectionModel(),
                                    0 /* start of region with selection changes */,
                                    0 /* end of region with selection changes */,
                                    false),
                            value)
            );

            table.getSelectionModel().setSelectionInterval(2, 2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (value) -> assertListSelectionEventEquals(
                    new ListSelectionEvent(
                            table.getSelectionModel(),
                            0 /* start of region with selection changes */,
                            2 /* end of region with selection changes */,
                            false),
                    value));
        }).awaitTerminal();
    }

    @Test
    public void jTableColumnSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

            JTable table = createJTable();
            table.getColumnModel().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

            ListSelectionEventSource
                    .fromListSelectionEventsOf(table.getColumnModel().getSelectionModel())
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            table.getColumnModel().getSelectionModel().setSelectionInterval(0, 0);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (value) -> assertListSelectionEventEquals(
                    new ListSelectionEvent(
                            table.getColumnModel().getSelectionModel(),
                            0 /* start of region with selection changes */,
                            0 /* end of region with selection changes */,
                            false),
                    value));

            table.getColumnModel().getSelectionModel().setSelectionInterval(2, 2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (value) -> assertListSelectionEventEquals(
                    new ListSelectionEvent(
                            table.getColumnModel().getSelectionModel(),
                            0 /* start of region with selection changes */,
                            2 /* end of region with selection changes */,
                            false),
                    value));

        }).awaitTerminal();
    }

    @Test
    public void jListSelectionObservingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

            JList<String> jList = new JList<>(new String[]{"a", "b", "c", "d", "e", "f"});
            jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            ListSelectionEventSource
                    .fromListSelectionEventsOf(jList.getSelectionModel())
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            jList.getSelectionModel().setSelectionInterval(0, 0);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (value) -> assertListSelectionEventEquals(
                    new ListSelectionEvent(
                            jList.getSelectionModel(),
                            0 /* start of region with selection changes */,
                            0 /* end of region with selection changes */,
                            false),
                    value));

            jList.getSelectionModel().setSelectionInterval(2, 2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (value) -> assertListSelectionEventEquals(
                    new ListSelectionEvent(
                            jList.getSelectionModel(),
                            0 /* start of region with selection changes */,
                            2 /* end of region with selection changes */,
                            false),
                    value));
        }).awaitTerminal();
    }

    @Test
    public void jTableRowSelectionUnsubscribeRemovesRowSelectionListener() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ListSelectionEvent> testSubscriber = TestObserver.create();

            JTable table = createJTable();
            int numberOfListenersBefore = getNumberOfRowListSelectionListeners(table);

            ListSelectionEventSource
                    .fromListSelectionEventsOf(table.getSelectionModel())
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            testSubscriber.dispose();

            assertTrue(testSubscriber.isDisposed());

            table.getSelectionModel().setSelectionInterval(0, 0);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            assertEquals(numberOfListenersBefore, getNumberOfRowListSelectionListeners(table));
        }).awaitTerminal();
    }
}