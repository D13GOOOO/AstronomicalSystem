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
 * The primary JavaFX entry point and orchestration layer for the astronomical simulation.
 * <p>
 * Acting as the Application Composition Root, this class provisions the 3D scene graph,
 * instantiates the core physics engine via the {@link UniverseFactory}, and bridges
 * the domain model with the visual rendering pipeline utilizing the Observer pattern.
 * </p>
 * <p>
 * <b>Architecture Note:</b> Uses a layered rendering approach. The 3D universe is rendered 
 * within a hardware-accelerated {@link SubScene}, while 2D UI overlays (HUD, Menus) are 
 * stacked on top using a {@link StackPane}.
 * </p>
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
     * Bootstraps the application, assembling the MVC triad and starting the physics loop.
     *
     * @param primaryStage the primary JavaFX window provided by the runtime.
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
     * Explicitly overrides the pause state of the simulation.
     *
     * @param paused true to halt the physics engine, false to resume.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Toggles the current pause state of the simulation.
     */
    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    /**
     * Toggles the visibility of the central pause menu, automatically pausing/resuming physics.
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
     * Toggles the slide-out animation of the entity Creator Panel.
     */
    public void toggleCreatorPanel() {
        this.creatorPanel.toggle();
    }

    /**
     * Displays the telemetry readout for a specific celestial body and pauses the simulation.
     *
     * @param body the entity to inspect.
     */
    public void showInfoPanel(CelestialBody body) {
        this.infoPanel.showPlanet(body);
        this.isPaused = true;
    }

    /**
     * Hides the telemetry readout and resumes the simulation.
     */
    public void hideInfoPanel() {
        this.infoPanel.hide();
        this.isPaused = false;
    }

    /**
     * Serializes the full interactive session with visual UI feedback.
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
     * Deserializes the full interactive session with visual UI feedback.
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
     * Instantiates and registers a new dynamic planetary body via the Creator Panel.
     * Calculates perfect orbital velocity dynamically and allows scalar modifications.
     *
     * @param massMultiplier     the mass coefficient relative to Earth.
     * @param distance           the spawn distance from the system anchor.
     * @param velocityMultiplier the scalar to alter the perfect circular orbit.
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
     * Generates an immense background sphere textured with a starfield to simulate deep space.
     *
     * @param root3D the root group of the 3D subscene.
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
     * Attempts to resolve the primary or fallback skybox texture streams.
     *
     * @return the resolved {@link InputStream}, or null if neither exists.
     */
    private InputStream openSkyboxTexture() {
        InputStream milkyWay = getClass().getResourceAsStream("/milky_way.jpg");
        if (milkyWay != null) {
            return milkyWay;
        }
        return getClass().getResourceAsStream("/stars.jpg");
    }

    /**
     * Captures the volatile current state of the application for serialization.
     *
     * @return a populated {@link SessionState} snapshot.
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
     * Reconstructs the application state from a modern session snapshot.
     *
     * @param sessionState the deserialized data transfer object.
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
     * Reconstructs the application state gracefully when loading a legacy, physics-only payload.
     *
     * @param loadedSystem the deserialized physics engine.
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
     * Semantic wrapper for the 3D world group, acting as the pivot origin for camera orbits.
     */
    private static class SmartGroup extends Group { }

    /**
     * Application entry point delegated by {@link Launcher}.
     *
     * @param args command-line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}