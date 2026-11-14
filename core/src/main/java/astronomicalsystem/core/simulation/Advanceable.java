package astronomicalsystem.core.simulation;

import astronomicalsystem.core.physics.TimeStep;
import org.jetbrains.annotations.NotNull;

/**
 * Something that evolves in time by discrete steps.
 */
public interface Advanceable {

    /**
     * Takes several steps, one after the other.
     *
     * @param steps precondition: positive
     */
    void advance(@NotNull TimeStep timeStep, int steps);
}
