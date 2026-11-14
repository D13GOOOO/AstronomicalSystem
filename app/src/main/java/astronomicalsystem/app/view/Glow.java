package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Round patch of light, brightest in the middle and fading to nothing at its edge, that adds to
 * whatever lies behind it.
 *
 * @param diameter in pixels
 * @param colour   of its middle; black adds nothing
 */
public record Glow(@NotNull ClipPoint centre, double diameter, @NotNull Colour colour) {
}
