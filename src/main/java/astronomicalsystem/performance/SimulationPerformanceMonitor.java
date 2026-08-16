package astronomicalsystem.performance;

/**
 * A dedicated performance tracking system designed to measure and report the execution speed 
 * of both the physics engine and the graphical rendering pipeline. 
 * 
 * This monitor operates on an opt-in basis, meaning it actively collects telemetry data only 
 * when explicitly enabled. When disabled, it immediately bypasses all internal logic to ensure 
 * absolutely zero computational overhead is added to the simulation. The collected timing data 
 * is internally managed in nanoseconds for extreme precision, but is automatically converted 
 * and aggregated into human readable milliseconds when outputting periodic reports to the standard 
 * system console.
 */
public class SimulationPerformanceMonitor {

    /**
     * A flag determining whether the performance monitor is actively recording telemetry data.
     */
    private final boolean enabled;

    /**
     * The exact number of simulation frames that must pass before the monitor prints a fresh 
     * performance report to the console.
     */
    private final int reportEveryFrames;

    /**
     * The total accumulated number of physics integration frames processed since the monitor was created.
     */
    private long frameCount;

    /**
     * The cumulative execution time of all recorded physics frames, measured in nanoseconds.
     */
    private long totalNanos;

    /**
     * The single longest duration recorded for a physics frame, indicating the worst case 
     * performance spike or lag in the mathematical calculations.
     */
    private long maxNanos;
    
    /**
     * The total accumulated number of graphical rendering passes processed since the monitor was created.
     */
    private long renderCount;

    /**
     * The cumulative execution time of all recorded rendering passes, measured in nanoseconds.
     */
    private long totalRenderNanos;

    /**
     * The single longest duration recorded for a graphical rendering pass, indicating the worst case 
     * visual stutter or frame drop.
     */
    private long maxRenderNanos;

    /**
     * Constructs a new performance monitor and configures its reporting behavior. A built in safety 
     * mechanism ensures that the reporting interval can never be zero or negative, preventing 
     * mathematical errors when calculating averages or determining when to print the next console report.
     *
     * @param enabled           true to activate telemetry collection and console reporting, false to 
     *                          completely disable the monitor and save processing power.
     * @param reportEveryFrames the specific number of frames to wait between each printed console report.
     */
    public SimulationPerformanceMonitor(boolean enabled, int reportEveryFrames) {
        this.enabled = enabled;
        this.reportEveryFrames = Math.max(1, reportEveryFrames);
    }

    /**
     * Logs the execution time of a single physics calculation cycle. 
     * 
     * If the monitor is currently disabled, this method immediately exits. If enabled, it safely adds 
     * the provided duration to the running totals and checks if a new maximum lag spike has occurred. 
     * When the total number of recorded frames reaches the predefined reporting threshold, this method 
     * automatically prints a comprehensive performance summary to the system console. This method is 
     * synchronized to guarantee thread safety when accessed by parallel physics processes.
     *
     * @param nanos the exact duration it took to calculate the current physics step, measured in nanoseconds.
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
     * Logs the execution time of a single graphical drawing cycle. 
     * 
     * Similar to the physics frame tracker, this method ignores the input if the monitor is disabled. 
     * When active, it updates the total accumulated rendering time and tracks the absolute longest rendering 
     * delay encountered so far. It is synchronized to prevent data corruption if the rendering thread and 
     * physics thread attempt to log data at the exact same moment.
     *
     * @param nanos the exact duration it took to draw the current visual frame on the screen, measured in nanoseconds.
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
     * Retrieves the total number of physics calculation cycles that have been successfully recorded 
     * by this monitor since it was initialized.
     *
     * @return the total physical frame count.
     */
    public synchronized long getFrameCount() {
        return this.frameCount;
    }

    /**
     * Calculates the historical average execution time of a single physics frame across the entire lifespan 
     * of the simulation. It safely divides the total accumulated time by the number of recorded frames, and 
     * then converts the resulting nanoseconds into milliseconds for easier reading. If no frames have been 
     * recorded yet, it safely returns zero to prevent division errors.
     *
     * @return the average physics calculation time in milliseconds, or zero if no data has been recorded.
     */
    public synchronized double getAverageFrameMillis() {
        if (this.frameCount == 0) {
            return 0.0;
        }
        return (this.totalNanos / (double) this.frameCount) / 1_000_000.0;
    }

    /**
     * Retrieves the absolute longest execution time recorded for any single physics frame. This is crucial 
     * for identifying severe lag spikes or bottlenecks that disrupt the smooth flow of the mathematical simulation. 
     * The value is converted from nanoseconds to milliseconds before being returned.
     *
     * @return the maximum recorded physics calculation time in milliseconds.
     */
    public synchronized double getMaxFrameMillis() {
        return this.maxNanos / 1_000_000.0;
    }

    /**
     * Calculates the historical average execution time of a single graphical rendering pass. It divides the 
     * total accumulated rendering time by the number of recorded drawing cycles, converting the result from 
     * nanoseconds into milliseconds. It includes a safeguard to return zero if no rendering passes have been logged.
     *
     * @return the average visual rendering time in milliseconds, or zero if no data has been recorded.
     */
    public synchronized double getAverageRenderMillis() {
        if (this.renderCount == 0) {
            return 0.0;
        }
        return (this.totalRenderNanos / (double) this.renderCount) / 1_000_000.0;
    }

    /**
     * Retrieves the absolute longest execution time recorded for a single graphical drawing pass. This metric 
     * helps identify severe visual stutters or moments where the graphics engine struggled to render the scene. 
     * The duration is converted from nanoseconds to milliseconds.
     *
     * @return the maximum recorded visual rendering time in milliseconds.
     */
    public synchronized double getMaxRenderMillis() {
        return this.maxRenderNanos / 1_000_000.0;
    }
}