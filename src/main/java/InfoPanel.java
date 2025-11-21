import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class InfoPanel extends VBox {

    private final Label nameLabel;
    private final Label massLabel;
    private final Label radiusLabel;
    private final Label tempLabel;
    private final Label rotLabel;
    private final Label revLabel;

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

        nameLabel = createLabel(20, Color.GOLD, true);
        massLabel = createLabel(14, Color.WHITE, false);
        radiusLabel = createLabel(14, Color.WHITE, false);
        tempLabel = createLabel(14, Color.CYAN, false);
        rotLabel = createLabel(14, Color.LIGHTGRAY, false);
        revLabel = createLabel(14, Color.LIGHTGRAY, false);

        this.getChildren().addAll(nameLabel, createSeparator(), massLabel, radiusLabel, tempLabel, rotLabel, revLabel);
    }

    private Label createLabel(int size, Color color, boolean bold) {
        Label l = new Label();
        l.setTextFill(color);
        l.setStyle("-fx-effect: dropshadow(one-pass-box, black, 2, 0.5, 0, 0);");
        l.setFont(Font.font("Arial", bold ? FontWeight.BOLD : FontWeight.NORMAL, size));
        l.setWrapText(true);
        return l;
    }

    private javafx.scene.shape.Line createSeparator() {
        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 0, 220, 0);
        line.setStroke(Color.rgb(255, 255, 255, 0.3));
        return line;
    }

    public void showPlanet(CelestialBody body) {
        nameLabel.setText(body.getName().toUpperCase());

        // ORA LEGGE DIRETTAMENTE LE STRINGHE REALI, IGNORANDO I CALCOLI
        massLabel.setText("Mass: " + body.getDisplayMass());
        radiusLabel.setText("Radius: " + body.getDisplayRadius());

        tempLabel.setText("Temperature: " + body.getTempInfo());
        rotLabel.setText("Rotation Time: " + body.getRotPeriod());
        revLabel.setText("Revolution Time: " + body.getRevPeriod());

        this.setVisible(true);
    }

    public void hide() {
        this.setVisible(false);
    }
}