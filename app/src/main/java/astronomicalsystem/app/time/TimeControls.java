package astronomicalsystem.app.time;

/**
 * Operations the user can perform on the flow of simulated time.
 */
public interface TimeControls {

    /**
     * Doubles the time warp, unless it would become faster than the fastest one allowed.
     */
    void speedUp();

    /**
     * Halves the time warp, unless it would become slower than the slowest one allowed.
     */
    void slowDown();

    /**
     * Stops simulated time, or lets it flow again at the same warp.
     */
    void togglePause();
}
