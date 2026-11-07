package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Picture of the bodies, redrawn whenever they move.
 */
public interface SceneView {

    void show(@NotNull List<Disc> discs);
}
