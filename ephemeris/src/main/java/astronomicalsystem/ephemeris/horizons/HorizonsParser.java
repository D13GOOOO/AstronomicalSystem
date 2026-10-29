package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.orbit.StateVector;
import org.jetbrains.annotations.NotNull;

/**
 * Reads the state vectors of a NASA JPL Horizons response, requested as plain text with
 * {@code CSV_FORMAT='YES'}, {@code OUT_UNITS='AU-D'} and {@code VEC_TABLE='2'}.
 */
public final class HorizonsParser {

    public @NotNull StateVector parse(@NotNull String response) {
        throw new UnsupportedOperationException("not yet implemented");
    }
}
