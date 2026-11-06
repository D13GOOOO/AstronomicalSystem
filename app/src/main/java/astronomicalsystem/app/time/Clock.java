package astronomicalsystem.app.time;

/**
 * Monotonic source of real time.
 */
public interface Clock {

    /**
     * @return seconds elapsed since an arbitrary origin, never decreasing
     */
    double seconds();
}
