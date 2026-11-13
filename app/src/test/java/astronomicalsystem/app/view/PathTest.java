package astronomicalsystem.app.view;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathTest {

    private static final ClipPoint HERE = new ClipPoint(0.0, 0.0, 0.0, 1.0);
    private static final ClipPoint THERE = new ClipPoint(0.5, 0.5, 0.0, 1.0);
    private static final Colour WHITE = new Colour(1.0, 1.0, 1.0);

    @Test
    void thePointsCannotBeChangedFromOutside() {
        List<ClipPoint> given = new ArrayList<>(List.of(HERE, THERE));
        Path SUT = new Path(given, WHITE);

        given.clear();

        assertThat(SUT.points()).containsExactly(HERE, THERE);
        assertThatThrownBy(() -> SUT.points().add(HERE)).isInstanceOf(UnsupportedOperationException.class);
    }
}
