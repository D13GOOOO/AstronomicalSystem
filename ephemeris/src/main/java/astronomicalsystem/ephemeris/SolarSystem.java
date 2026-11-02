package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.orbit.Eccentricity;
import astronomicalsystem.core.orbit.OrbitalElements;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.horizons.Horizons;
import astronomicalsystem.ephemeris.horizons.HorizonsCommand;
import astronomicalsystem.ephemeris.horizons.HorizonsEphemeris;
import astronomicalsystem.ephemeris.horizons.HorizonsTarget;
import astronomicalsystem.ephemeris.kepler.KeplerEphemeris;
import astronomicalsystem.ephemeris.kepler.KeplerOrbit;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Catalogue of the Sun and the planets.
 * <p>
 * Sources: masses from the GM values of the JPL ephemeris DE440, each planet together with its
 * moons (https://ssd.jpl.nasa.gov/astro_par.html); mean radii from the IAU 2015 report
 * (https://ssd.jpl.nasa.gov/planets/phys_par.html) and the nominal solar radius of IAU 2015
 * Resolution B3; orbital elements from table 1 of Standish and Williams, mean ecliptic and
 * equinox of J2000, valid from 1800 to 2050 (https://ssd.jpl.nasa.gov/planets/approx_pos.html).
 */
public final class SolarSystem {

    private static final double KILOMETRES_PER_AU = 149_597_870.7;
    private static final double SUN_GM_KM3_PER_S2 = 1.32712440041279419e11;
    private static final JulianDate J2000 = new JulianDate(2451545.0);

    private static final BodyProperties SUN =
            new BodyProperties(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), radius(695_700.0));

    // Each planet stands for its whole system: the mass includes the moons, and the elements and the
    // ephemerides describe the barycentre, which for the Earth is the Earth-Moon barycentre
    private static final BodyProperties MERCURY = planet("Mercury", 22_031.868551, 2_439.4);
    private static final BodyProperties VENUS = planet("Venus", 324_858.592000, 6_051.8);
    private static final BodyProperties EARTH = planet("Earth", 398_600.435507 + 4_902.800118, 6_371.0084);
    private static final BodyProperties MARS = planet("Mars", 42_828.375816, 3_389.50);
    private static final BodyProperties JUPITER = planet("Jupiter", 126_712_764.100000, 69_911.0);
    private static final BodyProperties SATURN = planet("Saturn", 37_940_584.841800, 58_232.0);
    private static final BodyProperties URANUS = planet("Uranus", 5_794_556.400000, 25_362.0);
    private static final BodyProperties NEPTUNE = planet("Neptune", 6_836_527.100580, 24_622.0);

    private static final List<KeplerOrbit> MEAN_ORBITS = List.of(
            new KeplerOrbit(MERCURY,
                    standish(0.38709927, 0.20563593, 7.00497902, 252.25032350, 77.45779628, 48.33076593)),
            new KeplerOrbit(VENUS,
                    standish(0.72333566, 0.00677672, 3.39467605, 181.97909950, 131.60246718, 76.67984255)),
            // The table gives I = -0.00001531 and a node at 0: the same plane with a positive
            // inclination has its ascending node on the opposite side, at 180
            new KeplerOrbit(EARTH,
                    standish(1.00000261, 0.01671123, 0.00001531, 100.46457166, 102.93768193, 180.0)),
            new KeplerOrbit(MARS,
                    standish(1.52371034, 0.09339410, 1.84969142, -4.55343205, -23.94362959, 49.55953891)),
            new KeplerOrbit(JUPITER,
                    standish(5.20288700, 0.04838624, 1.30439695, 34.39644051, 14.72847983, 100.47390909)),
            new KeplerOrbit(SATURN,
                    standish(9.53667594, 0.05386179, 2.48599187, 49.95424423, 92.59887831, 113.66242448)),
            new KeplerOrbit(URANUS,
                    standish(19.18916464, 0.04725744, 0.77263783, 313.23810451, 170.95427630, 74.01692503)),
            new KeplerOrbit(NEPTUNE,
                    standish(30.06992276, 0.00859048, 1.77004347, -55.12002969, 44.96476227, 131.78422574)));

    // Horizons numbers the barycentre of each planetary system from 1 to 8, outwards from the Sun
    private static final List<HorizonsTarget> HORIZONS_TARGETS = List.of(target(SUN, "10"), target(MERCURY, "1"),
            target(VENUS, "2"), target(EARTH, "3"), target(MARS, "4"), target(JUPITER, "5"), target(SATURN, "6"),
            target(URANUS, "7"), target(NEPTUNE, "8"));

    private SolarSystem() {
    }

    /**
     * @return the Sun and the planets on their mean orbits around it
     */
    public static @NotNull KeplerEphemeris meanOrbits() {
        return new KeplerEphemeris(SUN, J2000, MEAN_ORBITS);
    }

    /**
     * @return the Sun and the barycentres of the planetary systems where Horizons puts them
     */
    public static @NotNull EphemerisSource fromHorizons(@NotNull Horizons horizons) {
        return new HorizonsEphemeris(horizons, HORIZONS_TARGETS);
    }

    /**
     * @return the Sun and the planets where Horizons puts them or, when Horizons has no usable
     *         answer, on their mean orbits
     */
    public static @NotNull EphemerisSource withFallback(@NotNull Horizons horizons) {
        return new FallbackEphemeris(fromHorizons(horizons), meanOrbits());
    }

    private static @NotNull BodyProperties planet(@NotNull String name, double gmKm3PerS2, double meanRadiusKm) {
        return new BodyProperties(new BodyName(name), BodyKind.PLANET, new Mass(gmKm3PerS2 / SUN_GM_KM3_PER_S2),
                radius(meanRadiusKm));
    }

    private static @NotNull HorizonsTarget target(@NotNull BodyProperties body, @NotNull String command) {
        return new HorizonsTarget(body, new HorizonsCommand(command));
    }

    private static @NotNull Radius radius(double kilometres) {
        return new Radius(kilometres / KILOMETRES_PER_AU);
    }

    /**
     * Elements in the units and column order of Standish's table: AU and degrees.
     */
    private static @NotNull OrbitalElements standish(double semiMajorAxis, double eccentricity, double inclination,
                                                     double meanLongitude, double longitudeOfPerihelion,
                                                     double longitudeOfAscendingNode) {
        return OrbitalElements.fromLongitudes(semiMajorAxis, new Eccentricity(eccentricity),
                Math.toRadians(inclination), Math.toRadians(meanLongitude), Math.toRadians(longitudeOfPerihelion),
                Math.toRadians(longitudeOfAscendingNode));
    }
}
