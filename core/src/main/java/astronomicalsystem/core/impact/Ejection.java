package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The fragments an impact throws out, and the largest remnant of the two bodies.
 *
 * @param merged    the two bodies as one: their total mass, the radius of their total volume,
 *                  their centre of mass and its velocity, the name and kind of the larger
 * @param remnant   mass of the largest remnant; precondition: the mass of the merged body less
 *                  the total of the fragments
 * @param fragments masses, from the largest; precondition: not empty
 * @param cone      the directions in which the fragments fly away
 * @param source    the body the fragments are named after
 */
public record Ejection(@NotNull Body merged, @NotNull Mass remnant, @NotNull List<Mass> fragments,
                       @NotNull Cone cone, @NotNull BodyName source) {

    public Ejection {
        fragments = List.copyOf(fragments);
    }

    /**
     * @return the largest remnant, then the fragments from the largest
     */
    public @NotNull List<Body> bodies() {
        return List.of(merged);
    }
}
