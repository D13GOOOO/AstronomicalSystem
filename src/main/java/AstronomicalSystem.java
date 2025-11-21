import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.io.*;

public class AstronomicalSystem implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private List<CelestialBody> bodies;
    private transient List<SimulationObserver> observers;

    public AstronomicalSystem() {
        this.bodies = new ArrayList<>();
        this.observers = new ArrayList<>();
    }

    public void addBody(CelestialBody body) {
        bodies.add(body);
    }

    public void addObserver(SimulationObserver observer) {
        if (this.observers == null) this.observers = new ArrayList<>();
        this.observers.add(observer);
    }

    public void simulate(int steps) {
        notifyObservers(0);
        for (int i = 1; i <= steps; i++) {
            tick();
            notifyObservers(i);
        }
    }

    private void tick() {
        bodies.parallelStream().forEach(b -> b.updateVelocity(bodies));
        bodies.parallelStream().forEach(CelestialBody::updatePosition);
        handleCollisions();
    }

    private void handleCollisions() {
        List<CelestialBody> toAdd = new ArrayList<>();
        List<CelestialBody> toRemove = new ArrayList<>();

        for (int i = 0; i < bodies.size(); i++) {
            CelestialBody b1 = bodies.get(i);
            if (toRemove.contains(b1)) continue;

            for (int j = i + 1; j < bodies.size(); j++) {
                CelestialBody b2 = bodies.get(j);
                if (toRemove.contains(b2)) continue;

                // DELEGA A INTERACTIONS
                CelestialBody merged = Interactions.checkCollision(b1, b2);

                if (merged != null) {
                    toAdd.add(merged);
                    toRemove.add(b1);
                    toRemove.add(b2);
                    break;
                }
            }
        }
        bodies.removeAll(toRemove);
        bodies.addAll(toAdd);
    }

    public double calculateTotalEnergy() {
        return bodies.parallelStream().mapToDouble(CelestialBody::totalEnergy).sum();
    }

    public List<CelestialBody> getBodies() {
        return new ArrayList<>(this.bodies);
    }

    private void notifyObservers(int step) {
        if (observers == null) return;
        double energy = calculateTotalEnergy();
        List<CelestialBody> safeList = Collections.unmodifiableList(bodies);
        for (SimulationObserver obs : observers) {
            obs.onSimulationStep(step, safeList, energy);
        }
    }

    public void saveToFile(String filename) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(this.bodies);
        }
    }

    @SuppressWarnings("unchecked")
    public void loadFromFile(String filename) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            this.bodies = (List<CelestialBody>) ois.readObject();
        }
    }
}