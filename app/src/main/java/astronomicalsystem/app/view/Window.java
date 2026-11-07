package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Window the application draws in, frame after frame.
 */
public interface Window {

    /**
     * @return whether the user asked to close the window
     */
    boolean shouldClose();

    /**
     * Draws the content on a cleared frame, shows it and processes the events of the user.
     */
    void drawFrame(@NotNull Drawable content);
}
