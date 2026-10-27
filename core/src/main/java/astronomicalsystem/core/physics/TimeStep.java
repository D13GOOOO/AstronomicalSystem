package astronomicalsystem.core.physics;

import astronomicalsystem.core.math.Quantities;

/**
 * Duration of one integration step, in days.
 */
public record TimeStep(double days) {

    public TimeStep {
        Quantities.requirePositiveFinite(days, "Time step");
    }
}
