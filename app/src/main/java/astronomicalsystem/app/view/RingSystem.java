package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Rings around a disc drawn as a sphere: flat bands around its centre, hidden by the sphere where
 * they pass behind it and dark where they pass through its shadow. The directions are given in the
 * frame of the image: x to the right, y up, z towards the viewer.
 *
 * @param centre          of the sphere
 * @param diameter        of the sphere, in pixels
 * @param axis            unit vector perpendicular to the bands
 * @param towardsTheLight unit vector from the centre towards the light that casts the shadow
 * @param bands           from the inside, not overlapping
 */
public record RingSystem(@NotNull ClipPoint centre, double diameter, @NotNull Vector3 axis,
                         @NotNull Vector3 towardsTheLight, @NotNull List<Band> bands) {

    /**
     * @param inner   radius of the inner edge, in radii of the sphere
     * @param outer   radius of the outer edge, in radii of the sphere
     * @param light   the light the band sends towards the viewer, out of the shadow
     * @param opacity how much of what lies behind the band it hides, from 0 to 1
     */
    public record Band(double inner, double outer, @NotNull Colour light, double opacity) {
    }
}
