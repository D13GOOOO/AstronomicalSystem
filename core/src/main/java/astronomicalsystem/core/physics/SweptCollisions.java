package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.stream.IntStream;

/**
 * Finds the collisions of a step in the order in which the bodies meet, each body moving in a
 * straight line over the step; what a collision leaves is up to the subclasses.
 */
public abstract class SweptCollisions implements CollisionResolver {

    @Override
    public final @NotNull List<Body> resolve(@NotNull List<Body> before, @NotNull List<Body> after) {
        List<Sweep> sweeps = new ArrayList<>(IntStream.range(0, after.size())
                .mapToObj(i -> new Sweep(before.get(i), after.get(i)))
                .toList());
        Meeting meeting = earliestMeeting(sweeps);
        while (meeting.happens()) {
            Sweep second = sweeps.remove(meeting.second());
            List<Sweep> outcome = outcomeOf(sweeps.get(meeting.first()), second, meeting.fraction());
            sweeps.set(meeting.first(), outcome.getFirst());
            sweeps.addAll(outcome.subList(1, outcome.size()));
            meeting = earliestMeeting(sweeps);
        }
        return sweeps.stream().map(Sweep::end).toList();
    }

    /**
     * @param first    the body met, the earlier of the two in the order of the system
     * @param second   the body that meets it
     * @param fraction of the step at which they start to overlap, in [0, 1)
     * @return what the collision leaves, not empty: the first body takes the place of the first of
     *         the two, the others join the system after the bodies already there
     */
    protected abstract @NotNull List<Sweep> outcomeOf(@NotNull Sweep first, @NotNull Sweep second, double fraction);

    // On a tie, the first pair in index order meets first
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
}
