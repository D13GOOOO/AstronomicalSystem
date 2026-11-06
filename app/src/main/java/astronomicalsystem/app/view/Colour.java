package astronomicalsystem.app.view;

/**
 * Colour as the intensities of red, green and blue light, each from 0 to 1.
 */
public record Colour(double red, double green, double blue) {

    public Colour {
        if (!isIntensity(red) || !isIntensity(green) || !isIntensity(blue)) {
            throw new IllegalArgumentException("Colour components must be between 0 and 1");
        }
    }

    // Written so that NaN fails the comparisons
    private static boolean isIntensity(double component) {
        return component >= 0.0 && component <= 1.0;
    }
}
