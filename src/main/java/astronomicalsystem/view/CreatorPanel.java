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
 * A dynamic HUD overlay for runtime instantiation of celestial bodies.
 * <p>
 * Implements a sliding drawer interface with a constrained layout and a minimalist,
 * deep-space aesthetic. Collects physical parameters via analog sliders and passes
 * them to an injected callback for execution within the physics engine context.
 * This class acts strictly as a View, delegating all domain logic to the {@link PlanetSpawner}.
 * </p>
 */
public class CreatorPanel extends VBox {

    private static final double PANEL_WIDTH = 300.0;
    
    private boolean isVisible = false;
    private final TranslateTransition transition;

    /**
     * Functional interface for delegating the physical creation logic back to the Controller or Model.
     */
    @FunctionalInterface
    public interface PlanetSpawner {
        /**
         * Invoked when the user confirms the creation of a new celestial body.
         *
         * @param massMultiplier the mass coefficient relative to a base value.
         * @param distance       the absolute spawn distance from the system anchor.
         * @param velocity       the initial orbital velocity scalar.
         */
        void spawn(double massMultiplier, double distance, double velocity);
    }

    /**
     * Initializes the slide-out creation interface with strict size constraints and styling.
     *
     * @param spawner the functional callback invoked upon user submission (must not be null).
     * @throws NullPointerException if the spawner callback is null.
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
     * Toggles the visibility state and executes the sliding drawer animation.
     */
    public void toggle() {
        this.isVisible = !this.isVisible;
        this.transition.setToX(this.isVisible ? 0 : PANEL_WIDTH);
        this.transition.play();
    }

    /**
     * Factory method for creating consistently styled parameter sliders.
     */
    private Slider createSlider(double min, double max, double value) {
        Slider slider = new Slider(min, max, value);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        slider.setStyle("-fx-control-inner-background: #202630;");
        return slider;
    }

    /**
     * Assembles a vertically stacked control group featuring a label bound to the slider's value.
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