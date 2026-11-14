package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.orbit.GravitationalParameter;
import org.jetbrains.annotations.NotNull;

/**
 * Two bodies meeting: how hard they hit each other and what is left of them, after the scaling
 * laws of Leinhardt and Stewart (2012) for bodies held together by their own gravity.
 *
 * @param target          the larger body; precondition: not lighter than the projectile
 * @param projectile      the smaller body
 * @param contactDistance between the centres when the surfaces touch, in AU; precondition: positive
 * @param speed           of the projectile relative to the target when they touch, in AU per day;
 *                        precondition: non-negative and finite
 */
public record Impact(@NotNull Mass target, @NotNull Mass projectile, double contactDistance, double speed) {

    private static final double G = GravitationalParameter.GRAVITATIONAL_CONSTANT;
    // 1 g/cm³ in solar masses per cubic AU: 1000 kg/m³, the cubic metres in a cubic AU, the
    // kilograms of the Sun (its GM over G)
    private static final double METRES_PER_AU = 1.495978707e11;
    private static final double KILOGRAMS_PER_SUN = 1.3271244e20 / 6.6743e-11;
    private static final double UNIT_DENSITY = 1000.0 * METRES_PER_AU * METRES_PER_AU * METRES_PER_AU
            / KILOGRAMS_PER_SUN;
    // c* of the scaling law for planets, which hold together by gravity alone
    private static final double PLANETARY_COEFFICIENT = 1.9;
    // mu-bar, the exponent of the coupling of the projectile's energy with the target
    private static final double COUPLING_EXPONENT = 0.36;
    // Beyond this energy, in units of Q*_RD, the universal law gives way to a power law
    private static final double SUPER_CATASTROPHIC = 1.8;
    private static final double SUPER_CATASTROPHIC_EXPONENT = -1.5;

    /**
     * @return whether the two bodies merge whole: they meet slower than their mutual escape speed
     *         √(2 G M / d), compared squared
     */
    public boolean merges() {
        return speed * speed < 2.0 * G * totalMass() / contactDistance;
    }

    /**
     * @return the mass of the largest body left by the impact: by the universal law
     *         M_lr = M (1 - Q_R / (2 Q*_RD)) up to 1.8 Q*_RD, where a tenth of the mass is left;
     *         beyond it M_lr = M 0.1 (Q_R / (1.8 Q*_RD))^-1.5, which joins it there
     */
    public @NotNull Mass largestRemnant() {
        double energy = specificEnergy() / disruptionEnergy();
        if (energy < SUPER_CATASTROPHIC) {
            return new Mass(totalMass() * (1.0 - energy / 2.0));
        }
        return new Mass(totalMass() * 0.1 * Math.pow(energy / SUPER_CATASTROPHIC, SUPER_CATASTROPHIC_EXPONENT));
    }

    // Q_R: the kinetic energy in the frame of the centre of mass, (1/2) mu v² with the reduced mass
    // mu = m M / (m + M), per unit of the total mass
    private double specificEnergy() {
        double reducedMass = target.solarMasses() * projectile.solarMasses() / totalMass();
        return 0.5 * reducedMass * speed * speed / totalMass();
    }

    // Q*_RD: the specific energy that leaves half of the mass in the largest remnant. For equal
    // masses, c* times the binding energy per unit mass (4/5) pi rho G R² of a sphere of the total
    // mass at 1 g/cm³; a smaller projectile couples less of its energy with the target, and the
    // threshold grows by ((1 + gamma)² / (4 gamma))^(2 / (3 mu-bar) - 1), gamma = m / M
    private double disruptionEnergy() {
        double radius = Math.cbrt(3.0 * totalMass() / (4.0 * Math.PI * UNIT_DENSITY));
        double equalMasses = PLANETARY_COEFFICIENT * 0.8 * Math.PI * UNIT_DENSITY * G * radius * radius;
        double massRatio = projectile.solarMasses() / target.solarMasses();
        double coupling = (1.0 + massRatio) * (1.0 + massRatio) / (4.0 * massRatio);
        return equalMasses * Math.pow(coupling, 2.0 / (3.0 * COUPLING_EXPONENT) - 1.0);
    }

    /**
     * @return the kinetic energy of the impact in the frame of the centre of mass, (1/2) mu v², in
     *         solar masses times AU squared per day squared
     */
    public double energy() {
        return specificEnergy() * totalMass();
    }

    private double totalMass() {
        return target.solarMasses() + projectile.solarMasses();
    }
}
