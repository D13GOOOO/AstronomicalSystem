package astronomicalsystem.controller;

import astronomicalsystem.SolarSystem3D;
import astronomicalsystem.model.CelestialBody;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.PickResult;
import javafx.scene.input.ScrollEvent;
import javafx.scene.transform.Rotate;

/**
 * The primary input controller for the application.
 * <p>
 * Acting as the Controller in the MVC architecture, this class intercepts hardware
 * input events (mouse and keyboard) and translates them into 3D scene transformations
 * (camera panning, rotation, zooming) or application state changes (pausing, entity selection).
 * </p>
 */
public class InputHandler {

    private static final double MOUSE_PAN_SPEED = 5.0;
    private static final double MOUSE_ROT_SPEED = 0.2;
    private static final double SCROLL_ZOOM_SPEED = 10.0;

    private static final double KEYBOARD_PAN_SPEED = 100.0;
    private static final double KEYBOARD_ZOOM_SPEED = 200.0;

    private static final double MIN_CAMERA_Z = -200.0;
    private static final double MAX_CAMERA_Z = -100000.0;

    private static final double DEFAULT_CAMERA_Z = -3000.0;
    private static final double DEFAULT_ROT_X = 20.0;
    private static final double DEFAULT_ROT_Y = 0.0;

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
     * @param app     the application orchestration layer for triggering state changes.
     * @param scene   the primary JavaFX scene to attach listeners to.
     * @param camera  the 3D perspective camera to manipulate.
     * @param rotateX the X-axis rotation transform applied to the world group.
     * @param rotateY the Y-axis rotation transform applied to the world group.
     */
    public InputHandler(SolarSystem3D app, Scene scene, PerspectiveCamera camera, Rotate rotateX, Rotate rotateY) {
        this.mainApp = app;
        this.camera = camera;
        this.rotateX = rotateX;
        this.rotateY = rotateY;

        initMouseControl(scene);
        initKeyboardControl(scene);
    }

    /**
     * Binds mouse press, drag, release, and scroll events.
     *
     * @param scene the active JavaFX scene.
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

            if (event.getButton() == MouseButton.SECONDARY || (event.getButton() == MouseButton.PRIMARY && event.isShiftDown())) {
                camera.setTranslateX(camera.getTranslateX() - deltaX * MOUSE_PAN_SPEED);
                camera.setTranslateY(camera.getTranslateY() - deltaY * MOUSE_PAN_SPEED);
            } else if (event.getButton() == MouseButton.PRIMARY) {
                rotateX.setAngle(rotateX.getAngle() + deltaY * MOUSE_ROT_SPEED);
                rotateY.setAngle(rotateY.getAngle() - deltaX * MOUSE_ROT_SPEED);
            }

            this.mouseOldX = mousePosX;
            this.mouseOldY = mousePosY;
        });

        scene.setOnMouseClicked(event -> {
            if (this.isDragging) return;

            PickResult pick = event.getPickResult();
            Node intersectedNode = pick.getIntersectedNode();

            if (intersectedNode != null && intersectedNode.getUserData() instanceof CelestialBody selectedBody) {
                mainApp.setPaused(true);
                mainApp.showInfoPanel(selectedBody);
            } else {
                mainApp.setPaused(false);
                mainApp.hideInfoPanel();
            }

            scene.getRoot().requestFocus();
        });

        scene.setOnScroll((ScrollEvent event) -> {
            double zoomDelta = event.getDeltaY();
            double newZ = camera.getTranslateZ() + zoomDelta * SCROLL_ZOOM_SPEED;

            if (newZ < MIN_CAMERA_Z && newZ > MAX_CAMERA_Z) {
                camera.setTranslateZ(newZ);
            }
        });
    }

    /**
     * Binds keyboard shortcuts for camera manipulation and simulation control.
     *
     * @param scene the active JavaFX scene.
     */
    private void initKeyboardControl(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W -> camera.setTranslateZ(camera.getTranslateZ() + KEYBOARD_ZOOM_SPEED);
                case S -> camera.setTranslateZ(camera.getTranslateZ() - KEYBOARD_ZOOM_SPEED);
                case LEFT -> camera.setTranslateX(camera.getTranslateX() - KEYBOARD_PAN_SPEED);
                case RIGHT -> camera.setTranslateX(camera.getTranslateX() + KEYBOARD_PAN_SPEED);
                case UP -> camera.setTranslateY(camera.getTranslateY() - KEYBOARD_PAN_SPEED);
                case DOWN -> camera.setTranslateY(camera.getTranslateY() + KEYBOARD_PAN_SPEED);
                case R -> {
                    camera.setTranslateZ(DEFAULT_CAMERA_Z);
                    camera.setTranslateX(0);
                    camera.setTranslateY(0);
                    rotateX.setAngle(DEFAULT_ROT_X);
                    rotateY.setAngle(DEFAULT_ROT_Y);
                }
                case SPACE -> mainApp.togglePause();
                default -> {}
            }
        });
    }
}