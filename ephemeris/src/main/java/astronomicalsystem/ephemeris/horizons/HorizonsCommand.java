package astronomicalsystem.ephemeris.horizons;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

/**
 * The Horizons identifier of a body (e.g. {@code 399} for Earth), the value of the
 * {@code COMMAND} parameter of a query.
 */
public record HorizonsCommand(@NotNull String value) {

    // SPK identifiers: positive for natural bodies, negative for spacecraft
    private static final Pattern BODY_IDENTIFIER = Pattern.compile("-?\\d+");

    public HorizonsCommand {
        if (!BODY_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException("Horizons command must be a numeric body identifier");
        }
    }
}
