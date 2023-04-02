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

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import rx.schedulers.SwingScheduler;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class ChangeEventSource {
    private static final String ADD_CHANGE_LISTENER_METHOD_NAME = "addChangeListener";
    private static final String REMOVE_CHANGE_LISTENER_METHOD_NAME = "removeChangeListener";
    private ChangeEventSource() {
    }

    /**
     * Creates an observable corresponding to change events (e.g. progressbar value changes).
     * <p>
     * Due to the lack of a common interface in Java (up to at least version 8), the implementation
     * is generic and uses internally reflection to add and remove it's {@link ChangeListener}'s.
     * The contract is therefor that the given parameter object MUST have the typical two public methods "addChangeListener"
     * (like {@link javax.swing.JProgressBar#addChangeListener(ChangeListener)}) and "removeChangeListener"
     * (like {@link javax.swing.JProgressBar#removeChangeListener(ChangeListener)}).
     * <p>
     * For more info to change listeners and events see <a href="https://docs.oracle.com/javase/tutorial/uiswing/events/changelistener.html">
     * How to Write a Change Listener</a>.
     *
     * @param changeEventSource The object to register the observable for.
     * @return Observable emitting the change events.
     * @throws IllegalArgumentException if the given parameter object has not the needed signature
     */
    public static Observable<ChangeEvent> fromChangeEventsOf(final Object changeEventSource) {
        checkHasChangeListenerSupport(changeEventSource);
        return Observable.create((ObservableOnSubscribe<ChangeEvent>) emitter -> {
            final ChangeListener listener = emitter::onNext;
            addChangeListener(changeEventSource, listener);
            emitter.setDisposable(Disposable.fromAction(() -> removeChangeListener(changeEventSource, listener)));
        }).subscribeOn(SwingScheduler.getInstance()).unsubscribeOn(SwingScheduler.getInstance());
    }

    private static void checkHasChangeListenerSupport(Object object) {
        checkPublicMethodExists(object, ADD_CHANGE_LISTENER_METHOD_NAME, ChangeListener.class);
        checkPublicMethodExists(object, REMOVE_CHANGE_LISTENER_METHOD_NAME, ChangeListener.class);
    }

    private static void checkPublicMethodExists(Object object, String methodName, @SuppressWarnings("SameParameterValue") Class<?>... parameterTypes) {
        try {
            Method method = object.getClass().getMethod(methodName, parameterTypes);
            if (!Modifier.isPublic(method.getModifiers())) {
                throw new IllegalArgumentException(
                        "Class '" + object.getClass().getName() + "' has not the expected signature to support change listeners in "
                                + ChangeEventSource.class.getName() + ". " + methodName + " is not accessible.");
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Class '" + object.getClass().getName() + "' has not the expected signature to support change listeners in " + ChangeEventSource.class.getName(), e);
        }
    }

    private static void addChangeListener(Object object, ChangeListener changeListener) {
        callChangeListenerMethodViaReflection(object, ADD_CHANGE_LISTENER_METHOD_NAME, changeListener);
    }

    private static void removeChangeListener(Object object, ChangeListener changeListener) {
        callChangeListenerMethodViaReflection(object, REMOVE_CHANGE_LISTENER_METHOD_NAME, changeListener);
    }

    private static void callChangeListenerMethodViaReflection(Object object,
                                                              String methodName,
                                                              ChangeListener changeListener) {
        try {
            object.getClass().getMethod(methodName, ChangeListener.class).invoke(object, changeListener);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            throw new IllegalArgumentException("Call of " + methodName + " via reflection failed. Does class " + object.getClass().getName() + " support change listeners?", e);
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException("Call of " + methodName + " via reflection failed.", e);
        }
    }
}
