package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lines of text written over the picture, from its top left corner.
 */
public interface TextView {

    void show(@NotNull List<String> lines);
}
