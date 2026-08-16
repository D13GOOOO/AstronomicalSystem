package astronomicalsystem;

import java.io.InputStream;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.controller.InputHandler;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import astronomicalsystem.model.Interactions;
import astronomicalsystem.model.Planet;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.Star;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
import astronomicalsystem.session.CameraState;
import astronomicalsystem.session.SessionPersistenceService;
import astronomicalsystem.session.SessionState;
import astronomicalsystem.view.CreatorPanel;
import astronomicalsystem.view.InfoPanel;
import astronomicalsystem.view.PauseMenu;
import astronomicalsystem.view.SimulationRenderer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

/**
 * The master control center and graphical foundation of the entire astronomical simulation.
 * 
 * As the main JavaFX application class, this is where the mathematical physics engine and the 
 * visual user interface finally meet. This class is responsible for initially creating the universe, 
 * building the three dimensional space, setting up the lighting and cameras, and starting the 
 * continuous loop that makes time flow forward.
 * 
 * To ensure everything looks beautiful and remains interactive, it uses a stacked visual architecture. 
 * The deep space environment, planets, and orbits are drawn on a specialized, hardware accelerated 
 * background layer. On top of that, it stacks a transparent foreground layer that holds all the 
 * standard two dimensional menus, sliders, and text panels, allowing the user to click buttons 
 * without accidentally dragging the 3D camera.
 */
