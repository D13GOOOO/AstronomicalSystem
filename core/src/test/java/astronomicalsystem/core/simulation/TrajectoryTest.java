package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TrajectoryTest {

    private static final BodyName EARTH = new BodyName("Earth");
    private static final Vector3 HERE = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 THERE = new Vector3(0.0, 1.0, 0.0);

    @Test
    void thePointsCannotBeChangedFromOutside() {
        List<Vector3> given = new ArrayList<>(List.of(HERE, THERE));
        Trajectory SUT = new Trajectory(EARTH, BodyKind.PLANET, given);

        given.clear();

        assertThat(SUT.points()).containsExactly(HERE, THERE);
        assertThatThrownBy(() -> SUT.points().add(HERE)).isInstanceOf(UnsupportedOperationException.class);
    }
}
