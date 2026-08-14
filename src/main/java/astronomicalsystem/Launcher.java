package astronomicalsystem;

/**
 * The primary entry point for the application.
 * <p>
 * <b>Architecture Note (JavaFX Workaround):</b> This class intentionally does not extend 
 * {@code javafx.application.Application}. Starting from Java 11, launching a JavaFX Application 
 * class directly without having JavaFX components explicitly on the module path causes a runtime error.
 * By using this decoupled Launcher class as the main entry point, the JVM initializes successfully 
 * and subsequently loads the JavaFX runtime dynamically when delegating to {@link SolarSystem3D}.
 * </p>
 */
public class Launcher {

    /**
     * The main method that bootstraps the application.
     *
     * @param args command-line arguments passed to the application.
     */
    public static void main(String[] args) {
        SolarSystem3D.main(args);
    }
}