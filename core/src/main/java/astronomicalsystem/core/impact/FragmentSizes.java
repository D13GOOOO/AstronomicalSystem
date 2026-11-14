package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * How the debris of an impact breaks into fragments.
 *
 * @param smallest the least mass a fragment can have: lighter pieces are dust, not followed
 * @param most     the most fragments an impact can leave
 */
public record FragmentSizes(@NotNull Mass smallest, int most) {

    /**
     * @param debris mass thrown out of the bodies by the impact, in solar masses; precondition:
     *               non-negative and finite
     * @return the masses of the fragments, from the largest
     */
    public @NotNull List<Mass> split(double debris) {
        if (debris < smallest.solarMasses()) {
            return List.of();
        }
        return List.of(new Mass(debris));
    }
}
