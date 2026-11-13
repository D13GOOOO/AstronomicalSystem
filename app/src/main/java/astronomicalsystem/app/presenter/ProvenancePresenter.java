package astronomicalsystem.app.presenter;

import astronomicalsystem.app.view.TextView;
import astronomicalsystem.ephemeris.Provenance;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tells the user how the positions the simulation started from were obtained.
 */
public class ProvenancePresenter {

    private static final String INITIAL_POSITIONS = "Initial positions: ";

    public ProvenancePresenter(@NotNull TextView view, @NotNull Provenance provenance) {
        view.show(List.of(INITIAL_POSITIONS + describe(provenance)));
    }

    private static @NotNull String describe(@NotNull Provenance provenance) {
        return switch (provenance) {
            case JPL_HORIZONS -> "JPL Horizons";
            case KEPLER_ELEMENTS -> "Kepler orbital elements";
        };
    }
}
