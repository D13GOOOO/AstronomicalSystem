package astronomicalsystem.view;

import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A graphical user interface component that functions as a control drawer, 
 * allowing the user to dynamically inject newly created planets into the running simulation.
 * 
 * This class strictly adheres to the Model-View-Controller architecture. It acts entirely as 
 * a "View", concerning itself only with how sliders look and feel and capturing the user's input. 
 * It contains absolutely zero physics logic. When the user clicks the launch button, this panel 
 * simply packages the slider values and passes them blindly to the delegated spawner callback, 
 * forcing the core application logic to handle the actual mathematics of creating the planet.
 */
public class CreatorPanel extends VBox {

    /**
     * An internal tracking flag indicating whether the drawer is currently extended onto the screen 
     * or hidden away out of bounds.
     */
    private boolean isVisible = false;

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
     * and styling colors. It formats the container to integrate seamlessly into a unified, 
     * dashboard-style interface utilizing an aerospace HUD aesthetic.
     *
     * @param spawner the functional callback that will receive the user's input data when they 
     *                attempt to create a new planet. Must not be null.
     * @throws NullPointerException if the provided spawner callback is missing.
     */
    public CreatorPanel(PlanetSpawner spawner) {
        Objects.requireNonNull(spawner, "PlanetSpawner callback cannot be null");

        this.setVisible(false);

        this.setPrefWidth(280.0);
        this.setMaxWidth(280.0);
        
        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(20);
        this.setPadding(new Insets(20, 20, 20, 20));

        this.setStyle(
                "-fx-background-color: rgba(12, 18, 28, 0.85);" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: rgba(0, 230, 255, 0.5) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 2 1 1 1;" +
                "-fx-border-radius: 4;"
        );

        Text targetHeader = new Text("INJECTION PROTOCOL // CUSTOM");
        targetHeader.setFill(Color.web("#FF7043"));
        targetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Text title = new Text("LAUNCH CONTROL");
        title.setFill(Color.web("#FFFFFF"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        VBox headerBox = new VBox(2, targetHeader, title);
        headerBox.setAlignment(Pos.TOP_LEFT);
        VBox.setMargin(headerBox, new Insets(0, 0, 5, 0));

        Slider massSlider = createSlider(0.1, 1000.0, 50.0);
        VBox massControl = createControlGroup("MASS (EARTHS)", massSlider);

        Slider distSlider = createSlider(200.0, 4000.0, 1000.0);
        VBox distControl = createControlGroup("SPAWN DISTANCE", distSlider);

        Slider velSlider = createSlider(0.5, 1.4, 1.0);
        VBox velControl = createControlGroup("ORBITAL MULTIPLIER", velSlider);

        Button launchBtn = new Button("INITIALIZE ORBIT");
        launchBtn.setPrefWidth(240);
        launchBtn.setPrefHeight(35);

        String btnStyle = "-fx-background-color: rgba(255, 112, 67, 0.1); -fx-border-color: rgba(255, 112, 67, 0.5); " +
                "-fx-text-fill: #FF7043; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-border-radius: 4; -fx-cursor: hand; -fx-padding: 5 15 5 15;";
                
        String btnHoverStyle = "-fx-background-color: rgba(255, 112, 67, 0.25); -fx-border-color: #FF7043; " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-border-radius: 4; -fx-cursor: hand; -fx-padding: 5 15 5 15;";

        launchBtn.setStyle(btnStyle);
        launchBtn.setOnMouseEntered(e -> launchBtn.setStyle(btnHoverStyle));
        launchBtn.setOnMouseExited(e -> launchBtn.setStyle(btnStyle));

        launchBtn.setOnAction(e -> spawner.spawn(massSlider.getValue(), distSlider.getValue(), velSlider.getValue()));

        this.getChildren().addAll(headerBox, massControl, distControl, velControl, launchBtn);
    }

    /**
     * Reverses the current visibility state of the panel. If the panel is hidden, it becomes 
     * visible. If it is already visible, it is hidden from the screen.
     */
    public void toggle() {
        this.isVisible = !this.isVisible;
        this.setVisible(this.isVisible);
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
        slider.setStyle("-fx-control-inner-background: #101622; -fx-background-color: transparent;");
        return slider;
    }

    /**
     * An internal factory helper that pairs a slider with an interactive telemetry label. 
     * It automatically binds the slider's movement to the label, ensuring that the text 
     * dynamically updates to show the exact number the user is currently selecting on the right side.
     *
     * @param labelText the static category name to display on the left side of the control block.
     * @param slider    the pre-configured slider component to bind to the dynamic value label.
     * @return a vertically stacked container holding the aligned text header and the slider.
     */
    private VBox createControlGroup(String labelText, Slider slider) {
        Text label = new Text(labelText);
        label.setFill(Color.web("#7EA6E0"));
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Text valueLabel = new Text(String.format("%.1f", slider.getValue()));
        valueLabel.setFill(Color.web("#00E6FF"));
        valueLabel.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox header = new HBox(label, spacer, valueLabel);
        header.setAlignment(Pos.CENTER);

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
                valueLabel.setText(String.format("%.1f", newVal.doubleValue()))
        );

        VBox box = new VBox(8, header, slider);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }
}