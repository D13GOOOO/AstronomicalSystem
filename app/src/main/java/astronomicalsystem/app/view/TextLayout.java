package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Where the lines of a text go in an image of a given size.
 */
@FunctionalInterface
interface TextLayout {

    @NotNull List<TextLine> linesIn(float imageWidth, float imageHeight);
}
