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

    /**
     * @param debris mass thrown out of the bodies by the impact, in solar masses; precondition:
     *               non-negative and finite
     * @return the masses of the fragments, from the largest
     */
    public @NotNull List<Mass> split(double debris) {
        if (debris < smallest.solarMasses()) {
            return List.of();
        }
        List<Double> weights = IntStream.rangeClosed(1, most).mapToObj(rank -> Math.pow(rank, RANK_EXPONENT)).toList();
        double total = weights.stream().mapToDouble(Double::doubleValue).sum();
        return weights.stream().map(weight -> new Mass(debris * weight / total)).toList();
    }
}
