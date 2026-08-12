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
            } else if (event.isPrimaryButtonDown()) {
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
            double newZ = camera.getTranslateZ() + zoomDelta * SETTINGS.input().scrollZoomSpeed();
            camera.setTranslateZ(clamp(newZ, SETTINGS.input().maxCameraZ(), SETTINGS.input().minCameraZ()));
        });
    }

    private void initKeyboardControl(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            switch (event.getCode()) {
                case W -> camera.setTranslateZ(camera.getTranslateZ() + SETTINGS.input().keyboardZoomSpeed());
                case S -> camera.setTranslateZ(camera.getTranslateZ() - SETTINGS.input().keyboardZoomSpeed());
                case LEFT -> camera.setTranslateX(camera.getTranslateX() - SETTINGS.input().keyboardPanSpeed());
                case RIGHT -> camera.setTranslateX(camera.getTranslateX() + SETTINGS.input().keyboardPanSpeed());
                case UP -> camera.setTranslateY(camera.getTranslateY() - SETTINGS.input().keyboardPanSpeed());
                case DOWN -> camera.setTranslateY(camera.getTranslateY() + SETTINGS.input().keyboardPanSpeed());
                case R -> {
                    camera.setTranslateZ(SETTINGS.input().defaultCameraZ());
                    camera.setTranslateX(0);
                    camera.setTranslateY(0);
                    rotateX.setAngle(SETTINGS.input().defaultRotX());
                    rotateY.setAngle(SETTINGS.input().defaultRotY());
                }
                case SPACE -> mainApp.togglePause();
                case P -> mainApp.togglePauseMenu();
                case C -> mainApp.toggleCreatorPanel();
                default -> {}
            }
        });
    }

    static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}