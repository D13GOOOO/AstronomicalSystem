package astronomicalsystem.app.time;

/**
 * How fast simulated time flows compared with real time.
 *
 * @param daysPerSecond simulated days that pass in one real second
 */
public record TimeWarp(double daysPerSecond) {
}
