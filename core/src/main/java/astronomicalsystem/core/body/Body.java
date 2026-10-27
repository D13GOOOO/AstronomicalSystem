package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Immutable snapshot of a body: its identity, physical properties and motion.
 */
public record Body(@NotNull BodyName name,
                   @NotNull BodyKind kind,
                   @NotNull Mass mass,
                   @NotNull Radius radius,
                   @NotNull Vector3 position,
                   @NotNull Vector3 velocity) {

    public @NotNull Body withMotion(@NotNull Vector3 newPosition, @NotNull Vector3 newVelocity) {
        return new Body(name, kind, mass, radius, newPosition, newVelocity);
    }

    /**
     * @param days a finite duration
     * @return this body moved at its current velocity for {@code days}
     */
    public @NotNull Body drifted(double days) {
        return this;
    }
}
