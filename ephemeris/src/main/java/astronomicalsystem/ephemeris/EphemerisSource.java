package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.time.JulianDate;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Where the bodies of a system are at a given date, relative to the solar-system barycentre in
 * the ecliptic J2000 frame.
 */
public interface EphemerisSource {

    /**
     * @throws EphemerisUnavailableException if the source cannot be reached or does not answer
     * @throws IllegalArgumentException      if the source answers with something that is not an
     *                                       ephemeris
     */
    @NotNull List<Body> bodiesAt(@NotNull JulianDate date);

    @NotNull Ephemeris ephemerisAt(@NotNull JulianDate date);
}
