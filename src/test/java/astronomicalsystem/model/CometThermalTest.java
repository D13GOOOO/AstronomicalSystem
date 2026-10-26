package astronomicalsystem.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class CometThermalTest {

    @Test
    void cometTailActivatesOnlyWhenNearStar() {
        Star sun = new Star("Sun", 10000.0, 50.0, Point3D.ZERO);
        
        // Cometa nello spazio profondo
        Comet comet = new Comet("Halley", 1.0, 5.0, new Point3D(10000.0, 0, 0), 1.0);
        comet.updateThermalState(sun.getPosition());
        
        assertEquals(0.0, comet.getTailIntensity(), 1e-9, "Nello spazio profondo la cometa deve essere ghiacciata");

        // Spostiamo la cometa vicinissimo al Sole (es. 500 unità)
        comet.setPosition(new Point3D(500.0, 0, 0));
        comet.updateThermalState(sun.getPosition());
        
        assertTrue(comet.getTailIntensity() > 0.5, "Vicino alla stella, il ghiaccio deve sublimare creando la coda");
    }
}