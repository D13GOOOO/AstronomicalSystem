package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import org.jetbrains.annotations.NotNull;

/**
 * What a catalogue knows of a body apart from its motion.
 */
public record BodyProperties(@NotNull BodyName name,
                             @NotNull BodyKind kind,
                             @NotNull Mass mass,
                             @NotNull Radius radius) {
}
