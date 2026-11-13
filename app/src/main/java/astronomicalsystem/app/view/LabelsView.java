package astronomicalsystem.app.view;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Short texts written over the picture, each at its own place.
 */
public interface LabelsView {

    void show(@NotNull List<Label> labels);
}
