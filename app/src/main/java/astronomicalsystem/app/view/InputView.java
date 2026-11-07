package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * View that reports what the user does to a handler.
 */
public interface InputView {

    void addHandlers(@NotNull InputHandler handler);
}
