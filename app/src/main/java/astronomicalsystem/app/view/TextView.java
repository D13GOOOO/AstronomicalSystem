package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lines of text written over the picture, near one of its corners.
 */
public interface TextView {

    void show(@NotNull List<String> lines);
}
