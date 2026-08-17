package astronomicalsystem.view;

import java.util.Objects;

import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.CelestialBody.BodyMetadata;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A graphical user interface panel designed to display detailed, human readable statistics 
 * about a specific celestial body when the user selects it within the simulation. 
 * 
 * To ensure the application remains perfectly smooth and responsive, this class employs a 
 * highly optimized design strategy known as Node Reuse. When the user clicks rapidly between 
 * different planets, the application does not destroy the old text boxes and build new ones. 
 * Instead, it builds the visual structure exactly once when the application launches, and simply 
 * swaps out the raw text values whenever a new planet is selected. This completely eliminates 
 * graphical stuttering and memory spikes that would otherwise be caused by constantly creating 
 * and destroying interface elements.
 */
public class InfoPanel extends VBox {

    /**
     * The prominent text element displaying the name of the currently selected celestial body.
     */
    private final Text nameLabel;
    
    /**
     * A reusable text element dedicated to displaying the estimated physical mass of the body.
     */
    private final Text massRow;

    /**
     * A reusable text element dedicated to displaying the estimated physical size of the body.
     */
    private final Text radiusRow;

    /**
     * A reusable text element dedicated to displaying the average surface temperature of the body.
     */
    private final Text tempRow;

    /**
     * A reusable text element dedicated to displaying how long it takes for the body to complete one full rotation on its axis.
     */
    private final Text rotationRow;

    /**
     * A reusable text element dedicated to displaying how long it takes for the body to complete one full orbit around its host.
     */
    private final Text orbitRow;

    /**
     * Constructs the information panel, configuring its internal layout structure, visual styling, 
     * and permanently allocating all the text elements it will need. It applies a sophisticated 
     * aerospace HUD design and integrates an interactive tactical release mechanism to return 
     * the camera to a free-roaming state.
     *
     * @param onReleaseFocus the executable action to trigger when the user clicks the return button, 
     *                       typically restoring the camera and unpausing the simulation. Must not be null.
     * @throws NullPointerException if the provided callback function is missing.
     */
    public InfoPanel(Runnable onReleaseFocus) {
        Objects.requireNonNull(onReleaseFocus, "Release focus callback cannot be null");

        this.setPadding(new Insets(20));
        this.setSpacing(15);
        this.setMaxWidth(280);

        this.setStyle(
                "-fx-background-color: rgba(12, 18, 28, 0.85);" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: rgba(0, 230, 255, 0.5) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 2 1 1 1;" +
                "-fx-border-radius: 4;"
        );

        Text targetHeader = new Text("TARGET LOCKED // TELEMETRY");
        targetHeader.setFill(Color.web("#FF7043"));
        targetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        this.nameLabel = new Text();
        this.nameLabel.setFill(Color.web("#FFFFFF"));
        this.nameLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));

        VBox headerBox = new VBox(2, targetHeader, this.nameLabel);

        VBox metadataContainer = new VBox(8);
        
        this.massRow = createValueNode();
        this.radiusRow = createValueNode();
        this.tempRow = createValueNode();
        this.rotationRow = createValueNode();
        this.orbitRow = createValueNode();

        metadataContainer.getChildren().addAll(
                createTelemetryRow("MASS", this.massRow),
                createTelemetryRow("RADIUS", this.radiusRow),
                createTelemetryRow("SURFACE TEMP", this.tempRow),
                createTelemetryRow("ROTATION", this.rotationRow),
                createTelemetryRow("ORBITAL PERIOD", this.orbitRow)
        );

        Button returnBtn = new Button("RETURN [ R ]");
        returnBtn.setMaxWidth(Double.MAX_VALUE);
        returnBtn.setPrefHeight(30);

        String btnStyle = "-fx-background-color: rgba(255, 112, 67, 0.1); -fx-border-color: rgba(255, 112, 67, 0.5); " +
                "-fx-text-fill: #FF7043; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 5 15 5 15;";
                
        String btnHoverStyle = "-fx-background-color: rgba(255, 112, 67, 0.25); -fx-border-color: #FF7043; " +
                "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-font-weight: bold; " +
                "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 5 15 5 15;";

        returnBtn.setStyle(btnStyle);
        returnBtn.setOnMouseEntered(e -> returnBtn.setStyle(btnHoverStyle));
        returnBtn.setOnMouseExited(e -> returnBtn.setStyle(btnStyle));
        returnBtn.setOnAction(e -> onReleaseFocus.run());

        VBox.setMargin(returnBtn, new Insets(10, 0, 0, 0));

        this.getChildren().addAll(headerBox, metadataContainer, returnBtn);
        this.setVisible(false);
    }

    /**
     * Replaces the currently displayed text with the specific statistical data belonging to the newly 
     * selected celestial body, and immediately makes the panel visible to the user.
     * 
     * If the selected celestial body was dynamically generated by the user and lacks detailed, real world 
     * descriptive text, this method safely falls back to displaying "Not Available" placeholders rather 
     * than allowing the application to crash due to missing data.
     *
     * @param body the celestial entity that the user has selected to inspect. Must not be null.
     * @throws NullPointerException if the provided celestial body reference is missing.
     */
    public void showPlanet(CelestialBody body) {
        Objects.requireNonNull(body, "Cannot display information for a null CelestialBody");
        
        this.nameLabel.setText(body.getName().toUpperCase());

        BodyMetadata meta = body.getMetadata();
        if (meta != null) {
            this.massRow.setText(meta.displayMass());
            this.radiusRow.setText(meta.displayRadius());
            this.tempRow.setText(meta.temperature());
            this.rotationRow.setText(meta.rotationPeriod());
            this.orbitRow.setText(meta.revolutionPeriod());
        } else {
            this.massRow.setText("N/A");
            this.radiusRow.setText("N/A");
            this.tempRow.setText("N/A");
            this.rotationRow.setText("N/A");
            this.orbitRow.setText("N/A");
        }

        this.setVisible(true);
    }

    /**
     * Hides the information panel from the screen. This is typically called when the user clicks 
     * into empty space to deselect their current target.
     */
    public void hide() {
        this.setVisible(false);
    }

    /**
     * An internal factory helper that generates a perfectly aligned horizontal row for the telemetry 
     * monitor. It places the descriptive label on the far left and pushes the dynamic data value to 
     * the far right, creating a clean, professional dashboard appearance.
     *
     * @param labelText the static category name to display on the left.
     * @param valueNode the dynamic text node that will display the actual data on the right.
     * @return a fully configured horizontal layout box containing both elements.
     */
    private HBox createTelemetryRow(String labelText, Text valueNode) {
        Text label = new Text(labelText);
        label.setFill(Color.web("#7EA6E0"));
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(label, spacer, valueNode);
        row.setAlignment(Pos.CENTER);
        return row;
    }

    /**
     * An internal factory helper that generates standard JavaFX text elements specifically styled 
     * for displaying numerical data. It uses a monospaced font and a bright neon color to mimic 
     * digital screens.
     *
     * @return a fully styled, empty text element ready to be injected into the interface layout.
     */
    private Text createValueNode() {
        Text row = new Text();
        row.setFill(Color.web("#00E6FF"));
        row.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        return row;
    }
}