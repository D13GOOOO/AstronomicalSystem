package astronomicalsystem.app;

import astronomicalsystem.app.camera.Lens;
import astronomicalsystem.app.camera.ObservableCameraRig;
import astronomicalsystem.app.camera.Orbit;
import astronomicalsystem.app.camera.Viewport;
import astronomicalsystem.app.follow.ObservableBodyFollower;
import astronomicalsystem.app.forecast.Forecast;
import astronomicalsystem.app.impact.Blasts;
import astronomicalsystem.app.impact.Launcher;
import astronomicalsystem.app.impact.SlowMotion;
import astronomicalsystem.app.presenter.BodiesPresenter;
import astronomicalsystem.app.presenter.BodyStyle;
import astronomicalsystem.app.presenter.CameraControlsPresenter;
import astronomicalsystem.app.presenter.ControlsHelpPresenter;
import astronomicalsystem.app.presenter.FlaresPresenter;
import astronomicalsystem.app.presenter.FocusPresenter;
import astronomicalsystem.app.presenter.LabelsPresenter;
import astronomicalsystem.app.presenter.ProvenancePresenter;
import astronomicalsystem.app.presenter.Skin;
import astronomicalsystem.app.presenter.Skins;
import astronomicalsystem.app.presenter.KeyboardPresenter;
import astronomicalsystem.app.presenter.TimeFlowPresenter;
import astronomicalsystem.app.presenter.RingsPresenter;
import astronomicalsystem.app.presenter.ScreenshotPresenter;
import astronomicalsystem.app.presenter.SkyPresenter;
import astronomicalsystem.app.presenter.TrajectoriesPresenter;
import astronomicalsystem.app.sky.Stars;
import astronomicalsystem.app.texture.BundledMaps;
import astronomicalsystem.app.texture.CachedMapFiles;
import astronomicalsystem.app.texture.HttpMapFiles;
import astronomicalsystem.app.texture.MapDeliveries;
import astronomicalsystem.app.texture.MapDownloads;
import astronomicalsystem.app.texture.MapSource;
import astronomicalsystem.app.texture.RetryingMapFiles;
import astronomicalsystem.app.time.ObservableTimeline;
import astronomicalsystem.app.time.SystemClock;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Corner;
import astronomicalsystem.app.view.GlDiscs;
import astronomicalsystem.app.view.GlFont;
import astronomicalsystem.app.view.GlGlows;
import astronomicalsystem.app.view.GlLabels;
import astronomicalsystem.app.view.GlPaths;
import astronomicalsystem.app.view.GlRings;
import astronomicalsystem.app.view.GlScreenshot;
import astronomicalsystem.app.view.GlText;
import astronomicalsystem.app.view.GlTextures;
import astronomicalsystem.app.view.GlfwWindow;
import astronomicalsystem.app.view.Layers;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.impact.FragmentSizes;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.GravitationalParameter;
import astronomicalsystem.core.physics.FragmentingCollisions;
import astronomicalsystem.core.physics.NewtonianGravity;
import astronomicalsystem.core.physics.TimeStep;
import astronomicalsystem.core.physics.VelocityVerlet;
import astronomicalsystem.core.rotation.Spin;
import astronomicalsystem.core.simulation.ObservableSimulation;
import astronomicalsystem.core.simulation.Sampling;
import astronomicalsystem.core.simulation.TrajectoryPredictor;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.Ephemeris;
import astronomicalsystem.ephemeris.EphemerisSource;
import astronomicalsystem.ephemeris.SolarSystem;
import astronomicalsystem.ephemeris.horizons.HorizonsClient;
import astronomicalsystem.ephemeris.horizons.HorizonsParser;
import astronomicalsystem.ephemeris.horizons.JdkHttpTransport;

import java.net.URI;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Creates the solar system as it is now, the simulation and the window, and connects them.
 */
public final class Main {

