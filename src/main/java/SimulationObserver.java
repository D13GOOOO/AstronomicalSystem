import java.util.List;

public interface SimulationObserver {
    void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy);
}