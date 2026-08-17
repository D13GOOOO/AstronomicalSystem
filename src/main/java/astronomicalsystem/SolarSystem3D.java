package astronomicalsystem;

import java.io.InputStream;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.controller.InputHandler;
import astronomicalsystem.engine.SimulationEngine;
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
import astronomicalsystem.view.TimeControlPanel;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.PointLight;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
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
 * The user interface has been heavily engineered to mimic a high-tech aerospace Mission Control 
 * dashboard. A transparent, non-blocking Heads-Up Display (HUD) overlay is stacked securely on top 
 * of the hardware-accelerated 3D space scene. This unified layout intelligently groups telemetry 
 * data and creation controls into a right-aligned sidebar, while time manipulation controls are 
 * anchored to the bottom, ensuring maximum visibility of the cosmos while keeping vital scientific 
 * instruments instantly accessible.
 */
public class SolarSystem3D extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;
    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();

    private AstronomicalSystem system;
    private SimulationEngine physicsEngine;
    
    private InfoPanel infoPanel;
    private PauseMenu pauseMenu;
    private CreatorPanel creatorPanel;
    private TimeControlPanel timeControlPanel;
    private SimulationRenderer renderer;
    private SimulationPerformanceMonitor performanceMonitor;
    private PerspectiveCamera camera;
    private final SessionPersistenceService sessionPersistenceService = new SessionPersistenceService();
    
    private int customPlanetCount = 1;

    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    /**
     * The primary assembly line where the entire application is constructed right before appearing 
     * on the user's screen.
     * 
     * It initiates the universe generation, constructs the three dimensional rendering sub-scene, 
     * and layers the modern aerospace HUD over the screen. It purposefully separates the graphical 
     * rendering pipeline from the heavy mathematical physics calculations by deploying the 
     * SimulationEngine onto a completely independent background thread.
     *
     * @param primaryStage the main window container provided by the operating system to hold the application.
     */
    @Override
    public void start(Stage primaryStage) {
        this.system = UniverseFactory.createSolarSystem();
        this.physicsEngine = new SimulationEngine(this.system);

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

        this.infoPanel = new InfoPanel(this::releaseFocus);
        this.creatorPanel = new CreatorPanel(this::spawnCustomPlanet);
        this.timeControlPanel = new TimeControlPanel(factor -> this.physicsEngine.setTimeWarp(factor));

        this.pauseMenu = new PauseMenu(
                this::togglePauseMenu,
                this::handleQuickSave,
                this::handleQuickLoad,
                () -> {
                    this.physicsEngine.stopEngine();
                    System.exit(0);
                }
        );

        BorderPane hudOverlay = new BorderPane();
        hudOverlay.setPickOnBounds(false); 

        VBox rightSidebar = new VBox(20);
        rightSidebar.setPickOnBounds(false);
        rightSidebar.setAlignment(Pos.TOP_RIGHT);
        rightSidebar.setPadding(new Insets(30, 30, 30, 0));
        rightSidebar.getChildren().addAll(this.infoPanel, this.creatorPanel);
        hudOverlay.setRight(rightSidebar);

        BorderPane.setAlignment(this.timeControlPanel, Pos.BOTTOM_CENTER);
        BorderPane.setMargin(this.timeControlPanel, new Insets(0, 0, 30, 0));
        hudOverlay.setBottom(this.timeControlPanel);

        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().addAll(subScene, hudOverlay, pauseMenu);
        StackPane.setAlignment(pauseMenu, Pos.CENTER);

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
            }
        };
        timer.start();

        primaryStage.setTitle("Interactive Solar System (Physics Engine V2)");
        primaryStage.setScene(mainScene);
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        Thread engineThread = new Thread(this.physicsEngine, "Physics-Engine-Thread");
        engineThread.setDaemon(true);
        engineThread.start();

        rootLayout.requestFocus();
    }

    /**
     * Instantly forces the independent physics engine to either freeze entirely or resume running. 
     * When frozen, gravity stops calculating and the planets hang motionless in space, though the 
     * camera can still be moved freely to inspect the frozen scene.
     *
     * @param paused true to completely halt the physics engine, false to resume the flow of time.
     */
    public void setPaused(boolean paused) {
        this.physicsEngine.setPaused(paused);
    }

    /**
     * Simply flips the current state of time. If the simulation is running, it freezes it. If it is 
     * already frozen, it resumes running.
     */
    public void togglePause() {
        this.physicsEngine.setPaused(!this.physicsEngine.isPaused());
    }

    /**
     * Controls the visibility of the central override menu while automatically managing the flow of time. 
     * If the menu is currently hidden, this method reveals the menu and instantly freezes the physics 
     * engine. If the menu is already visible, it hides the menu and resumes the simulation.
     */
    public void togglePauseMenu() {
        if (this.pauseMenu.isVisible()) {
            this.pauseMenu.setVisible(false);
            this.pauseMenu.clearStatus();
            this.physicsEngine.setPaused(false);
        } else {
            hideInfoPanel();
            this.pauseMenu.setVisible(true);
            this.physicsEngine.setPaused(true);
        }
    }

    /**
     * Commands the dynamic planet generation panel within the right sidebar to switch its visibility state.
     */
    public void toggleCreatorPanel() {
        this.creatorPanel.toggle();
    }

    /**
     * Executes a cinematic camera maneuver that teleports the user's viewpoint directly to the 
     * selected celestial body. It calculates the exact required zoom to fill the screen appropriately, 
     * halts the simulation time for observation, and activates the scientific HUD overlay.
     *
     * @param body the celestial entity the user wishes to inspect.
     */
    public void showInfoPanel(CelestialBody body) {
        this.physicsEngine.setPaused(true);
        this.infoPanel.showPlanet(body);
        this.renderer.setTargetBody(body);
        
        double renderScale = SETTINGS.render().positionScale();
        double physicalX = body.getPosition().x() * renderScale;
        double physicalY = body.getPosition().y() * renderScale;
        double physicalZ = body.getPosition().z() * renderScale;
        
        double visualRadius = this.renderer.getTargetVisualRadius(body);
        double zoomOffset = visualRadius * -8.5; 
        
        this.camera.setTranslateX(physicalX);
        this.camera.setTranslateY(physicalY);
        this.camera.setTranslateZ(physicalZ + zoomOffset);
        
        this.rotateX.setAngle(0);
        this.rotateY.setAngle(0);
    }

    /**
     * Executes a tactical release maneuver, instantly detaching the camera from the currently 
     * focused celestial body. It restores the camera to its original panoramic viewpoint, hides 
     * all telemetry overlays, and resumes the flow of time in the universe.
     */
    public void releaseFocus() {
        hideInfoPanel();
        
        this.camera.setTranslateX(0);
        this.camera.setTranslateY(0);
        this.camera.setTranslateZ(SETTINGS.camera().initialTranslateZ());
        this.rotateX.setAngle(20);
        this.rotateY.setAngle(0);
    }

    /**
     * Hides the informational telemetry panel, immediately deactivates the scientific HUD 3D vectors, 
     * and instantly resumes the flow of time in the universe.
     */
    public void hideInfoPanel() {
        this.infoPanel.hide();
        this.renderer.setTargetBody(null);
        this.physicsEngine.setPaused(false);
    }

    /**
     * Gathers all the data about the current state of the universe and asks the persistence service 
     * to save it to a file. It updates the central override menu to confirm whether the save was 
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
     * the universe from that data. It intelligently routes the data to the correct legacy or modern 
     * reconstruction method based on the file version format.
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
     * Executes the complex mathematics required to inject a brand new planet exactly when the user 
     * triggers the launch command from the creation panel. It dynamically calculates the perfect 
     * velocity required for the new planet to establish a stable orbit around the primary star, 
     * applying any user-defined multipliers for chaotic effects.
     *
     * @param massMultiplier     how much heavier or lighter the new planet should be compared to Earth.
     * @param distance           how far away from the center of the universe the planet should spawn.
     * @param velocityMultiplier a modifier allowing the user to purposely disrupt the perfect orbital speed.
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
     * Creates an unimaginably massive, hollow sphere surrounding the simulated universe. By painting 
     * the inside of this sphere with a high resolution galactic texture, it creates a perfect optical 
     * illusion of deep space.
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
     * Attempts to locate the optimal image files required to generate the deep space background illusion, 
     * falling back to lower quality defaults if the primary texture is missing.
     *
     * @return a data stream containing the image file, or null if no resources could be found.
     */
    private InputStream openSkyboxTexture() {
        InputStream milkyWay = getClass().getResourceAsStream("/milky_way.jpg");
        if (milkyWay != null) {
            return milkyWay;
        }
        return getClass().getResourceAsStream("/stars.jpg");
    }

    /**
     * Gathers the exact physics state, the current time warp status, and the precise camera angles, 
     * sealing them into an immutable snapshot ready for disk persistence.
     *
     * @return a perfectly sealed snapshot of the exact current moment in the simulation.
     */
    private SessionState captureSessionState() {
        return SessionState.current(
                this.system,
                this.physicsEngine.isPaused(),
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
     * Safely disposes of the running physics engine and overwrites the entire application state with 
     * data extracted from a verified modern save file.
     *
     * @param sessionState the sealed data package containing the loaded universe and camera positions.
     */
    private void restoreSessionState(SessionState sessionState) {
        if (this.physicsEngine != null) {
            this.physicsEngine.stopEngine();
        }
        
        this.system = sessionState.system();
        this.system.addObserver(this.renderer);
        
        this.physicsEngine = new SimulationEngine(this.system);
        this.physicsEngine.setPaused(sessionState.paused());
        
        Thread newEngineThread = new Thread(this.physicsEngine, "Physics-Engine-Thread");
        newEngineThread.setDaemon(true);
        newEngineThread.start();
        
        this.customPlanetCount = sessionState.customPlanetCount();

        CameraState cameraState = sessionState.cameraState();
        this.camera.setTranslateX(cameraState.translateX());
        this.camera.setTranslateY(cameraState.translateY());
        this.camera.setTranslateZ(cameraState.translateZ());
        this.rotateX.setAngle(cameraState.rotateX());
        this.rotateY.setAngle(cameraState.rotateY());

        this.pauseMenu.setVisible(this.physicsEngine.isPaused());
        this.infoPanel.hide();
        this.renderer.setTargetBody(null);
        this.pauseMenu.clearStatus();
    }

    /**
     * Reconstructs the application environment from an outdated save file. It replaces the core 
     * physical system but manually enforces safe default states for modern interface components that 
     * did not exist when the legacy file was created.
     *
     * @param loadedSystem the raw mathematical simulation engine extracted from the old save file.
     */
    private void restoreLegacySession(AstronomicalSystem loadedSystem) {
        if (this.physicsEngine != null) {
            this.physicsEngine.stopEngine();
        }
        
        this.system = loadedSystem;
        this.system.addObserver(this.renderer);
        
        this.physicsEngine = new SimulationEngine(this.system);
        this.physicsEngine.setPaused(false);
        
        Thread newEngineThread = new Thread(this.physicsEngine, "Physics-Engine-Thread");
        newEngineThread.setDaemon(true);
        newEngineThread.start();
        
        this.customPlanetCount = 1;
        this.infoPanel.hide();
        this.renderer.setTargetBody(null);
        this.pauseMenu.setVisible(false);
        this.pauseMenu.clearStatus();
    }

    /**
     * A specialized structural node used to group the core astronomical components within the 3D space, 
     * providing a solid anchor point for orbital camera rotations.
     */
    private static class SmartGroup extends Group { }

    /**
     * The internal launch method required by the JavaFX application lifecycle.
     *
     * @param args command-line arguments passed through from the launcher.
     */
    public static void main(String[] args) {
        launch(args);
    }
}