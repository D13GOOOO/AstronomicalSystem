package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

/**
 * Resolves every collision as a perfectly inelastic one: bodies that meet during the step merge
 * into one, in the order in which they meet.
 */
public class MergingCollisions implements CollisionResolver {

    @Override
    public @NotNull List<Body> resolve(@NotNull List<Body> before, @NotNull List<Body> after) {
        List<Sweep> sweeps = new ArrayList<>(IntStream.range(0, after.size())
                .mapToObj(i -> new Sweep(before.get(i), after.get(i)))
                .toList());
        Meeting meeting = earliestMeeting(sweeps);
        while (meeting.happens()) {
            sweeps.set(meeting.first(), sweeps.get(meeting.first()).mergedWith(sweeps.remove(meeting.second())));
            meeting = earliestMeeting(sweeps);
        }
        return sweeps.stream().map(Sweep::end).toList();
    }

    // The merged body takes the place of the first of the two; on a tie, the first pair in index
    // order meets first
    private static @NotNull Meeting earliestMeeting(@NotNull List<Sweep> sweeps) {
        Meeting earliest = Meeting.NONE;
        for (int first = 0; first < sweeps.size(); first++) {
            earliest = earliestMeetingOf(sweeps, first, earliest);
        }
        return earliest;
    }

    // The earliest between the one given and the meetings of the body at index first with the
    // bodies after it
    private static @NotNull Meeting earliestMeetingOf(@NotNull List<Sweep> sweeps, int first,
                                                      @NotNull Meeting earliest) {
        Meeting result = earliest;
        for (int second = first + 1; second < sweeps.size(); second++) {
            OptionalDouble contact = sweeps.get(first).firstContactWith(sweeps.get(second));
            if (contact.isPresent() && contact.getAsDouble() < result.fraction()) {
                result = new Meeting(first, second, contact.getAsDouble());
            }
        }
        return result;
    }

    // Two bodies, by their indices, and the fraction of the step at which they meet
    private record Meeting(int first, int second, double fraction) {

        // No pair meets within the step
        static final Meeting NONE = new Meeting(-1, -1, Double.POSITIVE_INFINITY);

        boolean happens() {
            return fraction < Double.POSITIVE_INFINITY;
        }
    }

    // A body over the step, moving in a straight line from where it starts to where it ends
    private record Sweep(@NotNull Body start, @NotNull Body end) {

        /**
         * The separation d + w s, where d is the separation at the start and w its change over the
         * step, reaches the sum of the radii R where |w|² s² + 2 (d·w) s + |d|² - R² = 0. The
         * smaller root, written as c / (-b + √(b² - a c)) so that nothing cancels, is the moment
         * of contact.
         *
         * @return the fraction of the step, in [0, 1), at which the two spheres start to overlap;
         *         0 if they already do; empty if they do not within the step
         */
        @NotNull OptionalDouble firstContactWith(@NotNull Sweep other) {
            Vector3 startSeparation = other.start.position().minus(start.position());
            Vector3 change = other.end.position().minus(end.position()).minus(startSeparation);
            double reach = end.radius().astronomicalUnits() + other.end.radius().astronomicalUnits();
            double c = startSeparation.lengthSquared() - reach * reach;
            if (c < 0.0) {
                return OptionalDouble.of(0.0);
            }
            double b = startSeparation.dot(change);
            double discriminant = b * b - change.lengthSquared() * c;
            // Moving apart, or passing at the sum of the radii at the closest
            if (b >= 0.0 || discriminant <= 0.0) {
                return OptionalDouble.empty();
            }
            double fraction = c / (-b + Math.sqrt(discriminant));
            return fraction < 1.0 ? OptionalDouble.of(fraction) : OptionalDouble.empty();
        }

        @NotNull Sweep mergedWith(@NotNull Sweep other) {
            return new Sweep(start.mergedWith(other.start), end.mergedWith(other.end));
        }
    }
}
