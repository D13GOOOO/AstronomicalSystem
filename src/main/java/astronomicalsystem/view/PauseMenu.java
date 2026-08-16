package astronomicalsystem.view;

import java.util.Objects;

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
     * The default visual styling rules applied to all buttons within this menu. 
     * Keeping this as a shared constant prevents the system from creating duplicate text 
     * strings in memory for every single button, slightly improving performance and ensuring 
     * a consistent deep space aesthetic.
     */
    private static final String BASE_BUTTON_STYLE = 
            "-fx-background-color: transparent; -fx-border-color: rgba(200, 200, 200, 0.2); " +
            "-fx-text-fill: #CFD8DC; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
            "-fx-border-radius: 4; -fx-cursor: hand;";

    /**
     * The alternative visual styling rules applied to a button when the user hovers their 
     * mouse cursor over it. This provides immediate visual feedback, highlighting the button 
     * to indicate it is interactive and ready to be clicked.
     */
    private static final String HOVER_BUTTON_STYLE = 
            "-fx-background-color: rgba(255, 255, 255, 0.05); -fx-border-color: #CFD8DC; " +
            "-fx-text-fill: white; -fx-font-family: 'Segoe UI'; -fx-font-size: 12px; " +
            "-fx-border-radius: 4; -fx-cursor: hand;";

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
     * loading, or quitting actually works behind the scenes.
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
     * An internal factory helper that generates a fully styled interactive button. It applies 
     * the standard colors, sets the correct dimensions, and automatically handles the color changes 
     * when the user hovers their mouse over it.
     *
     * @param text   the exact word or phrase that will be displayed on the face of the button.
     * @param action the specific behavior that should execute when the user clicks this button.
     * @return a fully assembled and interactive button ready to be added to the menu layout.
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
     * Updates the text at the bottom of the menu to provide the user with a specific message. 
     * This is extremely useful for confirming that a background task, like saving a file, has 
     * completed successfully or failed.
     *
     * @param message the exact sentence to display to the user.
     * @param color   the color to paint the text, typically using green for success messages and red for errors.
     */
    public void showStatus(String message, Color color) {
        this.statusText.setFill(color);
        this.statusText.setText(message);
    }

    /**
     * Erases any currently displayed status message from the bottom of the menu, returning that 
     * space to a blank state.
     */
    public void clearStatus() {
        this.statusText.setText("");
    }
}