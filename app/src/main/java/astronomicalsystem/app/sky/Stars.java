package astronomicalsystem.app.sky;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.random.RandomGenerator;

/**
 * Makes up the stars of a background sky.
 */
public final class Stars {

    private Stars() {
    }

    /**
     * @param random where the stars are drawn from: the same generator in the same state gives the
     *               same sky
     * @param count  how many stars; precondition: not negative
     * @return stars scattered evenly over the whole sky
     */
    public static @NotNull List<Star> scattered(@NotNull RandomGenerator random, int count) {
        return List.of();
    }
}
