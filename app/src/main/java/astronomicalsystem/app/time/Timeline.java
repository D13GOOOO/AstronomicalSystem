package astronomicalsystem.app.time;

/**
 * Lets simulated time flow as real time passes.
 */
public interface Timeline {

    /**
     * @param realSeconds real time elapsed since the previous call; precondition: non-negative and
     *                    finite
     */
    void elapse(double realSeconds);
}
