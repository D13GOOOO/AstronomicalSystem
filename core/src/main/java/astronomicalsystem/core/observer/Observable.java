package astronomicalsystem.core.observer;

import org.jetbrains.annotations.NotNull;

/**
 * Notifies its observers of every change of its state.
 *
 * @param <T> the type of the observed state
 */
public interface Observable<T> {

    void addObserver(@NotNull Observer<T> observer);

    void notifyObservers();

    @NotNull T getState();
}
