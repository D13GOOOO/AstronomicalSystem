package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The bodies of a system at a date, and how their positions were obtained.
 */
public record Ephemeris(@NotNull List<Body> bodies, @NotNull Provenance provenance) {

    public Ephemeris {
        bodies = List.copyOf(bodies);
    }
}
