package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lines drawn in the picture, each through its own points.
 */
public interface PathsView {

    void show(@NotNull List<Path> paths);
}
