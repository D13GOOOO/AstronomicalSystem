package astronomicalsystem.performance;

/**
 * An opt-in, low-overhead telemetry monitor for tracking simulation and rendering performance.
 * <p>
 * This profiler collects timing data in nanoseconds and periodically outputs aggregated 
 * statistics (averages and maximums) in milliseconds to the standard output.
 * It is designed to be completely bypassed when disabled, ensuring zero overhead 
 * during standard execution.
 * </p>
 * <p>
 * <b>Usage:</b> Enable via JVM arguments: {@code -Dastronomicalsystem.profile=true}.
 * </p>
 */
public class SimulationPerformanceMonitor {

    private final boolean enabled;
    private final int reportEveryFrames;

    private long frameCount;
    private long totalNanos;
    private long maxNanos;
    
    private long renderCount;
    private long totalRenderNanos;
    private long maxRenderNanos;

    /**
     * Initializes the performance monitor.
     *
     * @param enabled           whether telemetry collection and reporting are active.
     * @param reportEveryFrames the frequency of console reports (e.g., every 300 frames).
     */
    public SimulationPerformanceMonitor(boolean enabled, int reportEveryFrames) {
        this.enabled = enabled;
        // Failsafe to prevent division by zero or negative reporting intervals
        this.reportEveryFrames = Math.max(1, reportEveryFrames);
    }

    /**
     * Records the execution time of a single physics integration step.
     * <p>
     * If profiling is enabled, this method safely updates the running totals across threads 
     * and prints a formatted report to the console when the frame threshold is met.
     * </p>
     *
     * @param nanos the duration of the physics tick in nanoseconds.
     */
    public synchronized void recordFrame(long nanos) {
        if (!this.enabled) {
            return;
        }

        this.frameCount++;
        this.totalNanos += nanos;
        this.maxNanos = Math.max(this.maxNanos, nanos);

        if (this.frameCount % this.reportEveryFrames == 0) {
            System.out.printf(
                    "[PROFILE] frames=%d avg=%.3fms max=%.3fms renderAvg=%.3fms renderMax=%.3fms%n",
                    this.frameCount,
                    getAverageFrameMillis(),
                    getMaxFrameMillis(),
                    getAverageRenderMillis(),
                    getMaxRenderMillis()
            );
        }
    }

    /**
     * Records the execution time of a single JavaFX rendering pass.
     *
     * @param nanos the duration of the render update in nanoseconds.
     */
    public synchronized void recordRender(long nanos) {
        if (!this.enabled) {
            return;
        }

        this.renderCount++;
        this.totalRenderNanos += nanos;
        this.maxRenderNanos = Math.max(this.maxRenderNanos, nanos);
    }

    /**
     * Retrieves the total number of physics frames recorded.
     *
     * @return the total frame count.
     */
    public synchronized long getFrameCount() {
        return this.frameCount;
    }

    /**
     * Calculates the lifetime average duration of a physics frame in milliseconds.
     *
     * @return the average time in milliseconds, or 0.0 if no frames are recorded.
     */
    public synchronized double getAverageFrameMillis() {
        if (this.frameCount == 0) {
            return 0.0;
        }
        return (this.totalNanos / (double) this.frameCount) / 1_000_000.0;
    }

    /**
     * Retrieves the longest recorded physics frame duration.
     *
     * @return the maximum time in milliseconds.
     */
    public synchronized double getMaxFrameMillis() {
        return this.maxNanos / 1_000_000.0;
    }

    /**
     * Calculates the lifetime average duration of a rendering pass in milliseconds.
     *
     * @return the average time in milliseconds, or 0.0 if no renders are recorded.
     */
    public synchronized double getAverageRenderMillis() {
        if (this.renderCount == 0) {
            return 0.0;
        }
        return (this.totalRenderNanos / (double) this.renderCount) / 1_000_000.0;
    }

    /**
     * Retrieves the longest recorded rendering pass duration.
     *
     * @return the maximum time in milliseconds.
     */
    public synchronized double getMaxRenderMillis() {
        return this.maxRenderNanos / 1_000_000.0;
    }
}