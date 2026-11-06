package astronomicalsystem.app;

import astronomicalsystem.app.camera.Camera;
import astronomicalsystem.app.camera.Lens;
import astronomicalsystem.app.camera.Viewport;
import astronomicalsystem.app.presenter.BodiesPresenter;
import astronomicalsystem.app.presenter.BodyStyle;
import astronomicalsystem.app.time.FixedStepTimeline;
import astronomicalsystem.app.time.SystemClock;
import astronomicalsystem.app.time.Timeline;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.GlDiscs;
import astronomicalsystem.app.view.GlfwWindow;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.math.Vector3;
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
import java.util.Map;

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

    // 8 AU from the barycentre, 35 degrees above the ecliptic, with the ecliptic north up: the orbit
    // of Jupiter fills the image
    private static final Camera CAMERA = new Camera(new Vector3(0.0, -6.5, 4.6), Vector3.ZERO,
            new Vector3(0.0, 0.0, 1.0), new Lens(Math.toRadians(45.0), 0.01, 1000.0), new Viewport(WIDTH, HEIGHT));
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.STAR, new BodyStyle(new Colour(1.0, 0.85, 0.45), 16.0),
            BodyKind.PLANET, new BodyStyle(new Colour(0.55, 0.75, 1.0), 7.0),
            BodyKind.MOON, new BodyStyle(new Colour(0.75, 0.75, 0.75), 4.0),
            BodyKind.COMET, new BodyStyle(new Colour(0.6, 1.0, 0.95), 4.0),
            BodyKind.DEBRIS, new BodyStyle(new Colour(0.6, 0.5, 0.4), 3.0));

    private Main() {
    }

    public static void main(String[] args) {
        EphemerisSource solarSystem = SolarSystem.withFallback(
                new HorizonsClient(new JdkHttpTransport(HORIZONS_TIMEOUT), new HorizonsParser()));
        ObservableSimulation simulation = new ObservableSimulation(
                new VelocityVerlet(new NewtonianGravity(GravitationalParameter.GRAVITATIONAL_CONSTANT, NO_SOFTENING)),
                new MergingCollisions(), solarSystem.bodiesAt(JulianDate.at(Instant.now())));
        Timeline timeline = new FixedStepTimeline(simulation, ONE_HOUR, TEN_DAYS_PER_SECOND);
        // The scene needs the OpenGL context of the window
        try (GlfwWindow window = GlfwWindow.open(TITLE, WIDTH, HEIGHT); GlDiscs scene = GlDiscs.create()) {
            simulation.addObserver(new BodiesPresenter(scene, CAMERA, STYLES));
            simulation.notifyObservers();
            new MainLoop(window, new SystemClock(), timeline, scene).run();
        }
    }
}
