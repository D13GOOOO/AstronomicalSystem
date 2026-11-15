package astronomicalsystem.app.sky;

import astronomicalsystem.core.math.Vector3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Makes up the stars of a background sky.
 */
public final class Stars {

    // In a space filled evenly with stars alike, those that give more light than S are the ones
    // nearer than a distance that goes as S^(-1/2), and their number goes as its cube: S^(-3/2)
    private static final double FLUX_EXPONENT = -2.0 / 3.0;

    private Stars() {
    }

    /**
     * @param random where the stars are drawn from: the same generator in the same state gives the
     *               same sky
     * @param count  how many stars; precondition: not negative
     * @return stars scattered evenly over the whole sky
     */
    public static @NotNull List<Star> scattered(@NotNull RandomGenerator random, int count) {
        List<Star> stars = new ArrayList<>();
        for (int drawn = 0; drawn < count; drawn++) {
            stars.add(new Star(direction(random), flux(random)));
        }
        return stars;
    }

    // A sphere has the same area between any two parallel planes the same distance apart, wherever
    // they cut it: a height drawn evenly along the axis and a turn drawn evenly around it give a
    // direction drawn evenly over the sphere
    private static @NotNull Vector3 direction(@NotNull RandomGenerator random) {
        double height = 2.0 * random.nextDouble() - 1.0;
        double turn = 2.0 * Math.PI * random.nextDouble();
        double parallelRadius = Math.sqrt(1.0 - height * height);
        return new Vector3(parallelRadius * Math.cos(turn), parallelRadius * Math.sin(turn), height);
    }

    // The share of the stars fainter than this one is drawn evenly: the others, 1 - share of them,
    // are (flux)^(-3/2) of all, the faintest having flux 1
    private static double flux(@NotNull RandomGenerator random) {
        return Math.pow(1.0 - random.nextDouble(), FLUX_EXPONENT);
    }
}