    private static final String TITLE = "Astronomical System";
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final Duration HORIZONS_TIMEOUT = Duration.ofSeconds(10);
    // Exact Newtonian attraction: bodies never get closer than the sum of their radii, since they collide
    private static final double NO_SOFTENING = 0.0;
    // Impacts break off at most 24 fragments, none lighter than 2e18 kg, a body some 100 km across:
    // enough to see the debris fly, few enough to keep every body in the gravity of the others
    private static final FragmentSizes FRAGMENT_SIZES = new FragmentSizes(new Mass(1e-12), 24);
    // About 2100 steps along the orbit of Mercury, the fastest planet
    private static final TimeStep ONE_HOUR = new TimeStep(1.0 / 24.0);
    // Mercury goes round the Sun in about nine seconds, the Earth in about thirty-seven; the user
    // doubles or halves it with + and -, and pauses it with the space bar
    private static final TimeWarp TEN_DAYS_PER_SECOND = new TimeWarp(10.0);
    // At slow warps the steps are shorter than an hour, so that every frame shows the bodies moving
    private static final int A_STEP_PER_FRAME = 60;

    // The camera turns around the barycentre; it starts 8 AU away and 35 degrees above the ecliptic,
    // where the orbit of Jupiter fills the image
    private static final Vector3 BARYCENTRE = Vector3.ZERO;
    private static final Orbit FIRST_VIEW = new Orbit(8.0, 0.0, Math.toRadians(35.0));
    private static final Lens LENS = new Lens(Math.toRadians(45.0), 0.01, 1000.0);
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.STAR, new BodyStyle(new Colour(1.0, 0.85, 0.45), 16.0, true),
            BodyKind.PLANET, new BodyStyle(new Colour(0.55, 0.75, 1.0), 7.0, false),
            BodyKind.MOON, new BodyStyle(new Colour(0.75, 0.75, 0.75), 4.0, false),
            BodyKind.COMET, new BodyStyle(new Colour(0.6, 1.0, 0.95), 4.0, false),
            BodyKind.ASTEROID, new BodyStyle(new Colour(0.85, 0.7, 0.55), 4.0, false),
            BodyKind.DEBRIS, new BodyStyle(new Colour(0.6, 0.5, 0.4), 3.0, false));
    // The bodies wrapped in a map of their own, and the name of the map
    private static final Map<String, String> MAPS = Map.of("Sun", "sun", "Mercury", "mercury", "Venus", "venus",
            "Earth", "earth", "Mars", "mars", "Jupiter", "jupiter", "Saturn", "saturn", "Uranus", "uranus",
            "Neptune", "neptune");
    // The maps that come with the application: no public service has them
    private static final List<String> BUNDLED_MAPS = List.of("sun", "venus", "jupiter", "saturn", "uranus", "neptune");
    // The maps that NASA serves, fetched in the background at the first start and kept on disk: the
    // Earth in one image of 4096 x 2048 pixels from GIBS, the others in tiles of 256 pixels from the
    // Solar System Treks, 16 x 8 of them (4096 x 2048) or 8 x 4 (2048 x 1024)
    private static final String TREK = "https://trek.nasa.gov/tiles/";
    private static final int SIXTEEN_BY_EIGHT_TILES = 3;
    private static final int EIGHT_BY_FOUR_TILES = 2;
    private static final List<MapSource> NASA_MAPS = List.of(
            new MapSource("earth", 1, List.of(URI.create("https://gibs.earthdata.nasa.gov/wms/epsg4326/best/wms.cgi"
                    + "?SERVICE=WMS&VERSION=1.1.1&REQUEST=GetMap&LAYERS=BlueMarble_ShadedRelief_Bathymetry&STYLES="
                    + "&FORMAT=image/jpeg&SRS=EPSG:4326&BBOX=-180,-90,180,90&WIDTH=4096&HEIGHT=2048"))),
            MapSource.ofTrek("mercury",
                    TREK + "Mercury/EQ/Mercury_MESSENGER_MDIS_Basemap_MD3Color_Mosaic_Global_665m",
                    SIXTEEN_BY_EIGHT_TILES),
            MapSource.ofTrek("mars", TREK + "Mars/EQ/Mars_Viking_MDIM21_ClrMosaic_global_232m",
                    SIXTEEN_BY_EIGHT_TILES),
            MapSource.ofTrek("moon", TREK + "Moon/EQ/LRO_WAC_Mosaic_Global_303ppd_v02", EIGHT_BY_FOUR_TILES),
            MapSource.ofTrek("vesta", TREK + "Vesta/EQ/Vesta_Dawn_FC_HAMO_Mosaic_Global_74ppd",
                    EIGHT_BY_FOUR_TILES));
    // Water ice with a little dust: a pale sand
    private static final Colour RING_COLOUR = new Colour(0.86, 0.78, 0.64);
    // The sky is made up, the same at every run
    private static final long SKY_SEED = 1610L;
    private static final int STARS = 4000;
    private static final Duration MAP_TIMEOUT = Duration.ofSeconds(30);
    private static final int MAP_ATTEMPTS = 3;
    private static final int MAP_THREADS = 4;
    private static final Path MAP_CACHE = Path.of(System.getProperty("user.home"), ".astronomicalsystem", "maps");
    // Screenshots go with the user's other pictures
    private static final Path PICTURES = Path.of(System.getProperty("user.home"), "Pictures", "Astronomical System");
    // Bodies not known by name look like a body of their kind: a protoplanet like the Moon, asteroids
    // and debris like Vesta; nobody knows how they turn, so they do not
    private static final Spin STILL = new Spin(new Vector3(0.0, 0.0, 1.0), new Vector3(1.0, 0.0, 0.0),
            new JulianDate(2451545.0), 0.0);
    private static final Map<BodyKind, Skin> KIND_SKINS = Map.of(BodyKind.PLANET, new Skin("moon", STILL),
            BodyKind.ASTEROID, new Skin("vesta", STILL), BodyKind.DEBRIS, new Skin("vesta", STILL));
    // Debris comes in dozens of fragments, whose names would cover each other
    private static final Set<BodyKind> UNNAMED = Set.of(BodyKind.DEBRIS);
    // The colours of the bodies at half their brightness: the paths stay behind the discs
    private static final Map<BodyKind, Colour> PATH_COLOURS = Map.of(
            BodyKind.STAR, new Colour(0.5, 0.425, 0.225),
            BodyKind.PLANET, new Colour(0.275, 0.375, 0.5),
            BodyKind.MOON, new Colour(0.375, 0.375, 0.375),
            BodyKind.COMET, new Colour(0.3, 0.5, 0.475),
            BodyKind.ASTEROID, new Colour(0.425, 0.35, 0.275),
            BodyKind.DEBRIS, new Colour(0.3, 0.25, 0.2));
    // The paths look a year ahead, a point a day. The prediction takes steps of a day, not of an
    // hour like the simulation: 365 steps instead of 8766, and the paths differ by less than a pixel
    private static final TimeStep ONE_DAY = new TimeStep(1.0);
    private static final Sampling A_YEAR_DAY_BY_DAY = new Sampling(1, 366);
    // The simulation tells its bodies once per frame: about fifteen predictions a second, each a
    // couple of milliseconds
    private static final int FRAMES_PER_PREDICTION = 4;

    private Main() {
    }

    // Every mapped body with its map and the way it turns, from the catalogue
    private static Skins skins() {
        Map<BodyName, Spin> spins = SolarSystem.spins();
        return new Skins(MAPS.entrySet().stream().collect(Collectors.toMap(map -> new BodyName(map.getKey()),
                map -> new Skin(map.getValue(), spins.get(new BodyName(map.getKey()))))), KIND_SKINS);
    }

    // Threads that do not keep the application open when its window is closed
    private static Executor mapThreads() {
        return Executors.newFixedThreadPool(MAP_THREADS, download -> {
            Thread thread = new Thread(download, "maps");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static void main(String[] args) {
        EphemerisSource solarSystem = SolarSystem.withFallback(
                new HorizonsClient(new JdkHttpTransport(HORIZONS_TIMEOUT), new HorizonsParser()));
        JulianDate now = JulianDate.at(Instant.now());
        Ephemeris initialState = solarSystem.ephemerisAt(now);
        VelocityVerlet gravity = new VelocityVerlet(
                new NewtonianGravity(GravitationalParameter.GRAVITATIONAL_CONSTANT, NO_SOFTENING));
        // Without state of their own, the gravity and the collisions serve the prediction too
        FragmentingCollisions collisions = new FragmentingCollisions(FRAGMENT_SIZES);
        ObservableSimulation simulation = new ObservableSimulation(gravity, collisions, initialState.bodies());
        Forecast forecast = new Forecast(
                new TrajectoryPredictor(gravity, collisions, ONE_DAY, A_YEAR_DAY_BY_DAY),
                FRAMES_PER_PREDICTION);
        ObservableTimeline timeline = new ObservableTimeline(simulation, now, ONE_HOUR, TEN_DAYS_PER_SECOND,
                A_STEP_PER_FRAME);
        // The scene, the paths and the font need the OpenGL context of the window
        try (GlfwWindow window = GlfwWindow.open(TITLE, WIDTH, HEIGHT); GlTextures textures = GlTextures.create();
             GlDiscs scene = GlDiscs.create(textures);
             GlRings rings = GlRings.create();
             GlPaths paths = GlPaths.create(); GlGlows glows = GlGlows.create(); GlGlows sky = GlGlows.create();
             GlFont font = GlFont.create()) {
            GlText headUpDisplay = new GlText(font, Corner.TOP_LEFT);
            GlText controlsHelp = new GlText(font, Corner.BOTTOM_LEFT);
            GlText provenance = new GlText(font, Corner.BOTTOM_RIGHT);
            GlText focus = new GlText(font, Corner.TOP_RIGHT);
            GlLabels names = new GlLabels(font);
            ObservableCameraRig camera = new ObservableCameraRig(BARYCENTRE, FIRST_VIEW, LENS,
                    new Viewport(WIDTH, HEIGHT));
            BundledMaps maps = new BundledMaps(textures);
            BUNDLED_MAPS.forEach(maps::load);
            MapDeliveries deliveries = new MapDeliveries(textures);
            MapDownloads downloads = new MapDownloads(
                    new CachedMapFiles(new RetryingMapFiles(new HttpMapFiles(MAP_TIMEOUT), MAP_ATTEMPTS), MAP_CACHE),
                    deliveries, mapThreads());
            NASA_MAPS.forEach(downloads::request);
            BodiesPresenter bodies = new BodiesPresenter(scene, camera.camera(), STYLES, skins(), now);
            simulation.addObserver(bodies);
            timeline.addObserver(bodies.timeObserver());
            camera.addObserver(bodies.cameraObserver());
            RingsPresenter ringed = new RingsPresenter(rings, camera.camera(), STYLES, SolarSystem.rings(),
                    RING_COLOUR);
            simulation.addObserver(ringed);
            camera.addObserver(ringed.cameraObserver());
            LabelsPresenter labels = new LabelsPresenter(names, camera.camera(), STYLES, UNNAMED);
            simulation.addObserver(labels);
            camera.addObserver(labels.cameraObserver());
            TrajectoriesPresenter trajectories = new TrajectoriesPresenter(paths, camera.camera(), PATH_COLOURS);
            forecast.addObserver(trajectories);
            camera.addObserver(trajectories.cameraObserver());
            simulation.addObserver(forecast);
            // After the presenters of the bodies: they draw the new bodies before the camera follows them
            ObservableBodyFollower follower = new ObservableBodyFollower(camera);
            simulation.addObserver(follower);
            follower.addObserver(new FocusPresenter(focus));
            // What glows of the impacts, on the bodies that survived them, as time goes by
            Blasts blasts = new Blasts();
            simulation.addObserver(blasts.bodiesObserver());
            simulation.collisionNews().addObserver(blasts);
            timeline.addObserver(blasts.timeObserver());
            FlaresPresenter flares = new FlaresPresenter(glows, camera.camera());
            blasts.addObserver(flares);
            camera.addObserver(flares.cameraObserver());
            camera.addObserver(new SkyPresenter(sky, Stars.scattered(new Random(SKY_SEED), STARS)));
            // After the follower: it knows whether the body hit is the one the user watches
            simulation.collisionNews().addObserver(new SlowMotion(follower, timeline));
            new CameraControlsPresenter(window, camera);
            timeline.addObserver(new TimeFlowPresenter(headUpDisplay));
            // The last layer, so that a screenshot shows everything else
            GlScreenshot screenshot = new GlScreenshot();
            new KeyboardPresenter(window, timeline, follower, new Launcher(simulation, follower, camera),
                    new ScreenshotPresenter(screenshot, PICTURES, InstantSource.system(), ZoneId.systemDefault()));
            new ControlsHelpPresenter(controlsHelp);
            new ProvenancePresenter(provenance, initialState.provenance());
            simulation.notifyObservers();
            timeline.notifyObservers();
            follower.notifyObservers();
            camera.notifyObservers();
            // The maps that have arrived are handed over before anything is drawn
            Layers picture = new Layers(List.of(deliveries, sky, paths, scene, rings, glows, names, headUpDisplay,
                    focus, controlsHelp, provenance, screenshot));
            new MainLoop(window, new SystemClock(), timeline, picture).run();
        }
    }
}
