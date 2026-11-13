package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

/**
 * Line of text placed in the image.
 *
 * @param left     where the pen starts, in pixels from the left edge of the image
 * @param baseline where the glyphs stand, in pixels from the top edge of the image
 */
record TextLine(@NotNull String text, float left, float baseline) {
}
