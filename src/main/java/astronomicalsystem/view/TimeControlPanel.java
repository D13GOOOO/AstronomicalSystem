package astronomicalsystem.view;

import java.util.Objects;
import java.util.function.IntConsumer;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

/**
 * A dedicated graphical user interface component designed specifically to allow the user to manipulate 
 * the flow of time within the simulation.
 * 
 * Operating strictly as a View component within the Model View Controller architecture, this horizontal 
 * control panel does not contain any physics logic or internal state tracking for the time warp. It simply 
 * displays a series of interactive speed buttons and blindly passes the user's selected speed multiplier 
 * back to the core application controller using a provided callback function. This guarantees that the 
 * user interface remains completely decoupled from the background mathematical engine.
 */
public class TimeControlPanel extends HBox {

    private static final String BASE_BUTTON_STYLE = 
            "-fx-background-color: rgba(12, 18, 28, 0.5); -fx-border-color: rgba(0, 230, 255, 0.3); " +
            "-fx-text-fill: #7EA6E0; -fx-font-family: 'Consolas'; -fx-font-size: 12px; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 6 12 6 12;";
            
    private static final String HOVER_BUTTON_STYLE = 
            "-fx-background-color: rgba(0, 230, 255, 0.15); -fx-border-color: #00E6FF; " +
            "-fx-text-fill: #FFFFFF; -fx-font-family: 'Consolas'; -fx-font-size: 12px; -fx-font-weight: bold; " +
            "-fx-border-radius: 2; -fx-cursor: hand; -fx-padding: 6 12 6 12;";

    /**
     * Constructs the horizontal time control panel, configuring all internal buttons, text labels, 
     * and visual styling to match the aerospace HUD aesthetic of the application.
     * 
     * The panel automatically aligns itself horizontally and applies a compact padding. It utilizes 
     * a glassmorphism background with neon accents, acting as a tactical mission clock anchored to 
     * the bottom of the screen.
     *
     * @param onSpeedSelected the functional callback that will receive the numerical multiplier chosen 
     *                        by the user when they click a speed button. Must not be null.
     * @throws NullPointerException if the provided callback function is missing or uninitialized.
     */
    public TimeControlPanel(IntConsumer onSpeedSelected) {
        Objects.requireNonNull(onSpeedSelected, "Speed selection callback cannot be null");

        this.setAlignment(Pos.CENTER);
        this.setSpacing(15);
        this.setPadding(new Insets(12, 25, 12, 25));
        this.setMaxHeight(60);
        this.setMaxWidth(USE_PREF_SIZE); 

        this.setStyle(
                "-fx-background-color: rgba(12, 18, 28, 0.85);" +
                "-fx-background-radius: 4;" +
                "-fx-border-color: rgba(0, 230, 255, 0.5) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05) rgba(255, 255, 255, 0.05);" +
                "-fx-border-width: 2 1 1 1;" +
                "-fx-border-radius: 4;"
        );

        Text targetHeader = new Text("CHRONOS // SYS.TIME");
        targetHeader.setFill(Color.web("#FF7043"));
        targetHeader.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));

        Text title = new Text("TEMPORAL WARP");
        title.setFill(Color.web("#FFFFFF"));
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        
        VBox headerBox = new VBox(2, targetHeader, title);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setMargin(headerBox, new Insets(0, 15, 0, 0));

        Button btn1x = createSpeedButton("1x", 1, onSpeedSelected);
        Button btn10x = createSpeedButton("10x", 10, onSpeedSelected);
        Button btn100x = createSpeedButton("100x", 100, onSpeedSelected);
        Button btn1000x = createSpeedButton("1,000x", 1000, onSpeedSelected);
        Button btnExtreme = createSpeedButton("10,000x", 10000, onSpeedSelected);

        this.getChildren().addAll(headerBox, btn1x, btn10x, btn100x, btn1000x, btnExtreme);
    }

    /**
     * An internal factory helper that generates standard JavaFX buttons specifically styled for the 
     * time control dashboard. It ensures every speed button behaves identically and changes color 
     * smoothly when the user hovers their mouse over it.
     *
     * @param labelText   the exact word or phrase that will be displayed on the face of the button.
     * @param speedFactor the mathematical multiplier representing how fast time should flow when this button is clicked.
     * @param action      the callback function to execute, passing the speed factor back to the main application.
     * @return a fully styled and interactive button ready to be added to the horizontal layout.
     */
    private Button createSpeedButton(String labelText, int speedFactor, IntConsumer action) {
        Button btn = new Button(labelText);
        btn.setPrefWidth(85);

        btn.setStyle(BASE_BUTTON_STYLE);
        btn.setOnMouseEntered(e -> btn.setStyle(HOVER_BUTTON_STYLE));
        btn.setOnMouseExited(e -> btn.setStyle(BASE_BUTTON_STYLE));
        
        btn.setOnAction(e -> action.accept(speedFactor));
        
        return btn;
    }
}