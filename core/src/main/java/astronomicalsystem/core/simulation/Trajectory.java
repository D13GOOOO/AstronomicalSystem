package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Path a body is going to follow.
 *
 * @param points its positions, from the present one onwards, at equal intervals of time
 */
public record Trajectory(@NotNull BodyName body, @NotNull List<Vector3> points) {

    public Trajectory {
        points = List.copyOf(points);
    }
}
