package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Picture of the rings of the bodies, redrawn whenever they change.
 */
public interface RingsView {

    void show(@NotNull List<RingSystem> rings);
}
