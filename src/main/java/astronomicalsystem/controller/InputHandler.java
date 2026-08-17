package astronomicalsystem.controller;

import java.util.Objects;

import astronomicalsystem.SolarSystem3D;
import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.model.CelestialBody;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;

/**
 * The primary input controller for the application.
 * <p>
 * Acting as the Controller in the MVC architecture, this class intercepts hardware
 * input events (mouse and keyboard) and translates them into 3D scene transformations
 * (camera panning, rotation, zooming) or application state changes (HUD toggling, entity selection).
 * </p>
 */
public class InputHandler {

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();

    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final SolarSystem3D mainApp;

    private double mouseOldX;
    private double mouseOldY;
    private boolean isDragging = false;

    /**
     * Registers and initializes all event listeners on the provided 3D scene.
     *
     * @param app     the application orchestration layer for triggering state changes (must not be null).
     * @param scene   the primary JavaFX scene to attach listeners to (must not be null).
     * @param camera  the 3D perspective camera to manipulate (must not be null).
     * @param rotateX the X-axis rotation transform applied to the world group (must not be null).
     * @param rotateY the Y-axis rotation transform applied to the world group (must not be null).
     * @throws NullPointerException if any of the dependencies are null.
     */
    public InputHandler(SolarSystem3D app, Scene scene, PerspectiveCamera camera, Rotate rotateX, Rotate rotateY) {
        this.mainApp = Objects.requireNonNull(app, "Application reference cannot be null");
        this.camera = Objects.requireNonNull(camera, "Camera cannot be null");
        this.rotateX = Objects.requireNonNull(rotateX, "RotateX transform cannot be null");
        this.rotateY = Objects.requireNonNull(rotateY, "RotateY transform cannot be null");
        Objects.requireNonNull(scene, "Scene cannot be null");

        initMouseControl(scene);
        initKeyboardControl(scene);
    }

    /**
     * Initializes mouse listeners for orbiting, panning, zooming, and object selection.
     *
     * @param scene the JavaFX 3D scene.
     */
    private void initMouseControl(Scene scene) {
        scene.setOnMousePressed(event -> {
            this.mouseOldX = event.getSceneX();
            this.mouseOldY = event.getSceneY();
            this.isDragging = false;
        });

        scene.setOnMouseDragged(event -> {
            this.isDragging = true;
            double mousePosX = event.getSceneX();
            double mousePosY = event.getSceneY();
            double deltaX = mousePosX - mouseOldX;
            double deltaY = mousePosY - mouseOldY;

            if (event.isSecondaryButtonDown() || (event.isPrimaryButtonDown() && event.isShiftDown())) {
                camera.setTranslateX(camera.getTranslateX() - deltaX * SETTINGS.input().mousePanSpeed());
                camera.setTranslateY(camera.getTranslateY() - deltaY * SETTINGS.input().mousePanSpeed());
            } 
            else if (event.isPrimaryButtonDown()) {
                rotateX.setAngle(rotateX.getAngle() + deltaY * SETTINGS.input().mouseRotSpeed());
                rotateY.setAngle(rotateY.getAngle() - deltaX * SETTINGS.input().mouseRotSpeed());
            }

            this.mouseOldX = mousePosX;
            this.mouseOldY = mousePosY;
        });

        scene.setOnMouseClicked(event -> {
            if (this.isDragging) return;

            PickResult pick = event.getPickResult();
            Node intersectedNode = pick.getIntersectedNode();

            if (intersectedNode != null && intersectedNode.getUserData() instanceof CelestialBody selectedBody) {
                mainApp.showInfoPanel(selectedBody);
            } else {
                mainApp.releaseFocus();
            }

            scene.getRoot().requestFocus();
        });

        scene.setOnScroll((ScrollEvent event) -> {
            double zoomDelta = event.getDeltaY();
            double newZ = camera.getTranslateZ() + zoomDelta * SETTINGS.input().scrollZoomSpeed();
            
            camera.setTranslateZ(clamp(newZ, SETTINGS.input().minCameraZ(), SETTINGS.input().maxCameraZ()));
        });
    }

    /**
     * Initializes keyboard listeners for alternative navigation and global application commands.
     *
     * @param scene the JavaFX 3D scene.
     */
    private void initKeyboardControl(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W -> camera.setTranslateZ(clamp(camera.getTranslateZ() + SETTINGS.input().keyboardZoomSpeed(), 
                                                     SETTINGS.input().minCameraZ(), SETTINGS.input().maxCameraZ()));
                case S -> camera.setTranslateZ(clamp(camera.getTranslateZ() - SETTINGS.input().keyboardZoomSpeed(), 
                                                     SETTINGS.input().minCameraZ(), SETTINGS.input().maxCameraZ()));
                
                case LEFT -> camera.setTranslateX(camera.getTranslateX() - SETTINGS.input().keyboardPanSpeed());
                case RIGHT -> camera.setTranslateX(camera.getTranslateX() + SETTINGS.input().keyboardPanSpeed());
                case UP -> camera.setTranslateY(camera.getTranslateY() - SETTINGS.input().keyboardPanSpeed());
                case DOWN -> camera.setTranslateY(camera.getTranslateY() + SETTINGS.input().keyboardPanSpeed());
                
                case R, ESCAPE -> mainApp.releaseFocus();
                
                case P -> mainApp.togglePauseMenu();
                case C -> mainApp.toggleCreatorPanel();
                default -> {}
            }
        });
    }

    /**
     * Restricts a given value to reside strictly within the provided minimum and maximum boundaries.
     *
     * @param value the raw input value to evaluate.
     * @param min   the lower boundary.
     * @param max   the upper boundary.
     * @return the constrained value.
     */
    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}