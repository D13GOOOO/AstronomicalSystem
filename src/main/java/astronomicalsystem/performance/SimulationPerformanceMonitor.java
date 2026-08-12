package astronomicalsystem.performance;

/**
 * Opt-in frame timing monitor for the simulation loop.
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

    public SimulationPerformanceMonitor(boolean enabled, int reportEveryFrames) {
        this.enabled = enabled;
        this.reportEveryFrames = Math.max(1, reportEveryFrames);
    }

    public void recordFrame(long nanos) {
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

    public void recordRender(long nanos) {
        if (!this.enabled) {
            return;
        }

        this.renderCount++;
        this.totalRenderNanos += nanos;
        this.maxRenderNanos = Math.max(this.maxRenderNanos, nanos);
    }

    public long getFrameCount() {
        return this.frameCount;
    }

    public double getAverageFrameMillis() {
        if (this.frameCount == 0) {
            return 0.0;
        }
        return (this.totalNanos / (double) this.frameCount) / 1_000_000.0;
    }

    public double getMaxFrameMillis() {
        return this.maxNanos / 1_000_000.0;
    }

    public double getAverageRenderMillis() {
        if (this.renderCount == 0) {
            return 0.0;
        }
        return (this.totalRenderNanos / (double) this.renderCount) / 1_000_000.0;
    }

    public double getMaxRenderMillis() {
        return this.maxRenderNanos / 1_000_000.0;
    }
}