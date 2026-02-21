package astronomicalsystem;

import astronomicalsystem.controller.InputHandler;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.AstronomicalSystem;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.view.InfoPanel;
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
import javafx.scene.effect.ColorAdjust;

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

    private AstronomicalSystem system;
    private InfoPanel infoPanel;
    private boolean isPaused = false;

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

        SimulationRenderer renderer = new SimulationRenderer(world);
        this.system.addObserver(renderer);

        SubScene subScene = new SubScene(root3D, WIDTH, HEIGHT, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateZ(-3000);
        camera.setNearClip(1);
        camera.setFarClip(50000.0);
        subScene.setCamera(camera);

        this.infoPanel = new InfoPanel();
        StackPane rootLayout = new StackPane();
        rootLayout.getChildren().addAll(subScene, infoPanel);
        StackPane.setAlignment(infoPanel, Pos.TOP_LEFT);
        this.infoPanel.setMouseTransparent(true);

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
     * Provisions the static background environment mapping (Skybox).
     * <p>
     * Gracefully falls back to a solid background color if texture resources are unavailable.
     * </p>
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
            System.err.println("Skybox texture failed to load: " + e.getMessage());
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