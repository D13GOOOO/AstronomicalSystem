package astronomicalsystem.app.time;

import astronomicalsystem.core.math.Quantities;

/**
 * How fast simulated time flows compared with real time.
 *
 * @param daysPerSecond simulated days that pass in one real second
 */
public record TimeWarp(double daysPerSecond) {

    public TimeWarp {
        Quantities.requirePositiveFinite(daysPerSecond, "Time warp");
    }
}
