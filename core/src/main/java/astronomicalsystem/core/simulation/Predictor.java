package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Foresees the paths the bodies are going to follow.
 */
public interface Predictor {

    /**
     * @param bodies precondition: no two with the same name
     * @return the trajectory of every body, in the order of the bodies
     */
    @NotNull List<Trajectory> predict(@NotNull List<Body> bodies);
}
