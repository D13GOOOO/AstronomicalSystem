package astronomicalsystem.model;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;

class FragmentGhostTest {

    @Test
    void fragmentsPhaseThroughEachOtherWithoutColliding() {
        // Due frammenti spawnati letteralmente uno dentro l'altro
        Fragment frag1 = new Fragment("Debris-1", 0.5, 2.0, Point3D.ZERO);
        Fragment frag2 = new Fragment("Debris-2", 0.5, 2.0, Point3D.ZERO);

        // Il motore delle collisioni deve ignorarli del tutto restituendo null
        List<CelestialBody> collisionResult = Interactions.checkCollision(frag1, frag2);

        assertNull(collisionResult, "I frammenti devono ignorarsi fisicamente per prevenire Fork Bomb e lag estremo");
    }
}