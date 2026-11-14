package astronomicalsystem.core.impact;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Directions around an axis, within an angle of it: where the debris of an impact flies.
 *
 * @param axis      precondition: not zero
 * @param halfAngle between the axis and the side of the cone, in radians; precondition: in (0, pi]
 */
public record Cone(@NotNull Vector3 axis, double halfAngle) {

    // pi (3 - sqrt 5): consecutive points turn by it around the axis, so that none lines up
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));
    private static final Vector3 X_AXIS = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);

    /**
     * A Fibonacci spiral on the cap of the unit sphere inside the cone: the cap between the axis
     * and the angle t has area 2 pi (1 - cos t), so steps of equal size in cos t cut it into rings
     * of equal area, and the i-th direction sits in the middle of the i-th, turned by the golden
     * angle from the one before.
     *
     * @param count precondition: positive
     * @return that many unit vectors spread evenly inside the cone
     */
    public @NotNull List<Vector3> directions(int count) {
        Vector3 along = axis.normalized();
        // Any vector not parallel to the axis gives a basis across it
        Vector3 across = along.cross(Math.abs(along.x()) < 0.9 ? X_AXIS : Y_AXIS).normalized();
        Vector3 third = along.cross(across);
        return IntStream.range(0, count).mapToObj(i -> {
            double cos = 1.0 - (1.0 - Math.cos(halfAngle)) * (i + 0.5) / count;
            double sin = Math.sqrt(1.0 - cos * cos);
            double turn = i * GOLDEN_ANGLE;
            return along.times(cos).plus(across.times(sin * Math.cos(turn))).plus(third.times(sin * Math.sin(turn)));
        }).toList();
    }
}
