package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.orbit.StateVector;
import astronomicalsystem.core.time.JulianDate;
import astronomicalsystem.ephemeris.EphemerisEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static java.util.function.Predicate.not;

/**
 * Reads the state vectors of a NASA JPL Horizons response, requested as plain text with
 * {@code CSV_FORMAT='YES'}, {@code OUT_UNITS='AU-D'} and {@code VEC_TABLE='2'}.
 */
public final class HorizonsParser {

    private static final String START_OF_ENTRIES = "$$SOE";
    private static final String END_OF_ENTRIES = "$$EOE";
    private static final String SEPARATOR = ",";
    // Fields of a row: JDTDB, calendar date, X, Y, Z, VX, VY, VZ
    private static final int EPOCH_FIELD = 0;
    private static final int FIRST_POSITION_FIELD = 2;
    private static final int FIRST_VELOCITY_FIELD = 5;

    public @NotNull EphemerisEntry parse(@NotNull String response) {
        String[] fields = onlyRowOf(entriesOf(response)).split(SEPARATOR);
        StateVector state =
                new StateVector(vectorFrom(fields, FIRST_POSITION_FIELD), vectorFrom(fields, FIRST_VELOCITY_FIELD));
        return new EphemerisEntry(new JulianDate(Double.parseDouble(fields[EPOCH_FIELD])), state);
    }

    private static @NotNull String entriesOf(@NotNull String response) {
        int startMarker = response.indexOf(START_OF_ENTRIES);
        int endMarker = response.indexOf(END_OF_ENTRIES);
        if (startMarker < 0 || endMarker < 0) {
            throw new IllegalArgumentException("Horizons response is missing the $$SOE/$$EOE markers");
        }
        return response.substring(startMarker + START_OF_ENTRIES.length(), endMarker);
    }

    private static @NotNull String onlyRowOf(@NotNull String entries) {
        List<String> rows = entries.lines().filter(not(String::isBlank)).toList();
        if (rows.size() != 1) {
            throw new IllegalArgumentException("Horizons response must contain exactly one ephemeris entry");
        }
        return rows.getFirst();
    }

    private static @NotNull Vector3 vectorFrom(@NotNull String[] fields, int first) {
        return new Vector3(Double.parseDouble(fields[first]),
                Double.parseDouble(fields[first + 1]),
                Double.parseDouble(fields[first + 2]));
    }
}
