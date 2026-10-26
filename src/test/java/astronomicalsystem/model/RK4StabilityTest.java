package astronomicalsystem.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class RK4StabilityTest {

    @Test
    void totalMechanicalEnergyIsConservedOverTime() {
        AstronomicalSystem system = new AstronomicalSystem();

        // Sistema binario semplice per calcolare l'energia
        system.addBody(new Star("Sun", 10000.0, 50.0, Point3D.ZERO));
        Planet earth = new Planet("Earth", 50.0, 5.0, new Point3D(500.0, 0, 0));
        
        // Impostiamo una velocità iniziale per l'orbita
        earth.setVelocity(new Point3D(0, 10.0, 0));
        system.addBody(earth);

        double initialEnergy = system.calculateTotalEnergy();

        // Facciamo scorrere la simulazione per un tempo lungo
        system.simulate(5000, 1.0);

        double finalEnergy = system.calculateTotalEnergy();

        // L'energia deve rimanere invariata (tolleranza dello 0.1% per l'arrotondamento dei double)
        double tolerance = Math.abs(initialEnergy * 0.001);
        assertEquals(initialEnergy, finalEnergy, tolerance, "L'energia totale deve conservarsi grazie all'RK4");
    }
}