package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.orbit.StateVector;
import org.jetbrains.annotations.NotNull;

/**
 * What a catalogue knows of a body apart from its motion.
 */
public record BodyProperties(@NotNull BodyName name,
                             @NotNull BodyKind kind,
                             @NotNull Mass mass,
                             @NotNull Radius radius) {

    /**
     * @return the body with these properties in the given state
     */
    public @NotNull Body in(@NotNull StateVector state) {
        return new Body(name, kind, mass, radius, state.position(), state.velocity());
    }
}
