package astronomicalsystem.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class AstronomicalSystemCollisionTest {

    @Test
    void simulateThermodynamicCollisionSpawnsDebris() {
        AstronomicalSystem system = new AstronomicalSystem();

        // Facciamo scontrare frontalmente due pianeti (Massa Totale = 6.0)
        system.addBody(new Planet("A", 2.0, 1.0, Point3D.ZERO));
        system.addBody(new Planet("B", 4.0, 1.0, Point3D.ZERO));

        system.simulate(1);

        List<CelestialBody> bodies = system.getBodies();
        
        // Verifichiamo che l'impatto abbia generato detriti multipli
        assertTrue(bodies.size() > 1, "Collision should spawn multiple debris fragments");
        
        CelestialBody mainCore = bodies.stream()
                .filter(b -> b.getName().contains("+"))
                .max((b1, b2) -> Double.compare(b1.getMass(), b2.getMass()))
                .orElseThrow(() -> new AssertionError("No merged core found"));
                
        // In una Total Annihilation, il nucleo trattiene l'85% della massa (6.0 * 0.85 = 5.1)
        assertEquals(5.1, mainCore.getMass(), 1e-9);
        
        // La somma di tutta la massa dell'universo (Nucleo + tutti i frammenti sparsi)
        // deve rimanere ESATTAMENTE 6.0 (Conservazione della massa termodinamica)
        double totalMass = bodies.stream().mapToDouble(CelestialBody::getMass).sum();
        assertEquals(6.0, totalMass, 1e-9);
    }

    @Test
    void anchoredCollisionPreservesStationaryRole() {
        AstronomicalSystem system = new AstronomicalSystem();

        system.addBody(new Star("Sun", 10.0, 5.0, Point3D.ZERO));
        system.addBody(new Planet("Planet", 2.0, 1.0, Point3D.ZERO));

        system.simulate(1);

        List<CelestialBody> bodies = system.getBodies();
        assertEquals(1, bodies.size());
        
        CelestialBody merged = bodies.get(0);
        assertInstanceOf(Star.class, merged);
        assertEquals(12.0, merged.getMass(), 1e-9);
        assertEquals("Sun+", merged.getName());
        assertEquals(Point3D.ZERO, merged.getPosition());
    }
}