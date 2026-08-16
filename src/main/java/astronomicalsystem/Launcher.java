package astronomicalsystem;

/**
 * The absolute starting point and initial ignition switch for the entire astronomical simulation.
 * 
 * This class acts as a highly specific architectural workaround necessary for modern Java environments. 
 * Starting with Java 11, if an application attempts to launch directly from a file that inherits from 
 * the standard JavaFX application structure, the Java Virtual Machine will frequently crash upon startup 
 * unless the user has perfectly configured complex module paths on their operating system. 
 * 
 * To completely bypass this frustrating limitation, this launcher class remains entirely independent 
 * from JavaFX. When the user runs the program, the Java Virtual Machine safely loads this simple class 
 * first without triggering any graphical errors. Once the system environment is securely established, this 
 * class then manually hands over control to the actual three dimensional simulation engine, allowing the 
 * application to run smoothly on any computer without requiring advanced technical setup from the user.
 */
public class Launcher {

    /**
     * A private constructor designed specifically to prevent this class from ever being created as a 
     * standalone object in memory. Because this class acts solely as a static startup trigger for the 
     * application, instantiating it serves no purpose and is therefore strictly prohibited to maintain 
     * a clean architecture.
     */
    private Launcher() {
        throw new UnsupportedOperationException("The launcher class cannot be instantiated.");
    }

    /**
     * The very first sequence of code executed by the computer the exact moment the user opens the application. 
     * 
     * It intercepts the initial startup command, collects any hidden configuration arguments the user 
     * might have passed through the operating system's command line, and immediately delegates the actual 
     * startup process to the primary graphical solar system application, bridging the gap between the 
     * operating system and the JavaFX visual engine.
     *
     * @param args an array of text arguments passed directly from the operating system command line, 
     *             which may contain specialized startup configurations, file paths, or testing parameters.
     */
    public static void main(String[] args) {
        SolarSystem3D.main(args);
    }
}