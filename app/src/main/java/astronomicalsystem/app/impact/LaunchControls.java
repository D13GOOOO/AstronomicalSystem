package astronomicalsystem.app.impact;

import astronomicalsystem.core.impact.Impactor;
import org.jetbrains.annotations.NotNull;

/**
 * Operations the user can perform to throw bodies at the followed one.
 */
public interface LaunchControls {

    void launch(@NotNull Impactor impactor);
}
