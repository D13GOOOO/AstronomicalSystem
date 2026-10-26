package astronomicalsystem.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class SelectiveGravityTest {

    @Test
    void customBodiesApplyFullGravityWhileBasePlanetsAreDampened() {
        Planet basePlanet1 = new Planet("Earth", 100.0, 10.0, Point3D.ZERO);
        Planet basePlanet2 = new Planet("Mars", 100.0, 10.0, new Point3D(100.0, 0, 0));
        Planet customPlanet = new Planet("Custom Planet", 100.0, 10.0, new Point3D(100.0, 0, 0));

        // Test 1: Pianeta Base vs Pianeta Base (Gravità smorzata a 0.0001)
        Point3D dampenedAccel = Interactions.calculateAccelerationVector(
            basePlanet1.getPosition(), basePlanet1.getRadius(), basePlanet1, List.of(basePlanet1, basePlanet2)
        );

        // Test 2: Pianeta Base vs Pianeta Custom (Gravità pura 1.0)
        Point3D fullAccel = Interactions.calculateAccelerationVector(
            basePlanet1.getPosition(), basePlanet1.getRadius(), basePlanet1, List.of(basePlanet1, customPlanet)
        );

        double dampenedMagnitude = dampenedAccel.magnitude();
        double fullMagnitude = fullAccel.magnitude();

        // L'accelerazione causata dal pianeta custom deve essere drasticamente superiore
        assertTrue(fullMagnitude > dampenedMagnitude * 1000, 
            "I corpi Custom devono applicare la forza di gravità N-Body pura e non smorzata");
    }
}