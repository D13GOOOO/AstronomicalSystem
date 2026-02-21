package astronomicalsystem.view;

import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import javafx.geometry.Insets;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A telemetry readout overlay displaying real-time astronomical data.
 * <p>
 * Employs a unified deep-space visual language consistent with the
 * application's control modules. Automatically parses and presents
 * domain metadata for user inspection.
 * </p>
 */
public class InfoPanel extends VBox {

    private final Text nameLabel;
    private final VBox metadataContainer;

    /**
     * Initializes the telemetry interface and its internal layout structure.
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

        this.metadataContainer = new VBox(6);

        this.getChildren().addAll(nameLabel, metadataContainer);
        this.setVisible(false);
    }

    /**
     * Populates the readout with the target entity's metadata and renders the overlay.
     *
     * @param body the domain entity subject to inspection.
     */
    public void showPlanet(CelestialBody body) {
        this.nameLabel.setText(body.getName().toUpperCase());
        this.metadataContainer.getChildren().clear();

        BodyMetadata meta = body.getMetadata();
        if (meta != null) {
            addInfoRow("Mass:", meta.displayMass());
            addInfoRow("Radius:", meta.displayRadius());
            addInfoRow("Surface Temp:", meta.temperature());
            addInfoRow("Rotation Period:", meta.rotationPeriod());
            addInfoRow("Orbital Period:", meta.revolutionPeriod());
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
     * Formats and appends a discrete telemetry data point to the layout.
     *
     * @param label the data category key.
     * @param value the data category value.
     */
    private void addInfoRow(String label, String value) {
        Text row = new Text(label + " " + value);
        row.setFill(Color.web("#CFD8DC"));
        row.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 13));
        this.metadataContainer.getChildren().add(row);
    }
}