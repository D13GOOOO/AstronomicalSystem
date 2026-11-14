package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

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

    // Mass over the cube of the radius, proportional to the density
    @ParameterizedTest
    @EnumSource(Impactor.class)
    void anImpactorHasTheDensityOfItsTarget(Impactor SUT) {
        Body impactor = SUT.launchedAt(TARGET, FROM_X_AIMING_TOWARDS_Y, IMPACTOR_NAME);

        assertThat(impactor.mass().solarMasses() / cubed(impactor.radius().astronomicalUnits()))
                .isCloseTo(TARGET_MASS / cubed(TARGET_RADIUS), withinPercentage(PRECISION_PERCENT));
    }

    private static double cubed(double length) {
        return length * length * length;
    }
}
