package astronomicalsystem.app;

import astronomicalsystem.app.time.FixedStepTimeline;
import astronomicalsystem.app.time.SystemClock;
import astronomicalsystem.app.time.Timeline;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.GlfwWindow;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.physics.MergingCollisions;
import astronomicalsystem.core.physics.NewtonianGravity;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.physics.VelocityVerlet;
import astronomicalsystem.core.simulation.ObservableSimulation;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.EphemerisSource;
import astronomicalsystem.ephemeris.SolarSystem;
import astronomicalsystem.ephemeris.horizons.HorizonsClient;
import astronomicalsystem.ephemeris.horizons.HorizonsParser;
import astronomicalsystem.ephemeris.horizons.JdkHttpTransport;

import java.time.Duration;
import java.time.Instant;

/**
 * Creates the solar system as it is now, the simulation and the window, and connects them.
 */
public final class Main {

    private static final String TITLE = "Astronomical System";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final Duration HORIZONS_TIMEOUT = Duration.ofSeconds(10);
    // Exact Newtonian attraction: bodies never get closer than the sum of their radii, since they merge
    private static final double NO_SOFTENING = 0.0;
    // About 2100 steps along the orbit of Mercury, the fastest planet
    private static final TimeStep ONE_HOUR = new TimeStep(1.0 / 24.0);
    // Mercury goes round the Sun in about nine seconds, the Earth in about thirty-seven
    private static final TimeWarp TEN_DAYS_PER_SECOND = new TimeWarp(10.0);

    private Main() {
    }

    public static void main(String[] args) {
        EphemerisSource solarSystem = SolarSystem.withFallback(
                new HorizonsClient(new JdkHttpTransport(HORIZONS_TIMEOUT), new HorizonsParser()));
        ObservableSimulation simulation = new ObservableSimulation(
                new VelocityVerlet(new NewtonianGravity(GravitationalParameter.GRAVITATIONAL_CONSTANT, NO_SOFTENING)),
                new MergingCollisions(), solarSystem.bodiesAt(JulianDate.at(Instant.now())));
        Timeline timeline = new FixedStepTimeline(simulation, ONE_HOUR, TEN_DAYS_PER_SECOND);
        try (GlfwWindow window = GlfwWindow.open(TITLE, WIDTH, HEIGHT)) {
            new MainLoop(window, new SystemClock(), timeline).run();
        }
    }
}
