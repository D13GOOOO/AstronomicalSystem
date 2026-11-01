package astronomicalsystem.ephemeris.horizons;

import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.net.URI;

/**
 * Request to NASA JPL Horizons for the barycentric state vector of one body at one epoch.
 *
 * @param command the Horizons identifier of the body (e.g. {@code 399} for Earth)
 * @param epoch   the instant of the state vector
 */
public record HorizonsQuery(@NotNull String command, @NotNull JulianDate epoch) {

    private static final String ENDPOINT = "https://ssd.jpl.nasa.gov/api/horizons.api?format=text";

    /**
     * Position and velocity relative to the solar-system barycentre, ecliptic J2000 frame, AU and
     * AU/day, one CSV row for the epoch.
     */
    public @NotNull URI uri() {
        return URI.create(ENDPOINT
                + parameter("COMMAND", command)
                + parameter("OBJ_DATA", "NO")
                + parameter("MAKE_EPHEM", "YES")
                + parameter("EPHEM_TYPE", "VECTORS")
                + parameter("CENTER", "@0")
                + parameter("TLIST", plainDecimal(epoch.days()))
                + parameter("REF_PLANE", "ECLIPTIC")
                + parameter("OUT_UNITS", "AU-D")
                + parameter("CSV_FORMAT", "YES")
                + parameter("VEC_TABLE", "2"));
    }

    // Double.toString switches to scientific notation, which Horizons does not read as a date
    private static @NotNull String plainDecimal(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    // Horizons wants every value between single quotes, here percent-encoded
    private static @NotNull String parameter(@NotNull String name, @NotNull String value) {
        return "&" + name + "=%27" + value + "%27";
    }
}
