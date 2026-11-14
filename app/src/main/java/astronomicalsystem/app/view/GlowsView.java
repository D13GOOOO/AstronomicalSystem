package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Shows patches of light already placed in clip coordinates.
 */
public interface GlowsView {

    void show(@NotNull List<Glow> glows);
}
