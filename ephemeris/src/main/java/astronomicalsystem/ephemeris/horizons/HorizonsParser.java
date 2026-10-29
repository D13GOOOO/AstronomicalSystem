package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.EphemerisEntry;
import org.jetbrains.annotations.NotNull;

/**
 * Reads the state vectors of a NASA JPL Horizons response, requested as plain text with
 * {@code CSV_FORMAT='YES'}, {@code OUT_UNITS='AU-D'} and {@code VEC_TABLE='2'}.
 */
public final class HorizonsParser {

    private static final String START_OF_ENTRIES = "$$SOE";
    private static final String END_OF_ENTRIES = "$$EOE";
    private static final String SEPARATOR = ",";
    // Fields of a row: JDTDB, calendar date, X, Y, Z, VX, VY, VZ
    private static final int FIRST_POSITION_FIELD = 2;
    private static final int FIRST_VELOCITY_FIELD = 5;

    public @NotNull EphemerisEntry parse(@NotNull String response) {
        int start = response.indexOf(START_OF_ENTRIES) + START_OF_ENTRIES.length();
        int end = response.indexOf(END_OF_ENTRIES);
        String[] fields = response.substring(start, end).split(SEPARATOR);
        StateVector state =
                new StateVector(vectorFrom(fields, FIRST_POSITION_FIELD), vectorFrom(fields, FIRST_VELOCITY_FIELD));
        return new EphemerisEntry(new JulianDate(0.0), state);
    }

    private static @NotNull Vector3 vectorFrom(@NotNull String[] fields, int first) {
        return new Vector3(Double.parseDouble(fields[first]),
                Double.parseDouble(fields[first + 1]),
                Double.parseDouble(fields[first + 2]));
    }
}
