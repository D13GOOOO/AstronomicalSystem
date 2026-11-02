package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FallbackEphemerisTest {

    private static final JulianDate DATE = new JulianDate(2461041.5);
    private static final List<Body> PRIMARY_BODIES = List.of(sunAt(new Vector3(1.0, 0.0, 0.0)));
    private static final List<Body> FALLBACK_BODIES = List.of(sunAt(new Vector3(2.0, 0.0, 0.0)));

    private final EphemerisSource primary = mock();
    private final EphemerisSource fallback = mock();
    private FallbackEphemeris SUT;

    @BeforeEach
    void setUp() {
        SUT = new FallbackEphemeris(primary, fallback);
    }

    @Test
    void theAnswerOfThePrimarySourceIsTakenWithoutAskingTheFallback() {
        when(primary.bodiesAt(DATE)).thenReturn(PRIMARY_BODIES);

        List<Body> bodies = SUT.bodiesAt(DATE);

        assertThat(bodies).isEqualTo(PRIMARY_BODIES);
        verifyNoInteractions(fallback);
    }

    @Test
    void theFallbackAnswersWhenThePrimarySourceIsUnavailable() {
        when(primary.bodiesAt(DATE)).thenThrow(new EphemerisUnavailableException("Horizons cannot be reached"));
        when(fallback.bodiesAt(DATE)).thenReturn(FALLBACK_BODIES);

        List<Body> bodies = SUT.bodiesAt(DATE);

        assertThat(bodies).isEqualTo(FALLBACK_BODIES);
    }

    private static Body sunAt(Vector3 position) {
        return new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3), position,
                Vector3.ZERO);
    }
}
