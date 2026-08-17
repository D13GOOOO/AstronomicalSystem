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
 * The central processing engine that manages the lifecycle of the entire astronomical simulation.
 * This class acts as the primary orchestrator, maintaining the master list of all celestial entities
 * and coordinating the execution of physical laws at each discrete time step.
 * 
 * It strictly separates physical calculations from graphical rendering. While it calculates the 
 * exact spatial coordinates and velocities of planets and stars, it does not draw them. Instead, 
 * it implements the Observer design pattern, broadcasting the updated universe state to any attached 
 * visualizers or telemetry monitors only after all physics calculations for a frame are complete.
 * 
 * To ensure high performance and prevent graphical lag during rapid time acceleration, the integration 
 * loop has been optimized to run sequentially. Thread-safe data structures are utilized to prevent 
 * concurrency issues when the engine updates the positions of bodies while the interface attempts 
 * to read them.
 */
public class AstronomicalSystem implements Serializable {

    /**
     * The unique version identifier used during binary serialization to verify that a saved file 
     * is compatible with the current version of the application.
     */
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * The thread-safe collection housing all active celestial entities currently existing within 
     * the simulated universe.
     */
    private List<CelestialBody> bodies;

    /**
     * The list of external components listening for state changes. This is marked as transient 
     * to prevent the serialization process from attempting to save graphical interface elements 
     * into the binary save file.
     */
    private transient List<SimulationObserver> observers;

    /**
     * Constructs a new, completely empty universe simulation engine, initializing the necessary 
     * thread-safe collections for storing bodies and observers.
     */
    public AstronomicalSystem() {
        this.bodies = new CopyOnWriteArrayList<>();
        this.observers = new ArrayList<>();
    }

    /**
     * Injects a newly created celestial entity into the active simulation environment.
     *
     * @param body the initialized planet, star, or moon to introduce into the system.
     * @throws NullPointerException if the provided entity reference is null.
     */
    public void addBody(CelestialBody body) {
        this.bodies.add(Objects.requireNonNull(body, "Cannot add a null celestial body"));
    }

    /**
     * Connects an external listener, typically a graphical rendering engine, to the simulation.
     * The attached observer will receive periodic updates containing the exact positions of all bodies.
     * If the simulation was recently restored from a save file, this method also ensures the transient 
     * listener list is safely reinitialized.
     *
     * @param observer the listener component to register.
     * @throws NullPointerException if the provided listener reference is null.
     */
    public void addObserver(SimulationObserver observer) {
        if (this.observers == null) {
            this.observers = new ArrayList<>();
        }
        this.observers.add(Objects.requireNonNull(observer, "Cannot add a null observer"));
    }

    /**
     * Drives the entire simulation forward in time by a specific number of computational cycles, 
     * assuming a standard default time slice of 1.0 unit per frame.
     * 
     * This is an overloaded convenience method primarily maintained for legacy code or simple tests 
     * that do not utilize advanced time-warping capabilities. For precise control over the simulation 
     * speed, use the parameterized version of this method.
     *
     * @param steps the exact number of discrete integration frames to compute before stopping.
     */
    public void simulate(int steps) {
        simulate(steps, 1.0);
    }

    /**
     * Drives the entire simulation forward in time, providing fine-grained control over both the 
     * number of computational cycles (sub-stepping) and the exact slice of time (Delta Time) simulated 
     * during each individual cycle.
     * 
     * To maximize performance and prevent the graphical interface from freezing during rapid execution, 
     * the physics calculations are performed silently in a rapid loop, and the graphical observers 
     * are notified only once the final future state has been reached. This allows the system to remain 
     * mathematically stable even when fast-forwarding the universe by dividing heavy gravitational 
     * calculations into numerous tiny, highly accurate time slices.
     *
     * @param steps the number of internal, silent calculations to perform before notifying the interface.
     * @param dt    the precise slice of time (Delta Time) that passes during each internal calculation step.
     */
    public void simulate(int steps, double dt) {
        for (int i = 1; i <= steps; i++) {
            tick(dt);
        }
        notifyObservers(steps);
    }

    /**
     * Executes the foundational Newtonian physics calculations for a single moment in time.
     * This method enforces a strict sequential order to maintain mathematical stability:
     * First, the gravitational pull between all moving bodies is calculated based on the given slice of time.
     * Second, those newly scaled velocities are applied to update their spatial coordinates.
     * Third, objects on rigid, fixed orbital paths (like moons) are advanced.
     * Finally, the system scans for and permanently resolves any overlapping physical intersections.
     * 
     * @param dt the Delta Time slice being simulated during this exact moment.
     */
    private void tick(double dt) {
        for (CelestialBody b : this.bodies) {
            if (b instanceof Kinematic k) {
                k.updateVelocity(this.bodies, dt);
            }
        }

        for (CelestialBody b : this.bodies) {
            if (b instanceof Kinematic k) {
                k.updatePosition(dt);
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
     * Scans the entire universe for overlapping physical boundaries and enforces perfectly inelastic mergers.
     * When two entities occupy the same space, they are immediately removed from the active collection 
     * and replaced by a single, larger composite entity. Because a multiple-body pileup could occur in a 
     * single frame, this logic loops continuously until the entire spatial environment is confirmed to be 
     * free of any intersecting objects.
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
     * Computes the total mechanical energy currently present within the simulated universe.
     * This metric combines the movement energy of every traveling object with the inherent gravitational 
     * tension existing between every pair of masses. Monitoring this absolute value over time provides 
     * a mathematical proof of the physics engine's stability and accuracy.
     *
     * @return a single scalar value representing the combined total energy of the system.
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
     * Provides external components with a safe, detached snapshot of the currently existing entities.
     *
     * @return a newly allocated list containing references to all active bodies.
     */
    public List<CelestialBody> getBodies() {
        return new ArrayList<>(this.bodies);
    }

    /**
     * Transmits the current, read-only configuration of the universe to all connected listeners.
     *
     * @param step the specific computational frame index currently being broadcast.
     */
    private void notifyObservers(int step) {
        if (this.observers == null || this.observers.isEmpty()) return;

        List<CelestialBody> safeList = Collections.unmodifiableList(this.bodies);

        for (SimulationObserver obs : this.observers) {
            obs.onSimulationStep(step, safeList, 0.0);
        }
    }

    /**
     * Packages the entire collection of active physical entities and writes them to a binary storage file.
     * This creates a permanent snapshot of the universe's mathematical state that can be restored later.
     *
     * @param filename the specific string path and name of the file to create or overwrite.
     * @throws IOException if the system encounters a hardware error while attempting to write the data to disk.
     */
    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this.bodies);
        }
    }

    /**
     * Reads a previously constructed binary storage file and reconstructs the mathematical state of the universe.
     * All existing entities currently in the simulation will be immediately overwritten by the loaded data.
     *
     * @param filename the specific string path and name of the file to open and read.
     * @throws IOException if the file cannot be located or accessed due to hardware or permission issues.
     * @throws ClassNotFoundException if the application version reading the file lacks the definitions for the saved objects.
     */
    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            List<CelestialBody> loadedBodies = (List<CelestialBody>) ois.readObject();
            this.bodies = new CopyOnWriteArrayList<>(loadedBodies);
        }
    }
}