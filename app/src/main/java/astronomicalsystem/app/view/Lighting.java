package astronomicalsystem.app.view;

/**
 * How the light falls on a disc drawn as a sphere.
 */
public sealed interface Lighting permits Lighting.OwnLight, Lighting.LitFrom {

    Lighting OWN_LIGHT = new OwnLight();

    /**
     * Shines with its own light, like a star.
     */
    record OwnLight() implements Lighting {
    }

    /**
     * Lit from one side.
     *
     * @param x towards the light, to the right of the image
     * @param y towards the light, up in the image
     * @param z towards the light, out of the image towards the viewer; together a unit vector
     */
    record LitFrom(double x, double y, double z) implements Lighting {
    }
}
