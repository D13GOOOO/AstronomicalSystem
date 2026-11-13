package astronomicalsystem.ephemeris;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record Ephemeris(@NotNull List<Body> bodies, @NotNull Provenance provenance) {
}
