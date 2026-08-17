package astronomicalsystem.engine;

import java.util.Objects;

import astronomicalsystem.model.AstronomicalSystem;

/**
 * The dedicated background executor responsible for driving the astronomical simulation 
 * forward in time. 
 * 
 * By implementing the {@link Runnable} interface, this engine completely decouples the heavy 
 * mathematical physics calculations from the graphical rendering pipeline. It operates on its 
 * own isolated Java Thread, running a strict "Fixed Time Step" loop. This guarantees that the 
 * physics engine calculates the universe consistently and deterministically, entirely immune 
 * to graphical lag, monitor refresh rates, or interface freezes.
 * 
 * Furthermore, this engine includes an advanced Time Warp algorithm. It intelligently balances 
 * CPU load by utilizing Sub-stepping and dynamic Delta Time computation, allowing the universe 
 * to be fast-forwarded at extreme speeds without breaking the integration math or overloading 
 * the processor.
 */
public class SimulationEngine implements Runnable {

    /**
     * The target number of times the physics loop should execute per second. 
     * Running at 60 Ticks Per Second (TPS) provides smooth gravitational tracking while 
     * leaving plenty of CPU resources available for rendering and background tasks.
     */
    private static final int TARGET_TPS = 60;
    
    /**
     * The absolute maximum number of sub-steps the engine is allowed to perform in a single tick.
     * This acts as a strict CPU safety valve. If the user accelerates time to 10,000x, the engine 
     * will not attempt 10,000 calculations per frame; it caps at this maximum limit and scales 
     * the Delta Time to compensate.
     */
    private static final int MAX_SUB_STEPS = 100;

    private final AstronomicalSystem system;

    private volatile boolean running;
    private volatile boolean paused;
    private volatile int timeWarpFactor;

    /**
     * Constructs the independent simulation engine and binds it to the core mathematical universe.
     *
     * @param system the core astronomical system containing all celestial bodies. Must not be null.
     */
    public SimulationEngine(AstronomicalSystem system) {
        this.system = Objects.requireNonNull(system, "Simulation engine requires a valid AstronomicalSystem");
        this.running = false;
        this.paused = false;
        this.timeWarpFactor = 1;
    }

    /**
     * The primary operational loop of the physics engine, executed continuously on a dedicated background thread.
     * 
     * This implements a highly precise "Accumulator Loop". It measures the exact nanoseconds that have 
     * passed in the real world and triggers physics updates only when a full fraction of a 60-TPS frame 
     * has elapsed. When the loop is waiting for time to pass, it yields the CPU, preventing the application 
     * from monopolizing the computer's resources.
     */
    @Override
    public void run() {
        this.running = true;

        long lastTime = System.nanoTime();
        double nsPerTick = 1_000_000_000.0 / TARGET_TPS;
        double unprocessedTime = 0.0;

        while (this.running) {
            long now = System.nanoTime();
            unprocessedTime += (now - lastTime) / nsPerTick;
            lastTime = now;

            while (unprocessedTime >= 1.0) {
                if (!this.paused) {
                    executePhysicsTick();
                }
                unprocessedTime -= 1.0;
            }

            try {
                Thread.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                this.running = false;
            }
        }
    }

    /**
     * Calculates the ideal distribution of computational steps and Delta Time, then orders the 
     * Astronomical System to advance the universe.
     * 
     * This is the mathematical core of the Time Warp mechanic. For low time-warp values (like 10x), 
     * it performs 10 distinct, highly accurate small steps (dt = 1.0). For extreme time-warp values 
     * (like 10,000x), it caps the calculations at 100 steps to save the CPU, but assigns a massive 
     * Delta Time (dt = 100.0) to each step to ensure the physical distance traveled is correct.
     */
    private void executePhysicsTick() {
        int stepsToCalculate = Math.min(this.timeWarpFactor, MAX_SUB_STEPS);
        double deltaTime = (double) this.timeWarpFactor / stepsToCalculate;

        this.system.simulate(stepsToCalculate, deltaTime);
    }

    /**
     * Commands the engine to cleanly terminate its primary loop. 
     * Once stopped, a thread cannot be restarted; a new engine instance must be created.
     */
    public void stopEngine() {
        this.running = false;
    }

    /**
     * Instantly freezes or resumes the gravitational calculations without stopping the background thread itself.
     *
     * @param paused true to freeze the universe, false to resume the flow of time.
     */
    public void setPaused(boolean paused) {
        this.paused = paused;
    }
    
    /**
     * Retrieves the current pause state of the engine.
     * 
     * @return true if the engine is currently frozen, false otherwise.
     */
    public boolean isPaused() {
        return this.paused;
    }

    /**
     * Dynamically alters the speed at which time passes in the simulation.
     *
     * @param factor the speed multiplier (e.g., 1 for real-time, 100 for fast-forward). Must be 1 or greater.
     */
    public void setTimeWarp(int factor) {
        this.timeWarpFactor = Math.max(1, factor);
    }
    
    /**
     * Retrieves the current speed multiplier applied to the simulation time.
     * 
     * @return the active time warp factor.
     */
    public int getTimeWarp() {
        return this.timeWarpFactor;
    }
}