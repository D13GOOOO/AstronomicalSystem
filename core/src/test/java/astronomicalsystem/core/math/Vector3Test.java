package astronomicalsystem.core.math;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Vector3Test {

    @Test
    void plusAddsEachComponent() {
        Vector3 SUT = new Vector3(1.0, 2.0, 3.0);

        Vector3 sum = SUT.plus(new Vector3(10.0, 20.0, 30.0));

        assertThat(sum).isEqualTo(new Vector3(11.0, 22.0, 33.0));
    }
}
