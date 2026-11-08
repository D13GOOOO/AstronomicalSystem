package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * View that reports what the user types to a handler.
 */
public interface KeyboardView {

    void addHandlers(@NotNull KeyboardHandler handler);
}
