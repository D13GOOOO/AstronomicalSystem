package astronomicalsystem.view;

import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Objects;

/**
 * A telemetry readout overlay displaying real-time astronomical data.
 * <p>
 * Employs a unified deep-space visual language consistent with the
 * application's control modules. Automatically parses and presents
 * domain metadata for user inspection.
 * </p>
 * <p>
 * <b>Performance Optimization:</b> This class utilizes UI Node Reuse. 
 * Instead of destroying and recreating text nodes every time a new body is selected, 
 * it initializes the layout hierarchy exactly once and subsequently mutates only 
 * the text properties. This prevents SceneGraph recalculation overhead and 
 * eliminates Garbage Collection spikes during rapid user interactions.
 * </p>
 */
public class InfoPanel extends VBox {

    private final Text nameLabel;
    
    // Reusable text nodes for telemetry data
    private final Text massRow;
    private final Text radiusRow;
    private final Text tempRow;
    private final Text rotationRow;
    private final Text orbitRow;

    /**
     * Initializes the telemetry interface and pre-allocates its internal layout structure.
     */
    public InfoPanel() {
        this.setPadding(new Insets(20));
        this.setSpacing(10);
        this.setMaxWidth(280);
        this.setMaxHeight(200);

        this.setStyle(
                "-fx-background-color: rgba(18, 22, 28, 0.90);" +
                "-fx-background-radius: 0 0 8 0;" +
                "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                "-fx-border-width: 0 1 1 0;" +
                "-fx-border-radius: 0 0 8 0;"
        );

        this.nameLabel = new Text();
        this.nameLabel.setFill(Color.web("#7EA6E0"));
        this.nameLabel.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 18));

        VBox metadataContainer = new VBox(6);
        
        // Pre-allocate the rows to avoid continuous object creation
        this.massRow = createInfoRowNode();
        this.radiusRow = createInfoRowNode();
        this.tempRow = createInfoRowNode();
        this.rotationRow = createInfoRowNode();
        this.orbitRow = createInfoRowNode();

        metadataContainer.getChildren().addAll(massRow, radiusRow, tempRow, rotationRow, orbitRow);

        this.getChildren().addAll(nameLabel, metadataContainer);
        this.setVisible(false);
    }

    /**
     * Populates the readout with the target entity's metadata and renders the overlay.
     *
     * @param body the domain entity subject to inspection (must not be null).
     * @throws NullPointerException if the provided celestial body is null.
     */
    public void showPlanet(CelestialBody body) {
        Objects.requireNonNull(body, "Cannot display information for a null CelestialBody");
        
        this.nameLabel.setText(body.getName().toUpperCase());

        BodyMetadata meta = body.getMetadata();
        if (meta != null) {
            this.massRow.setText("Mass: " + meta.displayMass());
            this.radiusRow.setText("Radius: " + meta.displayRadius());
            this.tempRow.setText("Surface Temp: " + meta.temperature());
            this.rotationRow.setText("Rotation Period: " + meta.rotationPeriod());
            this.orbitRow.setText("Orbital Period: " + meta.revolutionPeriod());
        } else {
            // Fallback for bodies missing metadata
            this.massRow.setText("Mass: N/A");
            this.radiusRow.setText("Radius: N/A");
            this.tempRow.setText("Surface Temp: N/A");
            this.rotationRow.setText("Rotation Period: N/A");
            this.orbitRow.setText("Orbital Period: N/A");
        }

        this.setVisible(true);
    }

    /**
     * Hides the telemetry overlay from the view space.
     */
    public void hide() {
        this.setVisible(false);
    }

    /**
     * Factory method to pre-allocate an empty styled text node for telemetry data.
     *
     * @return a newly configured {@link Text} node.
     */
    private Text createInfoRowNode() {
        Text row = new Text();
        row.setFill(Color.web("#CFD8DC"));
        row.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        return row;
    }
}