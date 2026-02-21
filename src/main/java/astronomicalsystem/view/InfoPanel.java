package astronomicalsystem.view;

import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * A UI overlay component responsible for displaying non-physical, descriptive
 * metadata about a selected celestial body.
 * <p>
 * This class rigorously maintains the Model-View separation. It reads exclusively
 * from the immutable {@link BodyMetadata} record and intentionally avoids retaining
 * strong references to the {@link CelestialBody} domain models, thereby preventing
 * memory leaks when physical bodies are destroyed or merged during the simulation.
 * </p>
 */
public class InfoPanel extends VBox {

    private final Label nameLabel;
    private final Label massLabel;
    private final Label radiusLabel;
    private final Label tempLabel;
    private final Label rotLabel;
    private final Label revLabel;

    /**
     * Initializes the informational heads-up display (HUD) with predefined
     * JavaFX CSS styling, layout constraints, and typography.
     */
    public InfoPanel() {
        this.setStyle("-fx-background-color: rgba(20, 20, 30, 0.3); " +
                "-fx-background-radius: 10; " +
                "-fx-border-color: rgba(255, 255, 255, 0.3); " +
                "-fx-border-radius: 10; " +
                "-fx-border-width: 1;");

        this.setPadding(new Insets(12));
        this.setSpacing(4);
        this.setMaxSize(260, 180);
        this.setAlignment(Pos.TOP_LEFT);
        this.setVisible(false);

        this.nameLabel = createLabel(20, Color.GOLD, true);
        this.massLabel = createLabel(14, Color.WHITE, false);
        this.radiusLabel = createLabel(14, Color.WHITE, false);
        this.tempLabel = createLabel(14, Color.CYAN, false);
        this.rotLabel = createLabel(14, Color.LIGHTGRAY, false);
        this.revLabel = createLabel(14, Color.LIGHTGRAY, false);

        this.getChildren().addAll(
                nameLabel, createSeparator(), massLabel,
                radiusLabel, tempLabel, rotLabel, revLabel
        );
    }

    /**
     * A factory method for generating uniformly styled typography nodes.
     *
     * @param size  the font size in points.
     * @param color the font fill color.
     * @param bold  {@code true} to apply a bold font weight, {@code false} for normal.
     * @return a configured JavaFX {@link Label}.
     */
    private Label createLabel(int size, Color color, boolean bold) {
        Label label = new Label();
        label.setTextFill(color);
        label.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0.5, 0, 0);");
        label.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        label.setWrapText(true);
        return label;
    }

    /**
     * Generates a subtle horizontal divider for visual hierarchy in the HUD.
     *
     * @return a configured JavaFX {@link Line}.
     */
    private Line createSeparator() {
        Line line = new Line(0, 0, 220, 0);
        line.setStroke(Color.rgb(255, 255, 255, 0.3));
        return line;
    }

    /**
     * Populates the UI labels by extracting the metadata record from the target body.
     * <p>
     * Note: This method extracts primitive strings and discards the model reference
     * immediately upon completion, ensuring GC reachability is not artificially extended.
     * </p>
     *
     * @param body the selected celestial entity to inspect.
     */
    public void showPlanet(CelestialBody body) {
        BodyMetadata metadata = body.getMetadata();

        this.nameLabel.setText(body.getName().toUpperCase());
        this.massLabel.setText("Mass: " + metadata.displayMass());
        this.radiusLabel.setText("Radius: " + metadata.displayRadius());
        this.tempLabel.setText("Temperature: " + metadata.temperature());
        this.rotLabel.setText("Rotation Time: " + metadata.rotationPeriod());
        this.revLabel.setText("Revolution Time: " + metadata.revolutionPeriod());

        this.setVisible(true);
    }

    /**
     * Dismisses the informational overlay from the user's view.
     */
    public void hide() {
        this.setVisible(false);
    }
}