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

    @Test
    void minusSubtractsEachComponent() {
        Vector3 SUT = new Vector3(11.0, 22.0, 33.0);

        Vector3 difference = SUT.minus(new Vector3(1.0, 2.0, 3.0));

        assertThat(difference).isEqualTo(new Vector3(10.0, 20.0, 30.0));
    }

    @Test
    void timesScalesEachComponent() {
        Vector3 SUT = new Vector3(1.0, -2.0, 3.0);

        Vector3 scaled = SUT.times(2.5);

        assertThat(scaled).isEqualTo(new Vector3(2.5, -5.0, 7.5));
    }

    @Test
    void dotSumsProductsOfComponents() {
        Vector3 SUT = new Vector3(1.0, 2.0, 3.0);

        double product = SUT.dot(new Vector3(4.0, -5.0, 6.0));

        assertThat(product).isEqualTo(12.0);
    }

    @Test
    void lengthSquaredSumsSquaresOfComponents() {
        Vector3 SUT = new Vector3(2.0, -3.0, 6.0);

        double lengthSquared = SUT.lengthSquared();

        assertThat(lengthSquared).isEqualTo(49.0);
    }

    @Test
    void lengthIsEuclideanNorm() {
        Vector3 SUT = new Vector3(2.0, -3.0, 6.0);

        double length = SUT.length();

        assertThat(length).isEqualTo(7.0);
    }
}
