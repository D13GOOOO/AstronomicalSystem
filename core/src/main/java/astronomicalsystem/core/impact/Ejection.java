package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        Body largest = new Body(merged.name(), merged.kind(), remnant, radiusOf(remnant), merged.position(),
                merged.velocity());
        return Stream.concat(Stream.of(largest), IntStream.range(0, fragments.size()).mapToObj(this::fragment)).toList();
    }

    private @NotNull Body fragment(int index) {
        BodyName name = new BodyName(source.value() + " fragment " + (index + 1));
        return new Body(name, BodyKind.DEBRIS, fragments.get(index), radiusOf(fragments.get(index)),
                merged.position(), merged.velocity());
    }

    // The density of the bodies: the volume, and so the cube of the radius, scales as the mass
    private @NotNull Radius radiusOf(@NotNull Mass mass) {
        return new Radius(merged.radius().astronomicalUnits()
                * Math.cbrt(mass.solarMasses() / merged.mass().solarMasses()));
    }
}
