package astronomicalsystem.core.observer;

import org.jetbrains.annotations.NotNull;

/**
 * Receives the state of an {@link Observable} every time it changes.
 *
 * @param <T> the type of the observed state
 */
public interface Observer<T> {

    void update(@NotNull Observable<T> subject, @NotNull T state);
}
