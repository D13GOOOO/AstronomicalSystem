package astronomicalsystem.model;

import java.util.List;

/**
 * An interface that defines a clean, architectural bridge between the hidden mathematical physics engine 
 * and the visible components of the application, such as the graphical 3D renderer or the data telemetry panels.
 * 
 * It functions using the standard Observer design pattern. When the physics engine finishes calculating 
 * a single frame of movement for all planets, it broadcasts the results to any component that implements 
 * this interface. This guarantees that the mathematics engine never directly touches or controls the 
 * graphical interface, keeping the code modular and preventing the interface from slowing down the 
 * core gravitational calculations.
 */
@FunctionalInterface
public interface SimulationObserver {

    /**
     * The single action triggered by the physics engine the moment it completes a full cycle of 
     * mathematical calculations. The engine packages the current exact locations of all planets 
     * and hands them over to the listening observer so they can be drawn on the screen or analyzed.
     * 
     * Because this action happens inside the main loop of the physics engine, any code written inside 
     * this method must be incredibly fast. If an observer attempts to do something slow here, like saving 
     * a massive file to the hard drive, it will physically block the physics engine from moving to the 
     * next frame, causing the entire simulation to freeze and lag.
     *
     * @param step        the exact sequential frame number that the simulation just finished calculating.
     * @param bodies      a secured, read-only list containing all the celestial bodies currently existing 
     *                    in the simulation, ensuring the observer can read their positions but cannot 
     *                    accidentally delete or modify them.
     * @param totalEnergy a diagnostic measurement of the total energy within the system during this specific frame.
     */
    void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy);
}