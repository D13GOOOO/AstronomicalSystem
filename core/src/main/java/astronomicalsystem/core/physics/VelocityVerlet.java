package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Velocity Verlet integrator (kick-drift-kick).
 */
public final class VelocityVerlet implements Integrator {

    private final GravityModel gravity;

    public VelocityVerlet(@NotNull GravityModel gravity) {
        this.gravity = gravity;
    }

    @Override
    public @NotNull List<Body> step(@NotNull List<Body> bodies, @NotNull TimeStep timeStep) {
        double days = timeStep.days();
        List<Body> halfKicked = halfKick(bodies, gravity.accelerations(bodies), days);
        List<Body> drifted = halfKicked.stream().map(body -> drift(body, days)).toList();
        return halfKick(drifted, gravity.accelerations(drifted), days);
    }

    private static @NotNull List<Body> halfKick(@NotNull List<Body> bodies, @NotNull List<Vector3> accelerations,
                                                double days) {
        return IntStream.range(0, bodies.size())
                .mapToObj(i -> kick(bodies.get(i), accelerations.get(i), days / 2.0))
                .toList();
    }

    private static @NotNull Body kick(@NotNull Body body, @NotNull Vector3 acceleration, double days) {
        return body.withMotion(body.position(), body.velocity().plus(acceleration.times(days)));
    }

    private static @NotNull Body drift(@NotNull Body body, double days) {
        return body.withMotion(body.position().plus(body.velocity().times(days)), body.velocity());
    }
}
