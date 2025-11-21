import javafx.scene.Scene;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.PickResult;
import javafx.scene.transform.Rotate;

public class InputHandler {

    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final SolarSystem3D mainApp;
    private double mouseOldX, mouseOldY;
    private boolean isDragging = false;

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
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
            isDragging = false;
        });

        scene.setOnMouseDragged(event -> {
            isDragging = true;
            double mousePosX = event.getSceneX();
            double mousePosY = event.getSceneY();
            double deltaX = mousePosX - mouseOldX;
            double deltaY = mousePosY - mouseOldY;

            if (event.getButton() == MouseButton.SECONDARY || (event.getButton() == MouseButton.PRIMARY && event.isShiftDown())) {
                camera.setTranslateX(camera.getTranslateX() - deltaX * 5.0);
                camera.setTranslateY(camera.getTranslateY() - deltaY * 5.0);
            }
            else if (event.getButton() == MouseButton.PRIMARY) {
                rotateX.setAngle(rotateX.getAngle() - deltaY * 0.2);
                rotateY.setAngle(rotateY.getAngle() + deltaX * 0.2);
            }
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
        });

        // GESTIONE CLICK (Selezione Pianeta)
        scene.setOnMouseClicked(event -> {
            if (isDragging) return;

            PickResult pick = event.getPickResult();
            Node intersectedNode = pick.getIntersectedNode();

            if (intersectedNode != null && intersectedNode.getUserData() instanceof CelestialBody) {
                CelestialBody selectedBody = (CelestialBody) intersectedNode.getUserData();

                // Nessun controllo RingParticle necessario perché non esistono più come sfere cliccabili

                // PAUSA + INFO
                mainApp.setPaused(true);
                mainApp.showInfoPanel(selectedBody);
            }
            else {
                // RIPARTI + NASCONDI
                mainApp.setPaused(false);
                mainApp.hideInfoPanel();
            }

            scene.getRoot().requestFocus();
        });

        scene.setOnScroll((ScrollEvent event) -> {
            double zoomFactor = event.getDeltaY();
            double newZ = camera.getTranslateZ() + zoomFactor * 10.0;
            if (newZ < -200 && newZ > -100000) {
                camera.setTranslateZ(newZ);
            }
        });
    }

    private void initKeyboardControl(Scene scene) {
        scene.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            double panSpeed = 100.0;
            switch (event.getCode()) {
                case W: camera.setTranslateZ(camera.getTranslateZ() + 200); break;
                case S: camera.setTranslateZ(camera.getTranslateZ() - 200); break;
                case LEFT: camera.setTranslateX(camera.getTranslateX() - panSpeed); break;
                case RIGHT: camera.setTranslateX(camera.getTranslateX() + panSpeed); break;
                case UP: camera.setTranslateY(camera.getTranslateY() - panSpeed); break;
                case DOWN: camera.setTranslateY(camera.getTranslateY() + panSpeed); break;
                case R:
                    camera.setTranslateZ(-2500);
                    camera.setTranslateX(0);
                    camera.setTranslateY(0);
                    rotateX.setAngle(20);
                    rotateY.setAngle(0);
                    break;
                case SPACE:
                    mainApp.togglePause();
                    break;
            }
        });
    }
}