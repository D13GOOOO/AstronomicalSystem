package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * System of bodies that new bodies can join.
 */
public interface Joinable {

    @NotNull List<Body> bodies();

    /**
     * @param body joins the system after the bodies already there
     * @throws IllegalArgumentException if a body with the same name is already in the system
     */
    void add(@NotNull Body body);
}
