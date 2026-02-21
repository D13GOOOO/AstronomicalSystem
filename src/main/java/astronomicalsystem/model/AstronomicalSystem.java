package astronomicalsystem.model;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The core simulation engine responsible for orchestrating the physics loop,
 * managing celestial entities, and broadcasting state changes to observers.
 * <p>
 * This class leverages the {@link Kinematic} interface to safely segregate
 * static bodies from dynamic ones, ensuring strict compliance with the Liskov
 * Substitution Principle (LSP) during the parallel execution of physics integration steps.
 * </p>
 */
public class AstronomicalSystem implements Serializable {

    /**
     * The serialization identifier ensuring version compatibility.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    private List<CelestialBody> bodies;

    /**
     * Observers are strictly view/controller layer components and must not be serialized.
     */
    private transient List<SimulationObserver> observers;

    /**
     * Initializes a new, empty astronomical simulation system.
     */
    public AstronomicalSystem() {
        this.bodies = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Registers a celestial body within the simulation.
     *
     * @param body the celestial entity to add.
     */
    public void addBody(CelestialBody body) {
        this.bodies.add(body);
    }

    /**
     * Subscribes an observer to receive simulation step updates.
     * <p>
     * Reinitializes the transient observer list if the system was recently loaded from a file.
     * </p>
     *
     * @param observer the listener to register.
     */
    public void addObserver(SimulationObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        this.observers.add(observer);
    }

    /**
     * Advances the simulation by a specified number of discrete integration steps.
     *
     * @param steps the number of time steps to simulate.
     */
    public void simulate(int steps) {
        notifyObservers(0);
        for (int i = 1; i <= steps; i++) {
            tick();
            notifyObservers(i);
        }
    }

    /**
     * Executes a single discrete integration step for the entire system.
     * <p>
     * Applies gravitational forces and updates positions exclusively for entities
     * implementing the {@link Kinematic} interface, utilizing thread-safe parallel streams
     * to maximize multicore CPU throughput.
     * </p>
     */
    private void tick() {
        // Step 1: Calculate new velocities based on gravity
        this.bodies.parallelStream()
                .filter(b -> b instanceof Kinematic)
                .map(b -> (Kinematic) b)
                .forEach(k -> k.updateVelocity(this.bodies));

        // Step 2: Apply velocities to spatial positions
        this.bodies.parallelStream()
                .filter(b -> b instanceof Kinematic)
                .map(b -> (Kinematic) b)
                .forEach(Kinematic::updatePosition);

        // Step 3: Resolve physical intersections
        handleCollisions();
    }

    /**
     * Detects and resolves perfectly inelastic collisions between all entities in the system.
     * <p>
     * Optimization: Utilizes a {@link HashSet} for the removal queue to ensure $O(1)$
     * time complexity for containment checks within the $O(N^2)$ spatial iteration loop.
     * </p>
     */
    private void handleCollisions() {
        List<CelestialBody> toAdd = new ArrayList<>();
        Set<CelestialBody> toRemove = new HashSet<>(); // Optimization: O(1) lookup

        for (int i = 0; i < this.bodies.size(); i++) {
            CelestialBody b1 = this.bodies.get(i);
            if (toRemove.contains(b1)) continue;

            for (int j = i + 1; j < this.bodies.size(); j++) {
                CelestialBody b2 = this.bodies.get(j);
                if (toRemove.contains(b2)) continue;

                CelestialBody merged = Interactions.checkCollision(b1, b2);

                if (merged != null) {
                    toAdd.add(merged);
                    toRemove.add(b1);
                    toRemove.add(b2);
                    break; // b1 is destroyed, break inner loop to evaluate next surviving body
                }
            }
        }

        if (!toRemove.isEmpty() || !toAdd.isEmpty()) {
            this.bodies.removeAll(toRemove);
            this.bodies.addAll(toAdd);
        }
    }

    /**
     * Calculates the aggregate kinetic energy of the system.
     *
     * @return the total energy scalar.
     */
    public double calculateTotalEnergy() {
        return this.bodies.parallelStream()
                .filter(b -> b instanceof Kinematic)
                .mapToDouble(b -> ((Kinematic) b).totalEnergy())
                .sum();
    }

    /**
     * Retrieves a defensive copy of the current celestial bodies to prevent external mutation.
     *
     * @return a new list containing the active entities.
     */
    public List<CelestialBody> getBodies() {
        return new ArrayList<>(this.bodies);
    }

    /**
     * Broadcasts the current immutable system state to all registered observers.
     *
     * @param step the current simulation iteration index.
     */
    private void notifyObservers(int step) {
        if (this.observers == null || this.observers.isEmpty()) return;

        double energy = calculateTotalEnergy();
        List<CelestialBody> safeList = Collections.unmodifiableList(this.bodies);

        for (SimulationObserver obs : this.observers) {
            obs.onSimulationStep(step, safeList, energy);
        }
    }

    /**
     * Serializes the current system state to a persistent storage file.
     *
     * @param filename the target file path.
     * @throws IOException if an I/O error occurs during serialization.
     */
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this.bodies);
        }
    }

    /**
     * Deserializes and restores the system state from a persistent storage file.
     *
     * @param filename the source file path.
     * @throws IOException            if an I/O error occurs during deserialization.
     * @throws ClassNotFoundException if the serialized class definition is missing.
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            this.bodies = (List<CelestialBody>) ois.readObject();
        }
    }
}