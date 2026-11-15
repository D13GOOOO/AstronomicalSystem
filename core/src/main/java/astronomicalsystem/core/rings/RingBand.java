package astronomicalsystem.core.rings;

import astronomicalsystem.core.body.Radius;
import org.jetbrains.annotations.NotNull;

/**
 * Band of the rings of a planet: a flat, thin layer of small particles around it, between two
 * circles in the plane of its equator.
 *
 * @param inner        radius of the inner edge
 * @param outer        radius of the outer edge
 * @param opticalDepth how much of a ray of light across the band, perpendicular to it, the particles
 *                     stop: all but exp(-opticalDepth) of it
 */
public record RingBand(@NotNull Radius inner, @NotNull Radius outer, double opticalDepth) {

    /**
     * @param cosine of the angle between a ray of light and the perpendicular to the band, from 0
     *               for a ray along the band to 1; precondition: within these
     * @return the share of the ray that the band stops, from 0 to 1
     */
    public double opacity(double cosine) {
        return 0.0;
    }
}
