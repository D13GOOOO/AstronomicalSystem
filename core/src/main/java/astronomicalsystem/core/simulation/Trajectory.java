package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Trajectory(@NotNull BodyName body, @NotNull List<Vector3> points) {
}
