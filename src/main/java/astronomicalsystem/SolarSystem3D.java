package astronomicalsystem;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.controller.InputHandler;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import astronomicalsystem.model.Planet;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.session.CameraState;
import astronomicalsystem.session.SessionPersistenceService;
import astronomicalsystem.session.SessionState;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
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

import java.io.InputStream;

/**
 * The primary JavaFX entry point and orchestration layer for the astronomical simulation.
 * <p>
 * Acting as the Application Composition Root, this class provisions the 3D scene graph,
 * instantiates the core physics engine via the {@link UniverseFactory}, and bridges
 * the domain model with the visual rendering pipeline utilizing the Observer pattern.
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

    @Override
    public void start(Stage primaryStage) {
        this.system = UniverseFactory.createSolarSystem();

        SmartGroup world = new SmartGroup();
        world.getTransforms().addAll(rotateX, rotateY);

        Group root3D = new Group();
        root3D.getChildren().add(world);

        createSkybox(root3D);

        this.renderer = new SimulationRenderer(world, this.performanceMonitor);
        this.system.addObserver(this.renderer);

        SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        this.camera = new PerspectiveCamera(true);
        this.camera.setTranslateZ(SETTINGS.camera().initialTranslateZ());
        this.camera.setNearClip(SETTINGS.camera().nearClip());
        this.camera.setFarClip(SETTINGS.camera().farClip());
        subScene.setCamera(this.camera);

        this.performanceMonitor = new SimulationPerformanceMonitor(
            SETTINGS.performance().enabled(),
            SETTINGS.performance().reportEveryFrames()
        );

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
                    long frameStart = System.nanoTime();
                    system.simulate(1);
                    performanceMonitor.recordFrame(System.nanoTime() - frameStart);
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

    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

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

    public void toggleCreatorPanel() {
        this.creatorPanel.toggle();
    }

    public void showInfoPanel(CelestialBody body) {
        this.infoPanel.showPlanet(body);
        this.isPaused = true;
    }

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

    private void spawnCustomPlanet(double massMultiplier, double distance, double velocity) {
        String name = "Custom Planet X-" + (customPlanetCount++);
        double mass = SETTINGS.creator().baseEarthMasses() * massMultiplier;

        Planet customPlanet = new Planet(name, mass, 1.0, new Point3D(distance, 0, 0));
        customPlanet.setVelocity(new Point3D(0, 0, velocity));

        customPlanet.setMetadata(new BodyMetadata(
                String.format("%.1f Earth Masses", massMultiplier),
                "Unknown", "Dynamic", "Unknown", "Unknown"
        ));

        this.system.addBody(customPlanet);
    }

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

    private InputStream openSkyboxTexture() {
        InputStream milkyWay = getClass().getResourceAsStream("/milky_way.jpg");
        if (milkyWay != null) {
            return milkyWay;
        }
        return getClass().getResourceAsStream("/stars.jpg");
    }

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

    private void restoreLegacySession(AstronomicalSystem loadedSystem) {
        this.system = loadedSystem;
        this.system.addObserver(this.renderer);
        this.isPaused = false;
        this.customPlanetCount = 1;
        this.infoPanel.hide();
        this.pauseMenu.setVisible(false);
        this.pauseMenu.clearStatus();
    }

    private static class SmartGroup extends Group { }

    public static void main(String[] args) {
        launch(args);
    }
}
