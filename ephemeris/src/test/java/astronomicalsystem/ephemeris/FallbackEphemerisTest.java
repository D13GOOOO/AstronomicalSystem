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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FallbackEphemerisTest {

    private static final JulianDate DATE = new JulianDate(2461041.5);
    private static final List<Body> PRIMARY_BODIES = List.of(sunAt(new Vector3(1.0, 0.0, 0.0)));
    private static final List<Body> FALLBACK_BODIES = List.of(sunAt(new Vector3(2.0, 0.0, 0.0)));
    private static final Ephemeris PRIMARY_ANSWER = new Ephemeris(PRIMARY_BODIES, Provenance.JPL_HORIZONS);
    private static final Ephemeris FALLBACK_ANSWER = new Ephemeris(FALLBACK_BODIES, Provenance.KEPLER_ELEMENTS);

    private final EphemerisSource primary = mock();
    private final EphemerisSource fallback = mock();
    private FallbackEphemeris SUT;

    @BeforeEach
    void setUp() {
        SUT = new FallbackEphemeris(primary, fallback);
    }

    @Test
    void theEphemerisOfThePrimarySourceIsTakenWithoutAskingTheFallback() {
        when(primary.ephemerisAt(DATE)).thenReturn(PRIMARY_ANSWER);

        Ephemeris ephemeris = SUT.ephemerisAt(DATE);

        assertThat(ephemeris).isEqualTo(PRIMARY_ANSWER);
        verifyNoInteractions(fallback);
    }

    // The provenance tells that the fallback answered
    @ParameterizedTest
    @MethodSource("failuresOfThePrimary")
    void theFallbackGivesTheEphemerisWhenThePrimarySourceFails(RuntimeException failure) {
        when(primary.ephemerisAt(DATE)).thenThrow(failure);
        when(fallback.ephemerisAt(DATE)).thenReturn(FALLBACK_ANSWER);

        Ephemeris ephemeris = SUT.ephemerisAt(DATE);

        assertThat(ephemeris).isEqualTo(FALLBACK_ANSWER);
    }

    @Test
    void aDefectOfThePrimarySourceIsNotHiddenByTheFallback() {
        IllegalStateException defect = new IllegalStateException("not yet managed");
        when(primary.ephemerisAt(DATE)).thenThrow(defect);

        assertThatThrownBy(() -> SUT.ephemerisAt(DATE)).isSameAs(defect);
        verifyNoInteractions(fallback);
    }

    static Stream<RuntimeException> failuresOfThePrimary() {
        return Stream.of(new EphemerisUnavailableException("Horizons cannot be reached"),
                new IllegalArgumentException("Horizons response is missing the $$SOE/$$EOE markers"));
    }

    private static Body sunAt(Vector3 position) {
        return new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(4.65e-3), position,
                Vector3.ZERO);
    }
}
