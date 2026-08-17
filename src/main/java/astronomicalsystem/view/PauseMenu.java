package astronomicalsystem.view;

import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A graphical user interface overlay that appears when the user pauses the simulation. 
 * It provides a central hub for managing the state of the application, offering interactive 
 * controls to resume the simulation, save the current progress to a file, load a previously 
 * saved session, or safely exit the program. 
 * 
 * Following strict design principles, this class only handles the visual presentation and 
 * user clicks, delegating all actual background tasks to the provided executable callbacks. 
 * This keeps the user interface completely unaware of how the physics engine or the file 
 * system actually operates.
 */
public class PauseMenu extends VBox {

    /**
     * The default visual styling rules applied to standard interactive buttons within this menu. 
     * It uses a deep space aesthetic with neon cyan accents and monospaced fonts to mimic a 
     * digital terminal.
     */
    private static final String BASE_BUTTON_STYLE = 
            "-fx-background-color: rgba(12, 18, 28, 0.5); -fx-border-color: rgba(0, 230, 255, 0.3); " +
            "-fx-text-fill: #00E6FF; -fx-font-family: 'Consolas'; -fx-font-size: 13px; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

    /**
     * The highlighted visual styling rules applied to a standard button when the user hovers 
     * their mouse cursor over it, providing immediate tactical feedback.
     */
    private static final String HOVER_BUTTON_STYLE = 
            "-fx-background-color: rgba(0, 230, 255, 0.15); -fx-border-color: #00E6FF; " +
            "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-font-weight: bold; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

    /**
     * A specialized, highly visible styling rule applied specifically to destructive or 
     * critical actions, such as shutting down the entire simulation.
     */
    private static final String WARNING_BUTTON_STYLE = 
            "-fx-background-color: rgba(255, 112, 67, 0.1); -fx-border-color: rgba(255, 112, 67, 0.5); " +
            "-fx-text-fill: #FF7043; -fx-font-family: 'Consolas'; -fx-font-size: 13px; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

    /**
     * The highlighted visual styling rules applied to warning buttons during mouse hover.
     */
    private static final String WARNING_HOVER_STYLE = 
            "-fx-background-color: rgba(255, 112, 67, 0.25); -fx-border-color: #FF7043; " +
            "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas'; -fx-font-size: 13px; -fx-font-weight: bold; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 8 15 8 15;";

    /**
     * A dedicated text element positioned at the bottom of the menu used to display temporary 
     * system messages to the user, such as confirming a successful save or reporting an error 
     * if a file could not be loaded.
     */
    private final Text statusText;

    /**
     * Constructs the entire pause menu overlay, building the vertical layout and wiring the 
     * buttons to their respective actions. It requires four specific actions to be provided by 
     * the main application, ensuring that the menu itself does not need to understand how saving, 
     * loading, or quitting actually works behind the scenes. It formats the container as a central 
     * "System Override" mainframe terminal.
     *
     * @param onResume the executable action to trigger when the user clicks the resume button, typically unpausing the physics engine and hiding this menu. Must not be null.
     * @param onSave   the executable action to trigger when the user clicks the save button, responsible for gathering the universe data and writing it to the disk. Must not be null.
     * @param onLoad   the executable action to trigger when the user clicks the load button, responsible for reading a save file and replacing the current universe state. Must not be null.
     * @param onQuit   the executable action to trigger when the user clicks the terminate button, cleanly shutting down the entire application. Must not be null.
     * @throws NullPointerException if any of the provided executable callbacks are missing.
     */
    public PauseMenu(Runnable onResume, Runnable onSave, Runnable onLoad, Runnable onQuit) {
        Objects.requireNonNull(onResume, "Resume callback cannot be null");
        Objects.requireNonNull(onSave, "Save callback cannot be null");
        Objects.requireNonNull(onLoad, "Load callback cannot be null");
        Objects.requireNonNull(onQuit, "Quit callback cannot be null");

        this.setAlignment(Pos.TOP_CENTER);
        this.setSpacing(15);
        this.setPrefWidth(300.0);
        this.setMaxWidth(300.0);
        this.setPadding(new Insets(30, 25, 30, 25));

        this.setStyle(
                "-fx-background-color: rgba(12, 18, 28, 0.90);" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: rgba(0, 230, 255, 0.5);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 4;"
        );

        Text targetHeader = new Text("SYSTEM OVERRIDE // STANDBY");
        targetHeader.setFill(Color.web("#FF7043"));
        targetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Text title = new Text("MAIN TERMINAL");
        title.setFill(Color.web("#FFFFFF"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 18));
        
        VBox headerBox = new VBox(2, targetHeader, title);
        headerBox.setAlignment(Pos.CENTER);
        VBox.setMargin(headerBox, new Insets(0, 0, 15, 0));

        Button btnResume = createStandardButton("RESUME SIMULATION", onResume);
        Button btnSave = createStandardButton("QUICK SAVE", onSave);
        Button btnLoad = createStandardButton("QUICK LOAD", onLoad);
        Button btnQuit = createWarningButton("TERMINATE SYSTEM", onQuit);

        this.statusText = new Text("");
        this.statusText.setFont(Font.font("Consolas", FontWeight.NORMAL, 12));
        VBox.setMargin(this.statusText, new Insets(10, 0, 0, 0));

        this.getChildren().addAll(headerBox, btnResume, btnSave, btnLoad, btnQuit, this.statusText);
        this.setVisible(false);
    }

    /**
     * An internal factory helper that generates a fully styled standard interactive button. 
     * It applies the neon cyan aesthetic and automatically handles hover interactions.
     *
     * @param text   the exact word or phrase that will be displayed on the face of the button.
     * @param action the specific behavior that should execute when the user clicks this button.
     * @return a fully assembled and interactive button ready to be added to the menu layout.
     */
    private Button createStandardButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPrefHeight(35);

        btn.setStyle(BASE_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(HOVER_BUTTON_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BASE_BUTTON_STYLE));
        
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * An internal factory helper that generates a highly visible warning button designed 
     * specifically for critical actions like shutting down the application.
     *
     * @param text   the exact word or phrase that will be displayed on the face of the button.
     * @param action the specific behavior that should execute when the user clicks this button.
     * @return a fully assembled and interactive critical action button.
     */
    private Button createWarningButton(String text, Runnable action) {
        Button btn = new Button(text);
        btn.setPrefWidth(260);
        btn.setPrefHeight(35);

        btn.setStyle(WARNING_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(WARNING_HOVER_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(WARNING_BUTTON_STYLE));
        
        btn.setOnAction(e -> action.run());
        return btn;
    }

    /**
     * Updates the text at the bottom of the menu to provide the user with a specific message. 
     * This is extremely useful for confirming that a background task, like saving a file, has 
     * completed successfully or failed.
     *
     * @param message the exact sentence to display to the user.
     * @param color   the color to paint the text, typically using green for success messages and red for errors.
     */
    public void showStatus(String message, Color color) {
        this.statusText.setFill(color);
        this.statusText.setText("> " + message);
    }

    /**
     * Erases any currently displayed status message from the bottom of the menu, returning that 
     * space to a blank state.
     */
    public void clearStatus() {
        this.statusText.setText("");
    }
}