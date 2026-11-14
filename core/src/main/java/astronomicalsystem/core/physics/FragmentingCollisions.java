package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.impact.Cone;
import astronomicalsystem.core.impact.Ejection;
import astronomicalsystem.core.impact.FragmentSizes;
import astronomicalsystem.core.impact.Impact;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Stream;

/**
 * Resolves the collisions by the energy of the impact: bodies that meet slower than their escape
 * speed merge whole, harder impacts break off fragments.
 */
public class FragmentingCollisions extends SweptCollisions {

    // Ejecta leave in a cone of 45 degrees around the direction of the debris
    private static final double EJECTA_HALF_ANGLE = Math.PI / 4.0;

    private final FragmentSizes sizes;

    public FragmentingCollisions(@NotNull FragmentSizes sizes) {
        this.sizes = sizes;
    }

    @Override
    protected @NotNull List<Sweep> outcomeOf(@NotNull Sweep first, @NotNull Sweep second, double fraction) {
        Sweep merged = first.mergedWith(second);
        Contact contact = Contact.between(first, second, fraction);
        Impact impact = contact.impact();
        if (impact.merges()) {
            return List.of(merged);
        }
        Mass remnant = impact.largestRemnant();
        List<Mass> fragments = sizes.split(merged.end().mass().solarMasses() - remnant.solarMasses());
        if (fragments.isEmpty()) {
            return List.of(merged);
        }
        Cone cone = new Cone(contact.debrisAxis(), EJECTA_HALF_ANGLE);
        List<Body> bodies = new Ejection(merged.end(), remnant, fragments, cone, contact.projectile().end().name())
                .bodies();
        return sweepsOf(bodies, merged.start());
    }

    // The fragments keep their place around the remnant during the step, so that they do not meet
    // it or each other again before the next one
    private static @NotNull List<Sweep> sweepsOf(@NotNull List<Body> bodies, @NotNull Body mergedStart) {
        Body remnant = bodies.getFirst();
        Vector3 displacement = remnant.position().minus(mergedStart.position());
        Stream<Sweep> fragments = bodies.subList(1, bodies.size()).stream().map(fragment -> new Sweep(
                fragment.withMotion(fragment.position().minus(displacement), fragment.velocity()), fragment));
        return Stream.concat(Stream.of(new Sweep(mergedStart, remnant)), fragments).toList();
    }
}
