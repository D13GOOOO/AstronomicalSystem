package astronomicalsystem.factory;

import java.util.Objects;

import astronomicalsystem.api.NasaHorizonsClient;
import astronomicalsystem.api.NasaState;
import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import astronomicalsystem.model.Comet;
import astronomicalsystem.model.Moon;
import astronomicalsystem.model.Planet;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.Star;

/**
 * A creational factory responsible for assembling and initializing the physical
 * and informational state of the astronomical simulation.
 * <p>
 * This class encapsulates all domain data (masses, orbital parameters, UI metadata).
 * It now features a live-sync engine that connects to NASA's JPL Horizons API to
 * fetch real-time state vectors, adapting them via mathematical scalars to fit
 * perfectly within the simulation's bounded coordinate system.
 * </p>
 */
public final class UniverseFactory {

    private static final double M_EARTH_SIM = 50.0;
    private static final double M_SUN_SIM = M_EARTH_SIM * 200.0;

    /**
     * The Standard Gravitational Parameter (mu = G * M) for the central anchor (the Sun).
     */
    private static final double GM = M_SUN_SIM * SimulationSettings.defaults().physics().gravitationalConstant();

    /**
     * 1 Astronomical Unit (AU) in Kilometers.
     */
    private static final double AU_IN_KM = 149597870.7;

    /**
     * Private constructor to prevent instantiation of this factory class.
     */
    private UniverseFactory() {
        throw new UnsupportedOperationException("Factory classes cannot be instantiated.");
    }

