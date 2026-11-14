package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
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

    // The fragments start where the debris is a few minutes after the impact, ten radii of the
    // remnant out, where steps of an hour follow it well; farther if they need it to keep a little
    // apart from the remnant and from each other
    private static final double START_IN_RADII = 10.0;
    private static final double CLEARANCE = 1.1;
    // The largest fragment leaves the surface a little faster than the escape speed, the least speed
    // at which it does not fall back; smaller ones faster, as m^(-1/6), as measured for ejecta
    private static final double ESCAPE_MARGIN = 1.05;
    private static final double SPEED_EXPONENT = -1.0 / 6.0;
    private static final double G = GravitationalParameter.GRAVITATIONAL_CONSTANT;

    public Ejection {
        fragments = List.copyOf(fragments);
    }

    /**
     * @return the largest remnant, then the fragments from the largest
     */
    public @NotNull List<Body> bodies() {
        List<Vector3> directions = cone.directions(fragments.size());
        double shell = shellRadius(directions);
        List<Vector3> offsets = directions.stream().map(direction -> direction.times(shell)).toList();
        List<Vector3> kicks = IntStream.range(0, fragments.size())
                .mapToObj(index -> directions.get(index).times(speedAt(shell, fragments.get(index))))
                .toList();
        // M X = M_lr x + sum m_k (x + a_k): the remnant moves back by sum m_k a_k / M, and likewise
        // its velocity, so that the centre of mass and the momentum stay
        Vector3 position = merged.position().minus(weightedSum(offsets));
        Vector3 velocity = merged.velocity().minus(weightedSum(kicks));
        Body largest = new Body(merged.name(), merged.kind(), remnant, radiusOf(remnant), position, velocity);
        return Stream.concat(Stream.of(largest), IntStream.range(0, fragments.size())
                .mapToObj(index -> fragment(index, position.plus(offsets.get(index)), velocity.plus(kicks.get(index)))))
                .toList();
    }

    // Leaving the surface at u, a fragment keeps u² - v_e² (1 - R / r) at a distance r, where
    // v_e² = 2 G M_lr / R: the energy u² / 2 - G M_lr / R is the same at both places
    private double speedAt(double distance, @NotNull Mass fragment) {
        double remnantRadius = radiusOf(remnant).astronomicalUnits();
        double escapeSquared = 2.0 * G * remnant.solarMasses() / remnantRadius;
        double surface = ESCAPE_MARGIN
                * Math.pow(fragment.solarMasses() / fragments.getFirst().solarMasses(), SPEED_EXPONENT);
        return Math.sqrt(surface * surface * escapeSquared - escapeSquared * (1.0 - remnantRadius / distance));
    }

    // sum m_k v_k / M, with the masses of the fragments and the total mass
    private @NotNull Vector3 weightedSum(@NotNull List<Vector3> vectors) {
        return IntStream.range(0, fragments.size())
                .mapToObj(index -> vectors.get(index).times(fragments.get(index).solarMasses()))
                .reduce(Vector3.ZERO, Vector3::plus)
                .times(1.0 / merged.mass().solarMasses());
    }

    private @NotNull Body fragment(int index, @NotNull Vector3 position, @NotNull Vector3 velocity) {
        BodyName name = new BodyName(source.value() + " fragment " + (index + 1));
        return new Body(name, BodyKind.DEBRIS, fragments.get(index), radiusOf(fragments.get(index)), position,
                velocity);
    }

    // Two fragments in directions a chord c apart on the unit sphere are c times the shell apart, which
    // must exceed twice the radius of the largest fragment
    private double shellRadius(@NotNull List<Vector3> directions) {
        double remnantRadius = radiusOf(remnant).astronomicalUnits();
        double largestFragment = radiusOf(fragments.getFirst()).astronomicalUnits();
        double clearOfTheRemnant = remnantRadius + largestFragment;
        double clearOfEachOther = 2.0 * largestFragment / smallestChord(directions);
        return Math.max(START_IN_RADII * remnantRadius, CLEARANCE * Math.max(clearOfTheRemnant, clearOfEachOther));
    }

    // Infinite for a single direction, which has no neighbour
    private static double smallestChord(@NotNull List<Vector3> directions) {
        return IntStream.range(0, directions.size()).boxed()
                .flatMap(i -> IntStream.range(i + 1, directions.size())
                        .mapToObj(j -> directions.get(i).minus(directions.get(j)).length()))
                .min(Double::compare).orElse(Double.POSITIVE_INFINITY);
    }

    // The density of the bodies: the volume, and so the cube of the radius, scales as the mass
    private @NotNull Radius radiusOf(@NotNull Mass mass) {
        return new Radius(merged.radius().astronomicalUnits()
                * Math.cbrt(mass.solarMasses() / merged.mass().solarMasses()));
    }
}
