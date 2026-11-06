package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.Colour;
import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * How bodies of one kind are drawn.
 *
 * @param minimumDiameter in pixels: bodies are drawn at least this large, since at the scale of the
 *                        solar system they would be smaller than a pixel
 */
public record BodyStyle(@NotNull Colour colour, double minimumDiameter) {

    public BodyStyle {
        Quantities.requirePositiveFinite(minimumDiameter, "Minimum diameter");
    }
}