    /**
     * Constructs a fully populated solar system by querying the NASA JPL API in real-time.
     * Places planets exactly where they are in the real universe today, applying a smart 
     * visual compression algorithm to keep everything on screen.
     *
     * @return an initialized {@link AstronomicalSystem} ready for simulation.
     */
    public static AstronomicalSystem createLiveSolarSystem() {
        AstronomicalSystem system = new AstronomicalSystem();
        NasaHorizonsClient nasaClient = new NasaHorizonsClient();

        System.out.println("====== INIZIALIZZAZIONE SISTEMA SOLARE ======");
        System.out.println("Richiesta coordinate reali NASA in corso...");

        Star sun = new Star("Sun", M_SUN_SIM, 20.0, Point3D.ZERO);
        sun.setMetadata(new BodyMetadata("1.989 × 10^30 kg", "696,340 km", "5,500°C (Surface)", "25 Days", "N/A"));
        system.addBody(sun);

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "199", "Mercury", M_EARTH_SIM * 0.055, 350,
                new BodyMetadata("3.285 × 10^23 kg", "2,439 km", "-173°C / 427°C", "58.6 Days", "88 Days")));

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "299", "Venus", M_EARTH_SIM * 0.815, 500,
                new BodyMetadata("4.867 × 10^24 kg", "6,051 km", "462°C (Avg)", "243 Days", "225 Days")));

        Planet earth = fetchAndCreateLivePlanet(nasaClient, "399", "Earth", M_EARTH_SIM, 700,
                new BodyMetadata("5.972 × 10^24 kg", "6,371 km", "15°C (Avg)", "24 Hours", "365.25 Days"));
        system.addBody(earth);

        Moon moon = createMoon("Moon", M_EARTH_SIM * 0.0123, 60.0, 0.04, earth,
                new BodyMetadata("7.342 × 10^22 kg", "1,737 km", "-23°C (Avg)", "27.3 Days", "27.3 Days"));
        system.addBody(moon);

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "499", "Mars", M_EARTH_SIM * 0.107, 950,
                new BodyMetadata("6.39 × 10^23 kg", "3,389 km", "-65°C (Avg)", "24h 37m", "687 Days")));

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "599", "Jupiter", M_EARTH_SIM * 318, 1600,
                new BodyMetadata("1.898 × 10^27 kg", "69,911 km", "-110°C", "9h 56m", "11.86 Years")));

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "699", "Saturn", M_EARTH_SIM * 95, 2200,
                new BodyMetadata("5.683 × 10^26 kg", "58,232 km", "-140°C", "10h 34m", "29.45 Years")));

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "799", "Uranus", M_EARTH_SIM * 14.5, 2800,
                new BodyMetadata("8.681 × 10^25 kg", "25,362 km", "-195°C", "17h 14m", "84 Years")));

        system.addBody(fetchAndCreateLivePlanet(nasaClient, "899", "Neptune", M_EARTH_SIM * 17, 3300,
                new BodyMetadata("1.024 × 10^26 kg", "24,622 km", "-200°C", "16h 6m", "164.8 Years")));

        system.addBody(fetchAndCreateLiveComet(nasaClient, "90000033", "Halley's Comet", M_EARTH_SIM * 0.0001, 2600, 1.5,
                new BodyMetadata("2.2 × 10^14 kg", "11 km", "-220°C to 50°C", "2.2 Days", "75.3 Years")));

        System.out.println("====== SISTEMA SOLARE PRONTO ======");
        return system;
    }

    /**
     * Transforms true real-world distances into simulator distances using a piecewise algorithm.
     * Inner planets get a wide scale to avoid overlapping the Sun's 3D mesh.
     * Outer planets get a compressed scale so they don't fly off the edge of the screen.
     *
     * @param realDistanceKm the true distance in kilometers.
     * @return the scaled distance in simulation units.
     */
    private static double calculateVisualDistance(double realDistanceKm) {
        double distanceAU = realDistanceKm / AU_IN_KM;

        if (distanceAU <= 2.0) {
            return distanceAU * 800.0; 
        } else {
            return (2.0 * 800.0) + ((distanceAU - 2.0) * 150.0);
        }
    }

    /**
     * Attempts to build a planet using real NASA data. If the network request fails, 
     * it safely falls back to the hardcoded Keplerian mathematics.
     *
     * @param client       the active NASA API client.
     * @param nasaId       the official JPL identifier for the body.
     * @param name         the simulation name of the planet.
     * @param mass         the mass of the planet in simulation units.
     * @param fallbackAxis the fallback semi-major axis if offline.
     * @param metadata     the UI data record.
     * @return a fully initialized {@link Planet} instance.
     */
    private static Planet fetchAndCreateLivePlanet(NasaHorizonsClient client, String nasaId, String name, double mass, double fallbackAxis, BodyMetadata metadata) {
        NasaState state = client.fetchTodayState(nasaId);

        if (state == null) {
            System.err.println("-> " + name + ": API irraggiungibile. Utilizzo matematica orbitale di fallback.");
            return createPlanet(name, mass, fallbackAxis, metadata);
        }

        System.out.println("-> " + name + ": Sincronizzato con successo (NASA JPL)");

        double realDistanceKm = Math.sqrt(state.x() * state.x() + state.y() * state.y() + state.z() * state.z());
        double simDistance = calculateVisualDistance(realDistanceKm);
        double scaleFactor = simDistance / realDistanceKm;

        double simX = state.x() * scaleFactor;
        double simY = state.y() * scaleFactor;
        double simZ = state.z() * scaleFactor;
        Point3D simPosition = new Point3D(simX, simY, simZ);

        double distanceToSun = simPosition.magnitude();
        double idealSpeed = Math.sqrt(GM / distanceToSun); 

        Point3D nasaVelocity = new Point3D(state.vx(), state.vy(), state.vz());
        Point3D simVelocity = nasaVelocity.normalize().scale(idealSpeed);

        Planet planet = new Planet(name, mass, 1.0, simPosition);
        planet.setVelocity(simVelocity);
        planet.setMetadata(metadata);

        return planet;
    }

    /**
     * Attempts to build a comet using real NASA data. If the network request fails, 
     * it safely falls back to the hardcoded Keplerian mathematics.
     *
     * @param client         the active NASA API client.
     * @param nasaId         the official JPL identifier for the comet.
     * @param name           the simulation name of the comet.
     * @param mass           the mass of the comet in simulation units.
     * @param fallbackAxis   the fallback semi-major axis if offline.
     * @param baseIceContent the total potential intensity of the comet's tail.
     * @param metadata       the UI data record.
     * @return a fully initialized {@link Comet} instance.
     */
    private static Comet fetchAndCreateLiveComet(NasaHorizonsClient client, String nasaId, String name, double mass, double fallbackAxis, double baseIceContent, BodyMetadata metadata) {
        NasaState state = client.fetchTodayState(nasaId);

        if (state == null) {
            System.err.println("-> " + name + ": API irraggiungibile. Utilizzo matematica orbitale di fallback.");
            Planet fallbackPlanet = createPlanet(name, mass, fallbackAxis, metadata);
            Comet comet = new Comet(name, mass, 1.0, fallbackPlanet.getPosition(), baseIceContent);
            comet.setVelocity(fallbackPlanet.getVelocity());
            comet.setMetadata(metadata);
            return comet;
        }

        System.out.println("-> " + name + ": Sincronizzato con successo (NASA JPL)");

        double realDistanceKm = Math.sqrt(state.x() * state.x() + state.y() * state.y() + state.z() * state.z());
        double simDistance = calculateVisualDistance(realDistanceKm);
        double scaleFactor = simDistance / realDistanceKm;

        double simX = state.x() * scaleFactor;
        double simY = state.y() * scaleFactor;
        double simZ = state.z() * scaleFactor;
        Point3D simPosition = new Point3D(simX, simY, simZ);

        double distanceToSun = simPosition.magnitude();
        double idealSpeed = Math.sqrt(GM / distanceToSun); 

        Point3D nasaVelocity = new Point3D(state.vx(), state.vy(), state.vz());
        Point3D simVelocity = nasaVelocity.normalize().scale(idealSpeed);

        Comet comet = new Comet(name, mass, 1.0, simPosition, baseIceContent);
        comet.setVelocity(simVelocity);
        comet.setMetadata(metadata);

        return comet;
    }

    /**
     * Constructs a fully populated solar system using local, hardcoded mathematics.
     * Retained for legacy support, testing, and offline fallback.
     *
     * @return an initialized {@link AstronomicalSystem} ready for simulation.
     */
    public static AstronomicalSystem createSolarSystem() {
        return createLiveSolarSystem();
    }

    /**
     * Instantiates a new planetary body and calculates its initial kinematic state
     * relative to the central coordinate system origin (the Sun).
     *
     * @param name          the simulation name of the planet.
     * @param mass          the mass of the planet in simulation units.
     * @param semiMajorAxis the fallback semi-major axis.
     * @param metadata      the UI data record.
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
        Objects.requireNonNull(planetName, "Planet name cannot be null");
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 0.205;
            case "venus"   -> 0.007;
            case "earth"   -> 0.017;
            case "moon"    -> 0.0;
            case "mars"    -> 0.094;
            case "jupiter" -> 0.049;
            case "saturn"  -> 0.057;
            case "uranus"  -> 0.046;
            case "neptune" -> 0.011;
            default        -> 0.0;
        };
    }

    /**
     * Provides the orbital inclination relative to the ecliptic plane.
     *
     * @param planetName the name of the entity.
     * @return the inclination in degrees.
     */
    public static double getInclination(String planetName) {
        Objects.requireNonNull(planetName, "Planet name cannot be null");
        return switch (planetName.toLowerCase()) {
            case "mercury" -> 7.0;
            case "venus"   -> 3.39;
            case "earth"   -> 0.0;
            case "moon"    -> 5.14;
            case "mars"    -> 1.85;
            case "jupiter" -> 1.3;
            case "saturn"  -> 2.48;
            case "uranus"  -> 0.77;
            case "neptune" -> 1.77;
            default        -> 0.0;
        };
    }
}