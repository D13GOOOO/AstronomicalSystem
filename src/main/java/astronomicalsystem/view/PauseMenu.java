package astronomicalsystem.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A graphical overlay menu providing simulation control and state management.
 * <p>
 * Follows a minimalist deep-space aesthetic. Provides interactive buttons
 * for resuming, quick-saving, quick-loading, and terminating the application.
 * </p>
 */
public class PauseMenu extends VBox {

    private final Text statusText;

    /**
     * Constructs the pause menu overlay with interactive action bindings.
     *
     * @param onResume the callback triggered to close the menu and unpause.
     * @param onSave   the callback triggered to serialize the simulation state.
     * @param onLoad   the callback triggered to deserialize the simulation state.
     * @param onQuit   the callback triggered to terminate the JVM.
     */
    public PauseMenu(Runnable onResume, Runnable onSave, Runnable onLoad, Runnable onQuit) {
        this.setAlignment(Pos.CENTER);
        this.setSpacing(15);
        this.setMaxSize(280, 320);

        this.setStyle(
                "-fx-background-color: rgba(18, 22, 28, 0.90);" +
                        "-fx-background-radius: 8;" +
                        "-fx-border-color: rgba(255, 255, 255, 0.08);" +
                        "-fx-border-radius: 8;" +
                        "-fx-border-width: 1;"
        );

        Text title = new Text("SYSTEM PAUSED");
        title.setFill(Color.web("#7EA6E0"));
        title.setFont(Font.font("Segoe UI", FontWeight.SEMI_BOLD, 16));
        // Rimosso setLetterSpacing per compatibilità nativa

        VBox.setMargin(title, new javafx.geometry.Insets(0, 0, 15, 0));

        Button btnResume = createButton("RESUME SIMULATION", onResume);
        Button btnSave = createButton("QUICK SAVE", onSave);
        Button btnLoad = createButton("QUICK LOAD", onLoad);
        Button btnQuit = createButton("TERMINATE", onQuit);

        this.statusText = new Text("");
        this.statusText.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));

        this.getChildren().addAll(title, btnResume, btnSave, btnLoad, btnQuit, this.statusText);
        this.setVisible(false);
    }

    /**
     * Factory method for standardized UI buttons matching the unified theme.
     *
     * @param text   the display label.
     * @param action the executable behavior on click.
     * @return a styled JavaFX Button.
     */
    private Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(35);

        String baseStyle = "-fx-background-color: transparent; -fx-border-color: rgba(200, 200, 200, 0.2); " +
                "-fx-text-fill: #CFD8DC; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
                "-fx-border-radius: 4; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: #CFD8DC; " +
                "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
                "-fx-border-radius: 4; -fx-cursor: hand;";

        btn.setStyle(baseStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Displays transient visual feedback for user actions.
     *
     * @param message the text to display.
     * @param color   the semantic color of the message.
     */
    public void showStatus(String message, Color color) {
        this.statusText.setFill(color);
        this.statusText.setText(message);
    }

    /**
     * Clears any active status message.
     */
    public void clearStatus() {
        this.statusText.setText("");
    }
}