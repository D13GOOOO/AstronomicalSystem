package astronomicalsystem.view;

import java.util.Objects;

import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

/**
 * A graphical user interface component that functions as a slide-out control drawer, 
 * allowing the user to dynamically inject newly created planets into the running simulation.
 * 
 * This class strictly adheres to the Model-View-Controller architecture. It acts entirely as 
 * a "View", concerning itself only with how sliders look and feel, capturing the user's input, 
 * and animating the drawer in and out of the screen. It contains absolutely zero physics logic. 
 * When the user clicks the launch button, this panel simply packages the slider values and passes 
 * them blindly to the delegated spawner callback, forcing the core application logic to handle the 
 * actual math of creating the planet.
 */
public class CreatorPanel extends VBox {

    /**
     * The fixed horizontal width of the control panel in pixels. This value is also used to 
     * calculate exactly how far off-screen the panel needs to slide to hide itself completely.
     */
    private static final double PANEL_WIDTH = 300.0;
    
    /**
     * An internal tracking flag indicating whether the drawer is currently extended onto the screen 
     * or hidden away out of bounds.
     */
    private boolean isVisible = false;

    /**
     * The JavaFX animation object responsible for smoothly sliding the panel horizontally 
     * across the screen rather than having it instantly appear or disappear.
     */
    private final TranslateTransition transition;

    /**
     * A strict contract that must be fulfilled by the main application controller. It dictates exactly 
     * how the graphical interface hands over the user's creation parameters to the physics engine.
     */
    @FunctionalInterface
    public interface PlanetSpawner {
        /**
         * Triggered the exact moment the user clicks the initialization button on the interface.
         *
         * @param massMultiplier the mathematical weight of the new planet relative to the Earth.
         * @param distance       the physical starting distance from the center of the universe.
         * @param velocity       a multiplier applied to the planet's starting speed, allowing the user 
         *                       to create perfectly stable circular orbits or chaotic elliptical ones.
         */
        void spawn(double massMultiplier, double distance, double velocity);
    }

    /**
     * Constructs the graphical creation panel, configuring all internal sliders, text labels, 
     * styling colors, and animation settings. It automatically positions itself completely 
     * off-screen to the right upon creation, waiting for a toggle command to slide into view.
     *
     * @param spawner the functional callback that will receive the user's input data when they 
     *                attempt to create a new planet. Must not be null.
     * @throws NullPointerException if the provided spawner callback is missing.
     */
    public CreatorPanel(PlanetSpawner spawner) {
        Objects.requireNonNull(spawner, "PlanetSpawner callback cannot be null");

        this.setPrefWidth(PANEL_WIDTH);
        this.setMaxWidth(PANEL_WIDTH);
        this.setMaxHeight(400);
        
        this.setTranslateX(PANEL_WIDTH);

        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(20);
        this.setPadding(new Insets(25, 20, 25, 20));

        this.setStyle(
                "-fx-background-color: rgba(18, 22, 28, 0.90);" +
                "-fx-background-radius: 8 0 0 8;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-radius: 8 0 0 8;" +
                "-fx-border-width: 1 0 1 1;"
        );

        this.transition = new TranslateTransition(Duration.millis(300), this);

        Text title = new Text("CUSTOM ENTITY");
        title.setFill(Color.web("#7EA6E0"));
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));

        Slider massSlider = createSlider(0.1, 1000.0, 50.0);
        VBox massControl = createControlGroup("Mass (Earths): ", massSlider);

        Slider distSlider = createSlider(200.0, 4000.0, 1000.0);
        VBox distControl = createControlGroup("Spawn Distance: ", distSlider);

        Slider velSlider = createSlider(0.5, 1.4, 1.0);
        VBox velControl = createControlGroup("Orbital Multiplier: x", velSlider);

        Button launchBtn = new Button("INITIALIZE ORBIT");
        launchBtn.setPrefWidth(260);
        launchBtn.setPrefHeight(35);

        String btnStyle = "-fx-background-color: transparent; -fx-border-color: rgba(126, 166, 224, 0.4); " +
                "-fx-text-fill: #7EA6E0; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
                "-fx-border-radius: 4; -fx-cursor: hand; -fx-padding: 8 15 8 15;";
                
        String btnHoverStyle = "-fx-background-color: rgba(126, 166, 224, 0.1); -fx-border-color: #7EA6E0; " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 13px; " +
                "-fx-border-radius: 4; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

        launchBtn.setStyle(btnStyle);
        launchBtn.setOnMouseEntered(e -> launchBtn.setStyle(btnHoverStyle));
        launchBtn.setOnMouseExited(e -> launchBtn.setStyle(btnStyle));

        launchBtn.setOnAction(e -> spawner.spawn(massSlider.getValue(), distSlider.getValue(), velSlider.getValue()));

        this.getChildren().addAll(title, massControl, distControl, velControl, launchBtn);
    }

    /**
     * Reverses the current visibility state of the panel and triggers the sliding animation. 
     * If the panel is hidden, it slides into view. If it is already visible, it slides back 
     * out of the screen bounds.
     */
    public void toggle() {
        this.isVisible = !this.isVisible;
        this.transition.setToX(this.isVisible ? 0 : PANEL_WIDTH);
        this.transition.play();
    }

    /**
     * An internal factory helper that generates standard JavaFX sliders with consistent styling 
     * to match the deep space aesthetic of the user interface.
     *
     * @param min   the absolute minimum numerical limit the user can drag the slider to.
     * @param max   the absolute maximum numerical limit the user can drag the slider to.
     * @param value the default numerical value the slider should rest at upon creation.
     * @return a fully styled and configured JavaFX slider component.
     */
    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        slider.setStyle("-fx-control-inner-background: #202630;");
        return slider;
    }

    /**
     * An internal factory helper that pairs a slider with an interactive text label. 
     * It automatically binds the slider's movement to the label, ensuring that the text 
     * dynamically updates to show the exact number the user is currently selecting.
     *
     * @param labelText the static prefix text to display before the dynamically updating number.
     * @param slider    the pre-configured slider component to bind to the label.
     * @return a vertically stacked container holding both the dynamic label and the slider.
     */
    private VBox createControlGroup(String labelText, Slider slider) {
        Label label = new Label(labelText + String.format("%.1f", slider.getValue()));
        label.setTextFill(Color.web("#CFD8DC"));
        label.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                label.setText(labelText + String.format("%.1f", newVal.doubleValue()))
        );

        VBox box = new VBox(5, label, slider);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}