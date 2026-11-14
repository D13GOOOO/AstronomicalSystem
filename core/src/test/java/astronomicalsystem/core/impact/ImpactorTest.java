package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Tolerances;
import astronomicalsystem.core.math.Vector3;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withinPercentage;

class ImpactorTest {

    private static final double TARGET_MASS = 2.0;
    private static final double TARGET_RADIUS = 0.5;
    private static final Body TARGET = new Body(new BodyName("Target"), BodyKind.PLANET, new Mass(TARGET_MASS),
            new Radius(TARGET_RADIUS), new Vector3(1.0, 2.0, 3.0), new Vector3(0.0, 0.25, 0.0));
    private static final Approach FROM_X_AIMING_TOWARDS_Y = new Approach(new Vector3(1.0, 0.0, 0.0),
            new Vector3(0.0, 1.0, 0.0));
    private static final BodyName IMPACTOR_NAME = new BodyName("Impactor 1");
    private static final double PRECISION_PERCENT = 1e-10;
    // 10 km/s in AU per day: 864 000 km a day over the kilometres in an AU
    private static final double TEN_KILOMETRES_PER_SECOND = 864_000.0 / 149_597_870.7;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = Tolerances.closeEnough(1e-12);

    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            ASTEROID,     1e-12
            PLANETESIMAL, 1e-3
            PROTOPLANET,  0.1
            """)
    void anImpactorHasAFractionOfTheMassOfItsTarget(Impactor SUT, double massRatio) {
        Body impactor = SUT.launchedAt(TARGET, FROM_X_AIMING_TOWARDS_Y, IMPACTOR_NAME);

        assertThat(impactor.mass().solarMasses())
                .isCloseTo(massRatio * TARGET_MASS, withinPercentage(PRECISION_PERCENT));
    }

    // A protoplanet is as large as a planet already
    @ParameterizedTest(name = "{0}")
    @CsvSource(textBlock = """
            ASTEROID,     ASTEROID
            PLANETESIMAL, ASTEROID
            PROTOPLANET,  PLANET
            """)
    void anImpactorHasTheNameGivenAndAKindForItsSize(Impactor SUT, BodyKind kind) {
        Body impactor = SUT.launchedAt(TARGET, FROM_X_AIMING_TOWARDS_Y, IMPACTOR_NAME);

        assertThat(List.of(impactor.name(), impactor.kind())).containsExactly(IMPACTOR_NAME, kind);
    }

    // Mass over the cube of the radius, proportional to the density
    @ParameterizedTest
    @EnumSource(Impactor.class)
    void anImpactorHasTheDensityOfItsTarget(Impactor SUT) {
        Body impactor = SUT.launchedAt(TARGET, FROM_X_AIMING_TOWARDS_Y, IMPACTOR_NAME);

        assertThat(impactor.mass().solarMasses() / cubed(impactor.radius().astronomicalUnits()))
                .isCloseTo(TARGET_MASS / cubed(TARGET_RADIUS), withinPercentage(PRECISION_PERCENT));
    }

    // Twenty radii of the target away along from, half a radius aside, moving towards the target at
    // 10 km/s relative to it
    @Test
    void anImpactorStartsFarFromItsTargetAndMovesTowardsIt() {
        Body impactor = Impactor.ASTEROID.launchedAt(TARGET, FROM_X_AIMING_TOWARDS_Y, IMPACTOR_NAME);

        assertThat(List.of(impactor.position(), impactor.velocity())).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(List.of(new Vector3(11.0, 2.25, 3.0),
                        new Vector3(-TEN_KILOMETRES_PER_SECOND, 0.25, 0.0)));
    }

    private static double cubed(double length) {
        return length * length * length;
    }
}
