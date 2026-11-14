package astronomicalsystem.core.physics;

import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * Two bodies that met during a step, as anyone watching would see it.
 *
 * @param survivor the largest body left, which carries on the name of the larger of the two
 * @param site     unit vector from the centre of the larger body towards the point where it was hit
 * @param plume    unit vector of the direction in which the debris flies off the site
 * @param speed    at which they met, in AU per day
 * @param energy   kinetic energy of the impact in the frame of the centre of mass, (1/2) mu v², in
 *                 solar masses times AU squared per day squared
 */
public record Collision(@NotNull BodyName survivor, @NotNull Vector3 site, @NotNull Vector3 plume, double speed,
                        double energy) {
}
