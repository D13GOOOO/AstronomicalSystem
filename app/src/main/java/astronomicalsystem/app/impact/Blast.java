package astronomicalsystem.app.impact;

import astronomicalsystem.core.impact.Cone;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    // Ten minutes at least, twenty seconds when time is slowed down the most: small fireballs grow
    // in a fraction of a second
    private static final double SHORTEST_FIREBALL_LIFE = 10.0 / 1440.0;
    // The plume: embers thrown up in a cone of 45 degrees around the direction of the debris, as
    // high as half to four times the radius of the fireball
    private static final int EMBERS = 240;
    private static final double PLUME_HALF_ANGLE = Math.PI / 4.0;
    private static final double LOWEST_IN_FIREBALLS = 0.5;
    private static final double HEIGHTS_IN_FIREBALLS = 3.5;
    // 1 / phi: the fractional parts of its multiples spread evenly over [0, 1), never repeating
    private static final double GOLDEN_FRACTION = (Math.sqrt(5.0) - 1.0) / 2.0;
    // Embers start just above the ground, so that they are not taken as fallen back
    private static final double LIFT = 1e-3;
    // Steps of a twentieth of sqrt(R³ / G M), the time in which gravity changes their course much
    private static final double STEPS_PER_FALL = 20.0;

    /**
     * @param survivor where the body that was hit is now
     * @return what glows at the date, in space: the fireball first while it lasts, then the embers
     *         still in flight
     */
    public @NotNull List<Flare> flaresAt(@NotNull JulianDate now, @NotNull Vector3 survivor) {
        double age = now.days() - start.days();
        double radius = collision.survivor().radius().astronomicalUnits();
        List<Flare> flares = new ArrayList<>();
        if (age <= fireballLife()) {
            Vector3 site = survivor.plus(collision.site().times(radius));
            flares.add(new Flare(site, fireballRadius() * Math.min(1.0, age / riseTime()), heatAt(age)));
        }
        double emberHeat = Math.exp(-age / fireballLife());
        for (Ember ember : embers()) {
            ember.offsetAfter(age, gravitationalParameter(), radius)
                    .ifPresent(offset -> flares.add(new Flare(survivor.plus(offset), 0.0, emberHeat)));
        }
        return flares;
    }

    private double fireballLife() {
        return Math.max(FIREBALL_LIFE_IN_RISE_TIMES * riseTime(), SHORTEST_FIREBALL_LIFE);
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

    private double gravitationalParameter() {
        return G * collision.survivor().mass().solarMasses();
    }

    // Thrown up straight, an ember rises to a height h where v² = 2 G M (1 / R - 1 / (R + h)): the
    // same energy on its way along the cone
    private @NotNull List<Ember> embers() {
        double radius = collision.survivor().radius().astronomicalUnits();
        Vector3 site = collision.site().times(radius * (1.0 + LIFT));
        List<Vector3> directions = new Cone(collision.plume(), PLUME_HALF_ANGLE).directions(EMBERS);
        List<Ember> embers = new ArrayList<>();
        for (int index = 0; index < EMBERS; index++) {
            double share = ((index + 1) * GOLDEN_FRACTION) % 1.0;
            double height = fireballRadius() * (LOWEST_IN_FIREBALLS + HEIGHTS_IN_FIREBALLS * share);
            double speed = Math.sqrt(2.0 * gravitationalParameter() * (1.0 / radius - 1.0 / (radius + height)));
            embers.add(new Ember(site, directions.get(index).times(speed)));
        }
        return embers;
    }

    // A piece of the plume at its launch, relative to the centre of the body
    private record Ember(@NotNull Vector3 position, @NotNull Vector3 velocity) {

        /**
         * Leapfrog in the gravity of the body: half a kick, a drift, half a kick.
         *
         * @return where it is after the time, relative to the centre of the body; empty once it has
         *         fallen back
         */
        @NotNull Optional<Vector3> offsetAfter(double days, double gravitationalParameter, double bodyRadius) {
            double fallTime = Math.sqrt(bodyRadius * bodyRadius * bodyRadius / gravitationalParameter);
            int steps = (int) Math.ceil(days * STEPS_PER_FALL / fallTime);
            double step = days / Math.max(steps, 1);
            Vector3 where = position;
            Vector3 speed = velocity;
            for (int count = 0; count < steps; count++) {
                speed = speed.plus(pull(where, gravitationalParameter).times(step / 2.0));
                where = where.plus(speed.times(step));
                if (where.lengthSquared() < bodyRadius * bodyRadius) {
                    return Optional.empty();
                }
                speed = speed.plus(pull(where, gravitationalParameter).times(step / 2.0));
            }
            return Optional.of(where);
        }

        // -G M r / |r|³
        private static @NotNull Vector3 pull(@NotNull Vector3 where, double gravitationalParameter) {
            double distance = where.length();
            return where.times(-gravitationalParameter / (distance * distance * distance));
        }
    }
}
