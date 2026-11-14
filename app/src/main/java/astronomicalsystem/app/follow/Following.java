package astronomicalsystem.app.follow;

import org.jetbrains.annotations.NotNull;

/**
 * Tells what the camera is following.
 */
public interface Following {

    @NotNull Focus focus();
}
