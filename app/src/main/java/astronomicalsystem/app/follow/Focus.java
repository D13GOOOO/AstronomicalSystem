package astronomicalsystem.app.follow;

import astronomicalsystem.core.body.BodyName;
import org.jetbrains.annotations.NotNull;

/**
 * What the camera follows: the barycentre of the solar system or one of its bodies.
 */
public sealed interface Focus permits Focus.Barycentre, Focus.OnBody {

    Focus BARYCENTRE = new Barycentre();

    /**
     * The origin of the frame of the simulation.
     */
    record Barycentre() implements Focus {
    }

    record OnBody(@NotNull BodyName name) implements Focus {
    }
}
