/**
 * Copyright 2015 Netflix, Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
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
import javax.swing.colorchooser.ColorSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


public class ChangeEventSourceTest {

    private static JTabbedPane createTabbedPane() {
        final JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("tab1", new JPanel());
        tabbedPane.addTab("tab2", new JPanel());
        tabbedPane.addTab("tab3", new JPanel());
        return tabbedPane;
    }

    private static JSpinner createSpinner() {
        List<String> yearStrings = Arrays.asList("2014", "2015", "2016");
        SpinnerListModel spinnerListModel = new SpinnerListModel(yearStrings);
        return new JSpinner(spinnerListModel);
    }

    @Test
    public void jTabbedPane_observingSelectionEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JTabbedPane tabbedPane = createTabbedPane();
            ChangeEventSource.fromChangeEventsOf(tabbedPane)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            tabbedPane.setSelectedIndex(2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == tabbedPane));

            tabbedPane.setSelectedIndex(0);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == tabbedPane));
        }).awaitTerminal();
    }

    @Test
    public void jSlider_observingValueChangeEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JSlider slider = new JSlider();
            slider.setMinimum(0);
            slider.setMaximum(10);
            ChangeEventSource.fromChangeEventsOf(slider)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            slider.setValue(5);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == slider));

            slider.setValue(8);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == slider));
        }).awaitTerminal();
    }

    @Test
    public void jSpinner_observingValueChangeEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JSpinner spinner = createSpinner();
            ChangeEventSource.fromChangeEventsOf(spinner)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            spinner.setValue("2015");

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == spinner));

            spinner.setValue("2016");

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == spinner));
        }).awaitTerminal();
    }

    @Test
    public void spinnerModel_observingValueChangeEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JSpinner spinner = createSpinner();
            final SpinnerModel spinnerModel = spinner.getModel();
            ChangeEventSource.fromChangeEventsOf(spinnerModel)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            spinner.setValue("2015");

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == spinnerModel));

            spinner.setValue("2016");

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == spinnerModel));
        }).awaitTerminal();
    }

    @Test
    public void abstractButton_observingPressedEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            AbstractButton button = new JButton("Click me");
            ChangeEventSource.fromChangeEventsOf(button)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            button.getModel().setPressed(true);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == button));

            button.getModel().setPressed(false);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == button));
        }).awaitTerminal();
    }

    @Test
    public void buttonModel_observingPressedEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            AbstractButton button = new JButton("Click me");
            final ButtonModel buttonModel = button.getModel();
            ChangeEventSource.fromChangeEventsOf(buttonModel)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            buttonModel.setPressed(true);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == buttonModel));

            buttonModel.setPressed(false);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == buttonModel));
        }).awaitTerminal();
    }

    @Test
    public void jViewPort_observingScrollEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JTable table = new JTable(1000, 5);
            JScrollPane scrollPane = new JScrollPane(table);
            final JViewport viewPort = scrollPane.getViewport();
            ChangeEventSource.fromChangeEventsOf(viewPort)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            // scroll down
            table.scrollRectToVisible(table.getCellRect(table.getModel().getRowCount() - 1, 0, false));

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == viewPort));

            // scroll up
            table.scrollRectToVisible(table.getCellRect(0, 0, false));

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == viewPort));
        }).awaitTerminal();
    }

    @Test
    public void colorSelectionModel_observingColorChooserEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JColorChooser colorChooser = new JColorChooser();
            final ColorSelectionModel colorSelectionModel = colorChooser.getSelectionModel();
            ChangeEventSource.fromChangeEventsOf(colorSelectionModel)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            colorChooser.setColor(Color.BLUE);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == colorSelectionModel));

            colorChooser.setColor(Color.GREEN);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == colorSelectionModel));
        }).awaitTerminal();
    }

    @Test
    public void jProgressBar_observingProgressEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JProgressBar progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(10);
            ChangeEventSource.fromChangeEventsOf(progressBar)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            progressBar.setValue(1);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == progressBar));

            progressBar.setValue(2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == progressBar));
        }).awaitTerminal();
    }

    @Test
    public void boundedRangeModel_observingProgressEvents() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JProgressBar progressBar = new JProgressBar();
            progressBar.setMinimum(0);
            progressBar.setMaximum(10);
            final BoundedRangeModel boundedRangeModel = progressBar.getModel();
            ChangeEventSource.fromChangeEventsOf(boundedRangeModel)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            progressBar.setValue(1);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(1);

            testSubscriber.assertValueAt(0, (changeEvent -> changeEvent.getSource() == boundedRangeModel));

            progressBar.setValue(2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertValueCount(2);

            testSubscriber.assertValueAt(1, (changeEvent -> changeEvent.getSource() == boundedRangeModel));
        }).awaitTerminal();
    }

    @Test
    public void unsubscribeRemovesRowSelectionListener() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JTabbedPane tabbedPane = createTabbedPane();
            int numberOfListenersBefore = tabbedPane.getChangeListeners().length;

            ChangeEventSource.fromChangeEventsOf(tabbedPane)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            testSubscriber.dispose();

            assertTrue(testSubscriber.isDisposed());

            tabbedPane.setSelectedIndex(2);

            testSubscriber.assertNoErrors();
            testSubscriber.assertNoValues();

            assertEquals(numberOfListenersBefore, tabbedPane.getChangeListeners().length);
        }).awaitTerminal();
    }

    @Test
    public void fromChangeEventsOf_usingObjectWithoutExpectedChangeListenerSupport_failsFastWithException() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            try {
                ChangeEventSource.fromChangeEventsOf("doesNotSupportChangeListeners").subscribe(TestObserver.create());
                fail(IllegalArgumentException.class.getSimpleName() + " expected");
            } catch (IllegalArgumentException ex) {
                assertEquals("Class 'java.lang.String' has not the expected signature to support change listeners in rx.swing.sources.ChangeEventSource",
                        ex.getMessage());
            }

            Object changeEventSource = null;
            try {
                changeEventSource = new Object() {
                    @SuppressWarnings({"unused", "EmptyMethod"})
                    private void addChangeListener(ChangeListener changeListener) {/* no-op */ }

                    @SuppressWarnings({"unused", "EmptyMethod"})
                    private void removeChangeListener(ChangeListener changeListener) {/* no-op */ }

                    @Override
                    public String toString() {
                        return "hasWrongMethodModifiers";
                    }
                };
                ChangeEventSource.fromChangeEventsOf(changeEventSource).subscribe(TestObserver.create());
                fail(IllegalArgumentException.class.getSimpleName() + " expected");
            } catch (IllegalArgumentException ex) {
                assert changeEventSource != null;
                assertEquals("Class '" + changeEventSource.getClass().getName() + "' has not the expected signature to support change listeners in rx.swing.sources.ChangeEventSource",
                        ex.getMessage());
            }
        }).awaitTerminal();
    }

    @Test
    public void issuesWithAddingChangeListenerOnSubscriptionArePropagatedAsError() throws Throwable {
        SwingTestHelper.create().runInEventDispatchThread(() -> {
            TestObserver<ChangeEvent> testSubscriber = TestObserver.create();

            JProgressBar brokenProgressBarSubClass = new JProgressBar() {
                @Override
                public void addChangeListener(ChangeListener listener) {
                    if (listener.getClass().getName().contains(ChangeEventSource.class.getSimpleName())) {
                        throw new RuntimeException("Totally broken");
                    }
                }
            };
            ChangeEventSource.fromChangeEventsOf(brokenProgressBarSubClass)
                    .subscribe(testSubscriber);

            testSubscriber.assertNoValues();

            testSubscriber.assertError(RuntimeException.class);
            testSubscriber.assertError((error) ->
                    Objects.equals(error.getMessage(), "Call of addChangeListener via reflection failed.")
            );
            testSubscriber.assertError((error) ->
                    error.getCause().getClass() == InvocationTargetException.class
            );
        }).awaitTerminal();
    }
}