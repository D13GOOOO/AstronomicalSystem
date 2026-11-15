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
        // Beer-Lambert: the path through the layer is 1 / cosine times its thickness; along the
        // band the division gives infinity, and nothing gets through
        return 1.0 - Math.exp(-opticalDepth / cosine);
    }

    /**
     * How bright the band looks from the side the light falls on, when each ray is scattered once
     * and evenly in all directions.
     *
     * @param towardsViewer cosine of the angle between the line of sight and the perpendicular to
     *                      the band, from 0 to 1; precondition: within these
     * @param towardsLight  cosine of the angle between the rays of light and the perpendicular to
     *                      the band, from 0 to 1; precondition: within these
     * @return the brightness as a share, from 0 to 1, of the most that a band can send back
     */
    public double reflected(double towardsViewer, double towardsLight) {
        return 0.0;
    }
}
