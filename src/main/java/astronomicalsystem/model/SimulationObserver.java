package astronomicalsystem.model;

import java.util.List;

/**
 * Defines a subscription contract for receiving periodic state updates from the
 * physics simulation engine.
 * <p>
 * Implementing classes act as observers within the Observer design pattern.
 * They hook into the main integration loop to update UI components, record
 * telemetry, or trigger domain events without tightly coupling the physics
 * engine to the presentation or logging layers.
 * </p>
 */
@FunctionalInterface
public interface SimulationObserver {

    /**
     * Invoked synchronously by the simulation engine at the conclusion of a discrete integration step.
     * <p>
     * Implementations should be cautious of executing long-running or blocking I/O tasks
     * within this method, as it executes sequentially within the primary physics loop.
     * </p>
     *
     * @param step        the current discrete time step or iteration index.
     * @param bodies      a defensively copied or unmodifiable list of the current celestial entities.
     * @param totalEnergy the calculated total kinetic energy of the system for this iteration.
     */
    void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy);
}