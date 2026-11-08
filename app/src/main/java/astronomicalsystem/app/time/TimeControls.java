package astronomicalsystem.app.time;

/**
 * Operations the user can perform on the flow of simulated time.
 */
public interface TimeControls {

    /**
     * Doubles the time warp.
     */
    void speedUp();

    /**
     * Halves the time warp.
     */
    void slowDown();

    /**
     * Stops simulated time, or lets it flow again at the same warp.
     */
    void togglePause();
}
