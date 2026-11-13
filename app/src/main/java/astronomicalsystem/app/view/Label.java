package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Text written rightwards from its anchor, with its capitals centred vertically on the anchor.
 */
public record Label(@NotNull ImagePoint anchor, @NotNull String text) {
}
