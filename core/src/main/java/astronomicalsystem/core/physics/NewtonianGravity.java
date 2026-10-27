package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Quantities;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * Newtonian gravity with Plummer softening, computed over every pair of bodies.
 *
 * @param gravitationalConstant G, in AU cubed per solar mass per day squared
 * @param softeningLength       Plummer softening length, in AU
 */
public record NewtonianGravity(double gravitationalConstant, double softeningLength) implements GravityModel {

    public NewtonianGravity {
        Quantities.requirePositiveFinite(gravitationalConstant, "Gravitational constant");
        Quantities.requireNonNegativeFinite(softeningLength, "Softening length");
    }

    @Override
    public @NotNull List<Vector3> accelerations(@NotNull List<Body> bodies) {
        Vector3[] accelerations = new Vector3[bodies.size()];
        Arrays.fill(accelerations, Vector3.ZERO);
        for (int i = 0; i < bodies.size(); i++) {
            for (int j = i + 1; j < bodies.size(); j++) {
                addMutualAttraction(bodies, accelerations, i, j);
            }
        }
        return List.of(accelerations);
    }

    private void addMutualAttraction(@NotNull List<Body> bodies, @NotNull Vector3[] accelerations, int i, int j) {
        Body first = bodies.get(i);
        Body second = bodies.get(j);
        Vector3 separation = second.position().minus(first.position());
        double softenedDistance = Math.sqrt(separation.lengthSquared() + softeningLength * softeningLength);
        Vector3 pullPerUnitMass = separation.times(
                gravitationalConstant / (softenedDistance * softenedDistance * softenedDistance));
        accelerations[i] = accelerations[i].plus(pullPerUnitMass.times(second.mass().solarMasses()));
        accelerations[j] = accelerations[j].minus(pullPerUnitMass.times(first.mass().solarMasses()));
    }
}
