package astronomicalsystem.core.body;

import astronomicalsystem.core.math.Quantities;
import org.jetbrains.annotations.NotNull;

/**
 * Radius of a body, in astronomical units.
 */
public record Radius(double astronomicalUnits) {

    public Radius {
        Quantities.requirePositiveFinite(astronomicalUnits, "Radius");
    }

    /**
     * @return the radius of a sphere whose volume is the sum of the volumes of the two spheres
     */
    public @NotNull Radius withVolumeAdded(@NotNull Radius other) {
        return new Radius(Math.cbrt(cubed() + other.cubed()));
    }

    private double cubed() {
        return astronomicalUnits * astronomicalUnits * astronomicalUnits;
    }
}
