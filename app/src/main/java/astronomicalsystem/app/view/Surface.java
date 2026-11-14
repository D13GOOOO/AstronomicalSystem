package astronomicalsystem.app.view;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * What the surface of a disc drawn as a sphere looks like.
 */
public sealed interface Surface permits Surface.Plain, Surface.Mapped {

    Surface PLAIN = new Plain();

    /**
     * The colour of the disc all over.
     */
    record Plain() implements Surface {
    }

    /**
     * A map of the whole surface wrapped around the sphere: an image twice as wide as tall, with
     * the longitudes from 180 degrees west to 180 degrees east across it and the north at the top.
     * The axes of the body are given in the frame of the image (x to the right, y up, z towards
     * the viewer), as unit vectors perpendicular to each other.
     *
     * @param texture name of the map, as the view was given it; a view without it shows the colour
     *                of the disc
     * @param x       from the centre towards the equator on the prime meridian, longitude 0
     * @param y       from the centre towards the equator at longitude 90 degrees east
     * @param z       from the centre towards the north pole
     */
    record Mapped(@NotNull String texture, @NotNull Vector3 x, @NotNull Vector3 y, @NotNull Vector3 z)
            implements Surface {
    }
}