public class SolarSystem3D extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();

    private AstronomicalSystem system;
    private InfoPanel infoPanel;
    private PauseMenu pauseMenu;
    private CreatorPanel creatorPanel;
    private SimulationRenderer renderer;
    private SimulationPerformanceMonitor performanceMonitor;
    private PerspectiveCamera camera;
    private final SessionPersistenceService sessionPersistenceService = new SessionPersistenceService();
    
    private boolean isPaused = false;
    private int customPlanetCount = 1;

    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    /**
     * The primary assembly line where the entire application is constructed right before appearing 
     * on the user's screen.
     * 
     * It begins by asking the factory to construct the mathematical solar system. It then builds the 
     * 3D environment, injecting the stars, ambient lighting, and the brilliant light emitting from the 
     * sun. Next, it wires up the telemetry panels, the pause menu, and the planet creation drawer. 
     * Finally, it creates the massive, unending heartbeat of the application: the animation timer. 
     * This timer fires constantly, telling the physics engine to calculate the next frame and telling 
     * the performance monitor to record how long it took.
     *
     * @param primaryStage the main window container provided by the operating system to hold the application.
     */
    @Override
    public void start(Stage primaryStage) {
        this.system = UniverseFactory.createSolarSystem();

        SmartGroup world = new SmartGroup();
        world.getTransforms().addAll(rotateX, rotateY);

        Group root3D = new Group();
        root3D.getChildren().add(world);
        createSkybox(root3D);

        this.performanceMonitor = new SimulationPerformanceMonitor(
            SETTINGS.performance().enabled(),
            SETTINGS.performance().reportEveryFrames()
        );
        this.renderer = new SimulationRenderer(world, this.performanceMonitor);
        this.system.addObserver(this.renderer);

        SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        this.camera = new PerspectiveCamera(true);
        this.camera.setTranslateZ(SETTINGS.camera().initialTranslateZ());
        this.camera.setNearClip(SETTINGS.camera().nearClip());
        this.camera.setFarClip(SETTINGS.camera().farClip());
        subScene.setCamera(this.camera);

        this.infoPanel = new InfoPanel();
        this.infoPanel.setMouseTransparent(true);

        this.pauseMenu = new PauseMenu(
                this::togglePauseMenu,
                this::handleQuickSave,
                this::handleQuickLoad,
                () -> System.exit(0)
        );

        this.creatorPanel = new CreatorPanel(this::spawnCustomPlanet);

        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().addAll(subScene, infoPanel, pauseMenu, creatorPanel);
        StackPane.setAlignment(infoPanel, Pos.TOP_LEFT);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);
        StackPane.setAlignment(creatorPanel, Pos.CENTER_RIGHT);

        subScene.widthProperty().bind(rootLayout.widthProperty());
        subScene.heightProperty().bind(rootLayout.heightProperty());

        Scene mainScene = new Scene(rootLayout, WIDTH, HEIGHT);

        new InputHandler(this, mainScene, this.camera, rotateX, rotateY);

        PointLight sunLight = new PointLight(Color.WHITE);
        world.getChildren().add(sunLight);

        AmbientLight ambient = new AmbientLight(Color.rgb(120, 120, 120));
        root3D.getChildren().add(ambient);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    try {
                        long frameStart = System.nanoTime();
                        system.simulate(1);
                        performanceMonitor.recordFrame(System.nanoTime() - frameStart);
                    } catch (Throwable t) {
                        System.err.println("Physics Engine Guard caught an error: " + t.getMessage());
                    }
                }
            }
        };
        timer.start();

        primaryStage.setTitle("Interactive Solar System (Physics Engine V2)");
        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        rootLayout.requestFocus();
    }

    /**
     * Instantly forces the physics engine to either freeze entirely or resume running. When frozen, 
     * gravity stops calculating and the planets hang motionless in space, though the camera can still 
     * be moved freely.
     *
     * @param paused true to completely halt the physics engine, false to resume the flow of time.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Simply flips the current state of time. If the simulation is running, it freezes it. If it is 
     * already frozen, it resumes running.
     */
    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    /**
     * Controls the visibility of the central pause menu while automatically managing the flow of time. 
     * If the menu is currently hidden, this method reveals the menu and instantly freezes the physics 
     * engine. If the menu is already visible, it hides the menu and resumes the simulation.
     */
    public void togglePauseMenu() {
        if (this.pauseMenu.isVisible()) {
            this.pauseMenu.setVisible(false);
            this.pauseMenu.clearStatus();
            this.isPaused = false;
        } else {
            hideInfoPanel();
            this.pauseMenu.setVisible(true);
            this.isPaused = true;
        }
    }

    /**
     * Commands the dynamic creator panel on the right side of the screen to slide in or out of view.
     */
    public void toggleCreatorPanel() {
        this.creatorPanel.toggle();
    }

    /**
     * Reveals the informational telemetry panel in the top left corner, populating it with data from 
     * the specific celestial body the user just clicked on. It automatically pauses the simulation 
     * so the user can easily read the data without the planet flying away.
     *
     * @param body the celestial entity the user wishes to inspect.
     */
    public void showInfoPanel(CelestialBody body) {
        this.infoPanel.showPlanet(body);
        this.isPaused = true;
    }

    /**
     * Hides the informational telemetry panel and instantly resumes the flow of time.
     */
    public void hideInfoPanel() {
        this.infoPanel.hide();
        this.isPaused = false;
    }

    /**
     * Gathers all the data about the current state of the universe and asks the persistence service 
     * to save it to a file. It updates the pause menu text to inform the user whether the save was 
     * successful or if an error occurred.
     */
    private void handleQuickSave() {
        try {
            this.sessionPersistenceService.save("quicksave.dat", captureSessionState());
            this.pauseMenu.showStatus("Game saved successfully!", Color.LIGHTGREEN);
        } catch (Exception e) {
            this.pauseMenu.showStatus("Save failed: " + e.getMessage(), Color.SALMON);
        }
    }

    /**
     * Asks the persistence service to read a saved file from the hard drive and attempts to reconstruct 
     * the universe from that data. It intelligently checks if the loaded file is a modern save or an 
     * outdated legacy save, routes the data to the correct reconstruction method, and updates the pause 
     * menu text to confirm success or failure.
     */
    private void handleQuickLoad() {
        try {
            SessionState sessionState = this.sessionPersistenceService.load("quicksave.dat");

            if (sessionState.legacyPhysicsOnly()) {
                restoreLegacySession(sessionState.system());
                this.pauseMenu.showStatus("Legacy save loaded successfully!", Color.LIGHTGREEN);
            } else {
                restoreSessionState(sessionState);
                this.pauseMenu.showStatus("Game loaded successfully!", Color.LIGHTGREEN);
            }
        } catch (Exception e) {
            this.pauseMenu.showStatus("No save found or load failed!", Color.SALMON);
        }
    }

    /**
     * Handles the complex physics and mathematics required to create a brand new planet exactly when 
     * the user clicks the launch button in the creator panel. 
     * 
     * Instead of just dropping a planet randomly, this method actively calculates the perfect velocity 
     * required for the new planet to establish a stable, circular orbit around the central sun based on 
     * its exact starting distance. It then applies the user's velocity multiplier, allowing them to 
     * either launch it perfectly or purposely create chaotic elliptical orbits.
     *
     * @param massMultiplier     how much heavier or lighter the new planet should be compared to Earth.
     * @param distance           how far away from the center of the universe the planet should spawn.
     * @param velocityMultiplier a modifier allowing the user to increase or decrease the perfect orbital speed.
     */
    private void spawnCustomPlanet(double massMultiplier, double distance, double velocityMultiplier) {
        String name = "Custom Planet X-" + (customPlanetCount++);
        double mass = SETTINGS.creator().baseEarthMasses() * massMultiplier;

        double calculatedRadius = Math.cbrt(massMultiplier);
        if (calculatedRadius < 0.5) {
            calculatedRadius = 0.5;
        }

        double angleOffset = (customPlanetCount * 0.37) % (2 * Math.PI);
        double actualDistance = distance + (customPlanetCount % 5) * 15.0;

        double posX = actualDistance * Math.cos(angleOffset);
        double posZ = actualDistance * Math.sin(angleOffset);

        Planet customPlanet = new Planet(name, mass, calculatedRadius, new Point3D(posX, 0, posZ));

        double sunMass = this.system.getBodies().stream()
                .filter(b -> b instanceof Star)
                .mapToDouble(CelestialBody::getMass)
                .findFirst()
                .orElse(100000.0);

        double perfectVelocity = Math.sqrt((Interactions.G * sunMass) / actualDistance);
        double actualVelocity = perfectVelocity * velocityMultiplier;

        customPlanet.setVelocity(new Point3D(-actualVelocity * Math.sin(angleOffset), 0, actualVelocity * Math.cos(angleOffset)));

        customPlanet.setMetadata(new BodyMetadata(
                String.format("%.1f Earth Masses", massMultiplier),
                "Unknown", "Dynamic", "Unknown", "Unknown"
        ));

        this.system.addBody(customPlanet);
    }

    /**
     * Creates an unimaginably massive, hollow sphere that completely surrounds the entire simulated 
     * universe. By painting the inside of this sphere with a high resolution image of the Milky Way 
     * galaxy, it creates a perfect optical illusion of deep, endless space surrounding the planets.
     *
     * @param root3D the primary graphical group where the massive sky sphere will be added.
     */
    private void createSkybox(Group root3D) {
        Sphere skybox = new Sphere(25000);
        skybox.setId("skybox");
        skybox.setCullFace(CullFace.NONE);

        PhongMaterial mat = new PhongMaterial();
        try (InputStream stream = openSkyboxTexture()) {
            if (stream != null) {
                Image stars = new Image(stream, 0, 0, true, true);
                mat.setDiffuseMap(stars);
            } else {
                mat.setDiffuseColor(Color.web("#050510"));
            }
        } catch (Exception e) {
            mat.setDiffuseColor(Color.web("#050510"));
        }

        skybox.setMaterial(mat);
        root3D.getChildren().add(skybox);
    }

    /**
     * Attempts to locate and open the image files used to create the background space illusion. 
     * It looks for the high quality Milky Way texture first, and if that file is missing, it 
     * safely falls back to a generic starfield texture.
     *
     * @return a data stream containing the image file, or null if neither image could be found.
     */
    private InputStream openSkyboxTexture() {
        InputStream milkyWay = getClass().getResourceAsStream("/milky_way.jpg");
        if (milkyWay != null) {
            return milkyWay;
        }
        return getClass().getResourceAsStream("/stars.jpg");
    }

    /**
     * Gathers all the volatile, ever changing data from the running application—including the exact 
     * physics engine state, whether the game is paused, and exactly how the user has angled the 
     * camera—and packs it all into a single, immutable record ready to be saved to the hard drive.
     *
     * @return a perfectly sealed snapshot of the exact current moment in the simulation.
     */
    private SessionState captureSessionState() {
        return SessionState.current(
                this.system,
                this.isPaused,
                this.customPlanetCount,
                new CameraState(
                        this.camera.getTranslateX(),
                        this.camera.getTranslateY(),
                        this.camera.getTranslateZ(),
                        this.rotateX.getAngle(),
                        this.rotateY.getAngle()
                )
        );
    }

    /**
     * Carefully unpacks a modern session snapshot that was loaded from the hard drive and uses that 
     * data to completely overwrite the current state of the application. It replaces the entire physics 
     * engine, moves the camera back to the exact saved angle, and resets the interface menus.
     *
     * @param sessionState the sealed data package containing the loaded universe.
     */
    private void restoreSessionState(SessionState sessionState) {
        this.system = sessionState.system();
        this.system.addObserver(this.renderer);
        this.isPaused = sessionState.paused();
        this.customPlanetCount = sessionState.customPlanetCount();

        CameraState cameraState = sessionState.cameraState();
        this.camera.setTranslateX(cameraState.translateX());
        this.camera.setTranslateY(cameraState.translateY());
        this.camera.setTranslateZ(cameraState.translateZ());
        this.rotateX.setAngle(cameraState.rotateX());
        this.rotateY.setAngle(cameraState.rotateY());

        this.pauseMenu.setVisible(this.isPaused);
        this.infoPanel.hide();
        this.pauseMenu.clearStatus();
    }

    /**
     * Safely reconstructs the application state when loading an old, outdated save file that only 
     * contained planetary physics data. Because the old file has no camera or menu data, this method 
     * replaces the physics engine but manually applies safe, default settings to the user interface 
     * to prevent the modern application from breaking.
     *
     * @param loadedSystem the pure physics engine extracted from the old save file.
     */
    private void restoreLegacySession(AstronomicalSystem loadedSystem) {
        this.system = loadedSystem;
        this.system.addObserver(this.renderer);
        this.isPaused = false;
        this.customPlanetCount = 1;
        this.infoPanel.hide();
        this.pauseMenu.setVisible(false);
        this.pauseMenu.clearStatus();
    }

    /**
     * A specialized grouping container used to hold all the planets and the sun in the very center 
     * of the universe. This allows the camera mechanics to easily rotate the entire universe around 
     * a central pivot point.
     */
    private static class SmartGroup extends Group { }

    /**
     * The internal launch method required by the JavaFX framework. When the main launcher delegates 
     * control to this class, this method triggers the internal engine that eventually calls the main 
     * start method.
     *
     * @param args command-line arguments passed through from the launcher.
     */
    public static void main(String[] args) {
        launch(args);
    }
}