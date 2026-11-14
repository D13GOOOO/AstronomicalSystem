package astronomicalsystem.app.impact;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * What can be seen of an impact while it unfolds: a fireball on the site, and embers thrown up
 * that fall back onto the body.
 *
 * @param collision what happened
 * @param start     when it happened
 */
public record Blast(@NotNull Collision collision, @NotNull JulianDate start) {

    private static final double G = GravitationalParameter.GRAVITATIONAL_CONSTANT;
    // A fireball the size of the body covers the face it was hit on
    private static final double LARGEST_FIREBALL_IN_RADII = 1.0;
    // It glows for twenty times as long as it takes to grow, and cools down to 1/e three times in
    // the time left: about 5% of its heat is left at the end
    private static final double FIREBALL_LIFE_IN_RISE_TIMES = 20.0;
    private static final double COOLING_TIMES = 3.0;

    /**
     * @param survivor where the body that was hit is now
     * @return what glows at the date, in space: the fireball first
     */
    public @NotNull List<Flare> flaresAt(@NotNull JulianDate now, @NotNull Vector3 survivor) {
        double age = now.days() - start.days();
        if (age > fireballLife()) {
            return List.of();
        }
        double radius = collision.survivor().radius().astronomicalUnits();
        Vector3 site = survivor.plus(collision.site().times(radius));
        return List.of(new Flare(site, fireballRadius() * Math.min(1.0, age / riseTime()), heatAt(age)));
    }

    private double fireballLife() {
        return FIREBALL_LIFE_IN_RISE_TIMES * riseTime();
    }

    // As hot as it gets while it grows, then exp(-(age - t) / tau)
    private double heatAt(double age) {
        double riseTime = riseTime();
        if (age <= riseTime) {
            return 1.0;
        }
        double coolingTime = (fireballLife() - riseTime) / COOLING_TIMES;
        return Math.exp(-(age - riseTime) / coolingTime);
    }

    // The energy E of the impact over the binding energy U = (3/5) G M² / R that would scatter the
    // body: the fireball grows as the cube root of the energy, like the volume of the hot gas
    private double fireballRadius() {
        double mass = collision.survivor().mass().solarMasses();
        double radius = collision.survivor().radius().astronomicalUnits();
        double bindingEnergy = 0.6 * G * mass * mass / radius;
        return radius * Math.min(LARGEST_FIREBALL_IN_RADII, Math.cbrt(collision.energy() / bindingEnergy));
    }

    // The hot gas expands at about the speed of the impact
    private double riseTime() {
        return fireballRadius() / collision.speed();
    }
}
