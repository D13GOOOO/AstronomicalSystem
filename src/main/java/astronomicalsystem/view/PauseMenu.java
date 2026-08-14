package astronomicalsystem.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.Objects;

/**
 * A graphical overlay menu providing simulation control and state management.
 * <p>
 * Follows a minimalist deep-space aesthetic. Provides interactive buttons
 * for resuming, quick-saving, quick-loading, and terminating the application.
 * This class operates strictly as a View component, delegating all logical 
 * operations to the injected {@link Runnable} callbacks.
 * </p>
 */
public class PauseMenu extends VBox {

    /**
     * Shared CSS style for the default button state. 
     * Extracted as a constant to prevent redundant string allocations per button.
     */
    private static final String BASE_BUTTON_STYLE = 
            "-fx-background-color: transparent; -fx-border-color: rgba(200, 200, 200, 0.2); " +
            "-fx-text-fill: #CFD8DC; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
            "-fx-border-radius: 4; -fx-cursor: hand;";

    /**
     * Shared CSS style for the hover button state.
     */
    private static final String HOVER_BUTTON_STYLE = 
            "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: #CFD8DC; " +
            "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
            "-fx-border-radius: 4; -fx-cursor: hand;";

    private final Text statusText;

    /**
     * Constructs the pause menu overlay with interactive action bindings.
     * <p>
     * <b>Note:</b> The provided callbacks will be executed synchronously on the 
     * JavaFX Application Thread.
     * </p>
     *
     * @param onResume the callback triggered to close the menu and unpause (must not be null).
     * @param onSave   the callback triggered to serialize the simulation state (must not be null).
     * @param onLoad   the callback triggered to deserialize the simulation state (must not be null).
     * @param onQuit   the callback triggered to terminate the JVM (must not be null).
     * @throws NullPointerException if any of the provided callbacks are null.
     */
    public PauseMenu(Runnable onResume, Runnable onSave, Runnable onLoad, Runnable onQuit) {
        Objects.requireNonNull(onResume, "Resume callback cannot be null");
        Objects.requireNonNull(onSave, "Save callback cannot be null");
        Objects.requireNonNull(onLoad, "Load callback cannot be null");
        Objects.requireNonNull(onQuit, "Quit callback cannot be null");

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
     * @return a styled JavaFX {@link Button}.
     */
    private Button createButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(220);
        btn.setPrefHeight(35);

        btn.setStyle(BASE_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(HOVER_BUTTON_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BASE_BUTTON_STYLE));
        
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Displays transient visual feedback for user actions (e.g., "Save successful").
     *
     * @param message the text to display.
     * @param color   the semantic color of the message (e.g., Green for success, Red for error).
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