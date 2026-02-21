package astronomicalsystem.factory;

import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import astronomicalsystem.model.Interactions;
import astronomicalsystem.model.Moon;
import astronomicalsystem.model.Planet;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.Star;

/**
 * A creational factory responsible for assembling and initializing the physical
 * and informational state of the astronomical simulation.
 * <p>
 * This class encapsulates all hardcoded domain data (masses, orbital parameters,
 * UI metadata) and initial kinematic calculations, strictly separating data
 * generation from the presentation and simulation layers.
 * </p>
 */
public final class UniverseFactory {

    private static final double M_EARTH_SIM = 50.0;
    private static final double M_SUN_SIM = M_EARTH_SIM * 200.0;
    private static final double GM = M_SUN_SIM * Interactions.G;

    /**
     * Private constructor to prevent instantiation of this factory class.
     */
    private UniverseFactory() {
        throw new UnsupportedOperationException("Factory classes cannot be instantiated.");
    }

    /**
     * Constructs a fully populated solar system with accurate relative masses,
     * eccentricities, and inclinations, including host-bound satellite bodies.
     *
     * @return an initialized {@link AstronomicalSystem} ready for simulation.
     */
    public static AstronomicalSystem createSolarSystem() {
        AstronomicalSystem system = new AstronomicalSystem();

        Star sun = new Star("Sun", M_SUN_SIM, 20.0, new Point3D(0, 0, 0));
        sun.setMetadata(new BodyMetadata("1.989 × 10^30 kg", "696,340 km", "5,500°C (Surface)", "25 Days", "N/A"));
        system.addBody(sun);

        system.addBody(createPlanet("Mercury", M_EARTH_SIM * 0.055, 350,
                new BodyMetadata("3.285 × 10^23 kg", "2,439 km", "-173°C / 427°C", "58.6 Days", "88 Days")));

        system.addBody(createPlanet("Venus", M_EARTH_SIM * 0.815, 500,
                new BodyMetadata("4.867 × 10^24 kg", "6,051 km", "462°C (Avg)", "243 Days", "225 Days")));

        Planet earth = createPlanet("Earth", M_EARTH_SIM, 700,
                new BodyMetadata("5.972 × 10^24 kg", "6,371 km", "15°C (Avg)", "24 Hours", "365.25 Days"));
        system.addBody(earth);

        Moon moon = createMoon("Moon", M_EARTH_SIM * 0.0123, 60.0, 0.04, earth,
                new BodyMetadata("7.342 × 10^22 kg", "1,737 km", "-23°C (Avg)", "27.3 Days", "27.3 Days"));
        system.addBody(moon);

        system.addBody(createPlanet("Mars", M_EARTH_SIM * 0.107, 950,
                new BodyMetadata("6.39 × 10^23 kg", "3,389 km", "-65°C (Avg)", "24h 37m", "687 Days")));

        system.addBody(createPlanet("Jupiter", M_EARTH_SIM * 318, 1600,
                new BodyMetadata("1.898 × 10^27 kg", "69,911 km", "-110°C", "9h 56m", "11.86 Years")));

        system.addBody(createPlanet("Saturn", M_EARTH_SIM * 95, 2200,
                new BodyMetadata("5.683 × 10^26 kg", "58,232 km", "-140°C", "10h 34m", "29.45 Years")));

        system.addBody(createPlanet("Uranus", M_EARTH_SIM * 14.5, 2800,
                new BodyMetadata("8.681 × 10^25 kg", "25,362 km", "-195°C", "17h 14m", "84 Years")));

        system.addBody(createPlanet("Neptune", M_EARTH_SIM * 17, 3300,
                new BodyMetadata("1.024 × 10^26 kg", "24,622 km", "-200°C", "16h 6m", "164.8 Years")));

        return system;
    }

    /**
     * Instantiates a new planetary body and calculates its initial kinematic state
     * relative to the central coordinate system origin (the Sun).
     *
     * @param name          the unique identifier of the planet.
     * @param mass          the mass of the planet in simulation units.
     * @param semiMajorAxis the semi-major axis of the planet's orbit.
     * @param metadata      the immutable record containing descriptive UI data.
     * @return a fully initialized {@link Planet} instance.
     */
    private static Planet createPlanet(String name, double mass, double semiMajorAxis, BodyMetadata metadata) {
        double e = getEccentricity(name);
        double iDeg = getInclination(name);
        double iRad = Math.toRadians(iDeg);

        double rp = semiMajorAxis * (1.0 - e);

        double vTotal = Math.sqrt((GM / semiMajorAxis) * ((1.0 + e) / (1.0 - e)));

        double vy = vTotal * Math.sin(iRad);
        double vz = vTotal * Math.cos(iRad);

        Planet planet = new Planet(name, mass, 1.0, new Point3D(rp, 0, 0));
        planet.setVelocity(new Point3D(0, -vy, vz));
        planet.setMetadata(metadata);

        return planet;
    }

    /**
     * Instantiates a parametric natural satellite locked to a host body.
     *
     * @param name     the identifier of the moon.
     * @param mass     the mass in simulation units.
     * @param distance the fixed orbital radius.
     * @param speed    the angular velocity per integration step.
     * @param host     the central entity.
     * @param metadata the UI data record.
     * @return a fully initialized {@link Moon}.
     */
    private static Moon createMoon(String name, double mass, double distance, double speed, Planet host, BodyMetadata metadata) {
        Moon moon = new Moon(name, mass, 1.0, host, distance, speed);
        moon.setMetadata(metadata);
        return moon;
    }

    /**
     * Provides the orbital eccentricity for known celestial bodies.
     *
     * @param planetName the name of the entity.
     * @return the scalar eccentricity value.
     */
    public static double getEccentricity(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 0.205;
            case "venus" -> 0.007;
            case "earth" -> 0.017;
            case "moon" -> 0.0;
            case "mars" -> 0.094;
            case "jupiter" -> 0.049;
            case "saturn" -> 0.057;
            case "uranus" -> 0.046;
            case "neptune" -> 0.011;
            default -> 0.0;
        };
    }

    /**
     * Provides the orbital inclination relative to the ecliptic plane.
     *
     * @param planetName the name of the entity.
     * @return the inclination in degrees.
     */
    public static double getInclination(String planetName) {
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 7.0;
            case "venus" -> 3.39;
            case "earth" -> 0.0;
            case "moon" -> 5.14;
            case "mars" -> 1.85;
            case "jupiter" -> 1.3;
            case "saturn" -> 2.48;
            case "uranus" -> 0.77;
            case "neptune" -> 1.77;
            default -> 0.0;
        };
    }
}