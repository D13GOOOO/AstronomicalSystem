package astronomicalsystem;

import astronomicalsystem.controller.InputHandler;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import astronomicalsystem.model.Planet;
import astronomicalsystem.model.Point3D;
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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

    private AstronomicalSystem system;
    private InfoPanel infoPanel;
    private PauseMenu pauseMenu;
    private CreatorPanel creatorPanel;
    private SimulationRenderer renderer;
    private boolean isPaused = false;
    private int customPlanetCount = 1;

    private final Rotate rotateX = new Rotate(20, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);

    /**
     * Bootstraps the JavaFX application lifecycle.
     *
     * @param primaryStage the primary window hosting the application scene.
     */
    @Override
    public void start(Stage primaryStage) {
        this.system = UniverseFactory.createSolarSystem();

        SmartGroup world = new SmartGroup();
        world.getTransforms().addAll(rotateX, rotateY);

        Group root3D = new Group();
        root3D.getChildren().add(world);

        createSkybox(root3D);

        this.renderer = new SimulationRenderer(world);
        this.system.addObserver(this.renderer);

        SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-3000);
        camera.setNearClip(1);
        camera.setFarClip(50000.0);
        subScene.setCamera(camera);

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

        new InputHandler(this, mainScene, camera, rotateX, rotateY);

        PointLight sunLight = new PointLight(Color.WHITE);
        world.getChildren().add(sunLight);

        AmbientLight ambient = new AmbientLight(Color.rgb(120, 120, 120));
        root3D.getChildren().add(ambient);

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isPaused) {
                    system.simulate(1);
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
     * Modifies the runtime state of the integration loop.
     *
     * @param paused {@code true} to halt physics processing, {@code false} to resume.
     */
    public void setPaused(boolean paused) {
        this.isPaused = paused;
    }

    /**
     * Inverts the current runtime state of the integration loop.
     */
    public void togglePause() {
        this.isPaused = !this.isPaused;
    }

    /**
     * Intercepts the pause command to render the overlay UI.
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
     * Triggers the sliding animation for the creation HUD.
     */
    public void toggleCreatorPanel() {
        this.creatorPanel.toggle();
    }

    /**
     * Halts the simulation and exposes the metadata HUD for a targeted entity.
     *
     * @param body the domain entity subject to inspection.
     */
    public void showInfoPanel(CelestialBody body) {
        this.infoPanel.showPlanet(body);
        this.isPaused = true;
    }

    /**
     * Dismisses the metadata HUD and resumes the physical simulation.
     */
    public void hideInfoPanel() {
        this.infoPanel.hide();
        this.isPaused = false;
    }

    /**
     * Serializes the simulation state with visual UI feedback.
     */
    private void handleQuickSave() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("quicksave.dat"))) {
            out.writeObject(this.system);
            this.pauseMenu.showStatus("Game saved successfully!", Color.LIGHTGREEN);
        } catch (Exception e) {
            this.pauseMenu.showStatus("Save failed: " + e.getMessage(), Color.SALMON);
        }
    }

    /**
     * Deserializes the simulation state with visual UI feedback.
     */
    private void handleQuickLoad() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("quicksave.dat"))) {
            AstronomicalSystem loadedSystem = (AstronomicalSystem) in.readObject();
            if (loadedSystem != null) {
                this.system = loadedSystem;
                this.system.addObserver(this.renderer);
                this.pauseMenu.showStatus("Game loaded successfully!", Color.LIGHTGREEN);
            }
        } catch (Exception e) {
            this.pauseMenu.showStatus("No save found or load failed!", Color.SALMON);
        }
    }

    /**
     * Injects a dynamically instantiated celestial body into the active physics engine.
     *
     * @param massMultiplier the mass coefficient relative to Earth simulation mass.
     * @param distance       the initial spatial offset from the origin.
     * @param velocity       the initial tangential orbital velocity.
     */
    private void spawnCustomPlanet(double massMultiplier, double distance, double velocity) {
        String name = "Custom Planet X-" + (customPlanetCount++);
        double mass = 50.0 * massMultiplier;

        Planet customPlanet = new Planet(name, mass, 1.0, new Point3D(distance, 0, 0));
        customPlanet.setVelocity(new Point3D(0, 0, velocity));

        customPlanet.setMetadata(new BodyMetadata(
                String.format("%.1f Earth Masses", massMultiplier),
                "Unknown", "Dynamic", "Unknown", "Unknown"
        ));

        this.system.addBody(customPlanet);
    }

    /**
     * Provisions the static background environment mapping (Skybox).
     *
     * @param root3D the root structural node of the scene graph.
     */
    private void createSkybox(Group root3D) {
        Sphere skybox = new Sphere(25000);
        skybox.setId("skybox");
        skybox.setCullFace(CullFace.NONE);

        PhongMaterial mat = new PhongMaterial();
        try {
            InputStream stream = getClass().getResourceAsStream("/milky_way.jpg");
            if (stream == null) {
                stream = getClass().getResourceAsStream("/stars.jpg");
            }

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
     * A lightweight, internally-scoped extension of {@link Group} utilized to
     * semantically isolate the transformable world space from the static camera/skybox space.
     */
    private static class SmartGroup extends Group { }

    /**
     * The runtime entry point invoked by the JVM.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}