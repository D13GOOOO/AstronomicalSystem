package astronomicalsystem.model;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The core simulation engine responsible for orchestrating the physics loop,
 * managing celestial entities, and broadcasting state changes to observers.
 * <p>
 * This class leverages the {@link Kinematic} and {@link OrbitingBody} interfaces to safely 
 * segregate behaviors, ensuring strict compliance with the Liskov Substitution Principle (LSP).
 * </p>
 * <p>
 * <b>Performance Update:</b> The integration steps have been optimized to run sequentially 
 * in standard iterative loops. This eliminates the immense thread-context-switching overhead 
 * caused by parallel streams executing at 60 FPS. Diagnostic calculations (like Total Energy) 
 * have been bypassed in the main loop to relieve O(N^2) pressure on the CPU.
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
     * Observers are strictly view/controller layer components (e.g., JavaFX renderers) 
     * and must not be serialized to disk.
     */
    private transient List<SimulationObserver> observers;

    /**
     * Initializes a new, empty astronomical simulation system.
     */
    public AstronomicalSystem() {
        this.bodies = new CopyOnWriteArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Registers a celestial body within the simulation.
     *
     * @param body the celestial entity to add (must not be null).
     * @throws NullPointerException if the body is null.
     */
    public void addBody(CelestialBody body) {
        this.bodies.add(Objects.requireNonNull(body, "Cannot add a null celestial body"));
    }

    /**
     * Subscribes an observer to receive simulation step updates.
     * <p>
     * Reinitializes the transient observer list if the system was recently loaded from a file.
     * </p>
     *
     * @param observer the listener to register (must not be null).
     * @throws NullPointerException if the observer is null.
     */
    public void addObserver(SimulationObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        this.observers.add(Objects.requireNonNull(observer, "Cannot add a null observer"));
    }

    /**
     * Advances the simulation by a specified number of discrete integration steps.
     *
     * @param steps the number of time steps to simulate.
     */
    public void simulate(int steps) {
        for (int i = 1; i <= steps; i++) {
            tick();
        }
        notifyObservers(steps);
    }

    /**
     * Executes a single discrete integration step for the entire system.
     * <p>
     * <b>Execution Order:</b>
     * <ol>
     *   <li>Gravitational velocities are calculated for all {@link Kinematic} bodies.</li>
     *   <li>Positions are updated for all {@link Kinematic} bodies.</li>
     *   <li>Parametric orbits are advanced for {@link OrbitingBody} entities.</li>
     *   <li>Collisions and mergers are resolved iteratively until the system is spatially stable.</li>
     * </ol>
     * </p>
     */
    private void tick() {
        for (CelestialBody b : this.bodies) {
            if (b instanceof Kinematic k) {
                k.updateVelocity(this.bodies);
            }
        }

        for (CelestialBody b : this.bodies) {
            if (b instanceof Kinematic k) {
                k.updatePosition();
            }
        }

        for (CelestialBody b : this.bodies) {
            if (b instanceof OrbitingBody o) {
                o.updateOrbit();
            }
        }

        handleCollisions();
    }

    /**
     * Detects and resolves perfectly inelastic collisions between all entities in the system.
     * <p>
     * <b>Optimization:</b> This loop is iterative. If a merger happens, the lists are updated 
     * and the check runs again within the same tick until no bodies intersect. 
     * Uses atomic removeAll/addAll to maintain thread safety.
     * </p>
     */
    private void handleCollisions() {
        boolean mergedThisPass;

        do {
            mergedThisPass = false;
            List<CelestialBody> toAdd = new ArrayList<>();
            Set<CelestialBody> toRemove = new HashSet<>();

            int currentSize = this.bodies.size();
            for (int i = 0; i < currentSize; i++) {
                CelestialBody b1 = this.bodies.get(i);
                if (toRemove.contains(b1)) continue;

                for (int j = i + 1; j < currentSize; j++) {
                    CelestialBody b2 = this.bodies.get(j);
                    if (toRemove.contains(b2)) continue;

                    CelestialBody merged = Interactions.checkCollision(b1, b2);

                    if (merged != null) {
                        toAdd.add(merged);
                        toRemove.add(b1);
                        toRemove.add(b2);
                        mergedThisPass = true;
                        break;
                    }
                }

                if (mergedThisPass) {
                    break;
                }
            }

            if (mergedThisPass) {
                this.bodies.removeAll(toRemove);
                this.bodies.addAll(toAdd);
            }
        } while (mergedThisPass);
    }

    /**
     * Calculates the aggregate mechanical energy of the system.
     * <p>
     * The total energy is the sum of the Kinetic Energy of all dynamic bodies 
     * and the Gravitational Potential Energy between all pairs of bodies:
     * $$ E = \sum E_k + \sum_{i<j} \left( -G \frac{m_i m_j}{r_{ij}} \right) $$
     * </p>
     *
     * @return the total mechanical energy scalar.
     */
    public double calculateTotalEnergy() {
        double kinetic = this.bodies.parallelStream()
                .filter(b -> b instanceof Kinematic)
                .mapToDouble(b -> ((Kinematic) b).totalEnergy())
                .sum();

        double potential = 0.0;
        int size = this.bodies.size();
        for (int i = 0; i < size; i++) {
            CelestialBody b1 = this.bodies.get(i);
            for (int j = i + 1; j < size; j++) {
                CelestialBody b2 = this.bodies.get(j);
                double dist = b1.getPosition().distance(b2.getPosition());
                if (dist > 0) {
                    potential -= (Interactions.G * b1.getMass() * b2.getMass()) / dist;
                }
            }
        }

        return kinetic + potential;
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

        List<CelestialBody> safeList = Collections.unmodifiableList(this.bodies);

        for (SimulationObserver obs : this.observers) {
            obs.onSimulationStep(step, safeList, 0.0);
        }
    }

    /**
     * Serializes the current system state (legacy physical-only payload) to a persistent storage file.
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
            List<CelestialBody> loadedBodies = (List<CelestialBody>) ois.readObject();
            this.bodies = new CopyOnWriteArrayList<>(loadedBodies);
        }
    }
}