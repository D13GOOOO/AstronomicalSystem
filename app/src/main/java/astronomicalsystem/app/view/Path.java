package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Line of one colour through a sequence of points.
 */
public record Path(@NotNull List<ClipPoint> points, @NotNull Colour colour) {
}
