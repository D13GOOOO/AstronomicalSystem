package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.IntStream;

/**
 * How the debris of an impact breaks into fragments.
 *
 * @param smallest the least mass a fragment can have: lighter pieces are dust, not followed
 * @param most     the most fragments an impact can leave
 */
public record FragmentSizes(@NotNull Mass smallest, int most) {

    // The k-th largest fragment has a mass proportional to k^(-1/b) when N(>m) ~ m^-b; b = 0.95
    // from the size distribution N(>D) ~ D^-2.85 of Leinhardt and Stewart, with m ~ D³
    private static final double RANK_EXPONENT = -1.0 / 0.95;

    public FragmentSizes {
        if (most < 1) {
            throw new IllegalArgumentException("An impact must be allowed at least one fragment");
        }
    }

    /**
     * @param debris mass thrown out of the bodies by the impact, in solar masses; precondition:
     *               non-negative and finite
     * @return the masses of the fragments, from the largest: as many as can share the debris with
     *         none lighter than the smallest, up to the most; none if the debris is lighter
     */
    public @NotNull List<Mass> split(double debris) {
        if (debris < smallest.solarMasses()) {
            return List.of();
        }
        // One fragment is always heavy enough, since it takes the whole debris
        int count = most;
        while (count > 1 && share(debris, count, count) < smallest.solarMasses()) {
            count--;
        }
        int fragments = count;
        return IntStream.rangeClosed(1, fragments).mapToObj(rank -> new Mass(share(debris, rank, fragments))).toList();
    }

    // The mass of the fragment of the given rank, when the debris breaks into count fragments
    private static double share(double debris, int rank, int count) {
        double total = IntStream.rangeClosed(1, count).mapToDouble(FragmentSizes::weight).sum();
        return debris * weight(rank) / total;
    }

    private static double weight(int rank) {
        return Math.pow(rank, RANK_EXPONENT);
    }
}
