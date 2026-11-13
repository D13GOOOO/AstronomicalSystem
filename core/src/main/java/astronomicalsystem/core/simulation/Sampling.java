package astronomicalsystem.core.simulation;

/**
 * How a predicted trajectory is sampled: its first point is the present position, then one point
 * every {@code stepsPerPoint} integration steps.
 *
 * @param stepsPerPoint at least one
 * @param points        at least two, so that the trajectory is a line
 */
public record Sampling(int stepsPerPoint, int points) {

    public Sampling {
        if (stepsPerPoint < 1) {
            throw new IllegalArgumentException("Steps per point must be positive");
        }
        if (points < 2) {
            throw new IllegalArgumentException("A trajectory needs at least two points");
        }
    }
}
