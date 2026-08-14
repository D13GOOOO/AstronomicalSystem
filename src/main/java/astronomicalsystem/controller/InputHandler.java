package astronomicalsystem.controller;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.SolarSystem3D;
import astronomicalsystem.model.CelestialBody;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;

import java.util.Objects;

/**
 * The primary input controller for the application.
 * <p>
 * Acting as the Controller in the MVC architecture, this class intercepts hardware
 * input events (mouse and keyboard) and translates them into 3D scene transformations
 * (camera panning, rotation, zooming) or application state changes (pausing, entity selection).
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

            // Secondary button (Right click) or Primary + Shift (Left click + Shift) pans the camera
            if (event.isSecondaryButtonDown() || (event.isPrimaryButtonDown() && event.isShiftDown())) {
                camera.setTranslateX(camera.getTranslateX() - deltaX * SETTINGS.input().mousePanSpeed());
                camera.setTranslateY(camera.getTranslateY() - deltaY * SETTINGS.input().mousePanSpeed());
            } 
            // Primary button (Left click) rotates the camera (Orbital controls)
            else if (event.isPrimaryButtonDown()) {
                rotateX.setAngle(rotateX.getAngle() + deltaY * SETTINGS.input().mouseRotSpeed());
                rotateY.setAngle(rotateY.getAngle() - deltaX * SETTINGS.input().mouseRotSpeed());
            }

            this.mouseOldX = mousePosX;
            this.mouseOldY = mousePosY;
        });

        scene.setOnMouseClicked(event -> {
            // Ignore click logic if the user was actively dragging the camera
            if (this.isDragging) return;

            // Perform 3D raycasting to detect clicks on celestial meshes
            PickResult pick = event.getPickResult();
            Node intersectedNode = pick.getIntersectedNode();

            // Check if a valid 3D node was clicked and extract its mapped model entity
            if (intersectedNode != null && intersectedNode.getUserData() instanceof CelestialBody selectedBody) {
                mainApp.setPaused(true);
                mainApp.showInfoPanel(selectedBody);
            } else {
                mainApp.setPaused(false);
                mainApp.hideInfoPanel();
            }

            // Ensure the root node regains focus so keyboard events continue firing
            scene.getRoot().requestFocus();
        });

        scene.setOnScroll((ScrollEvent event) -> {
            double zoomDelta = event.getDeltaY();
            double newZ = camera.getTranslateZ() + zoomDelta * SETTINGS.input().scrollZoomSpeed();
            
            // Limit camera depth to prevent clipping out of the skybox or piercing planets
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
                // Zoom controls with safety clamping
                case W -> camera.setTranslateZ(clamp(camera.getTranslateZ() + SETTINGS.input().keyboardZoomSpeed(), 
                                                     SETTINGS.input().minCameraZ(), SETTINGS.input().maxCameraZ()));
                case S -> camera.setTranslateZ(clamp(camera.getTranslateZ() - SETTINGS.input().keyboardZoomSpeed(), 
                                                     SETTINGS.input().minCameraZ(), SETTINGS.input().maxCameraZ()));
                
                // Pan controls
                case LEFT -> camera.setTranslateX(camera.getTranslateX() - SETTINGS.input().keyboardPanSpeed());
                case RIGHT -> camera.setTranslateX(camera.getTranslateX() + SETTINGS.input().keyboardPanSpeed());
                case UP -> camera.setTranslateY(camera.getTranslateY() - SETTINGS.input().keyboardPanSpeed());
                case DOWN -> camera.setTranslateY(camera.getTranslateY() + SETTINGS.input().keyboardPanSpeed());
                
                // Camera reset
                case R -> {
                    camera.setTranslateZ(SETTINGS.input().defaultCameraZ());
                    camera.setTranslateX(0);
                    camera.setTranslateY(0);
                    rotateX.setAngle(SETTINGS.input().defaultRotX());
                    rotateY.setAngle(SETTINGS.input().defaultRotY());
                }
                
                // Application state toggles
                case SPACE -> mainApp.togglePause();
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