package astronomicalsystem.view;

import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.SimulationObserver;
import astronomicalsystem.model.Star;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An observer implementation responsible for translating physical simulation
 * state into 3D visual representations.
 * <p>
 * This class maintains the mapping between backend domain models and frontend
 * JavaFX geometry. It receives periodic state updates and applies spatial
 * transformations, ensuring strict separation between physics processing
 * and the graphics pipeline.
 * </p>
 */
public class SimulationRenderer implements SimulationObserver {

    /**
     * Scaling factor mapping simulation distance units to JavaFX pixel coordinates.
     */
    private static final double POS_SCALE = 0.4;

    private final Group worldGroup;
    private final Map<CelestialBody, Sphere> visualObjects;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;

    /**
     * Initializes the rendering engine and binds it to the primary 3D scene group.
     *
     * @param worldGroup the JavaFX group node acting as the coordinate system root.
     */
    public SimulationRenderer(Group worldGroup) {
        this.worldGroup = worldGroup;
        this.visualObjects = new HashMap<>();
    }

    /**
     * Synchronizes the JavaFX 3D scene graph with the current physical state of the universe.
     * <p>
     * Optimizations applied: Uses {@link HashSet} for $O(1)$ presence validation and strictly
     * removes deleted objects from the scene graph without iterating over the entire node tree.
     * </p>
     *
     * @param step        the current iteration index.
     * @param bodies      the current active celestial entities.
     * @param totalEnergy the current total energy (unused in rendering, but respects contract).
     */
    @Override
    public void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy) {
        Set<CelestialBody> activeBodies = new HashSet<>(bodies);

        // O(N) Cleanup: Identify destroyed bodies and surgically remove their 3D meshes
        Iterator<Map.Entry<CelestialBody, Sphere>> it = this.visualObjects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<CelestialBody, Sphere> entry = it.next();
            CelestialBody body = entry.getKey();

            if (!activeBodies.contains(body)) {
                this.worldGroup.getChildren().remove(entry.getValue());
                it.remove();

                // Safely destroy Saturn's ring if Saturn was consumed in a collision
                if (body.equals(this.saturnBody) && this.saturnRingVisual != null) {
                    this.worldGroup.getChildren().remove(this.saturnRingVisual);
                    this.saturnRingVisual = null;
                    this.saturnBody = null;
                }
            }
        }

        // Render and update surviving/new bodies
        for (CelestialBody body : bodies) {
            if (!this.visualObjects.containsKey(body)) {
                createVisualsFor(body);
            }

            Sphere sphere = this.visualObjects.get(body);
            sphere.setTranslateX(body.getPosition().x() * POS_SCALE);
            sphere.setTranslateY(body.getPosition().y() * POS_SCALE);
            sphere.setTranslateZ(body.getPosition().z() * POS_SCALE);

            // Apply decorative axial rotation
            if (body instanceof Star) {
                sphere.setRotate(sphere.getRotate() + 0.05);
            } else {
                sphere.setRotate(sphere.getRotate() + 0.5);
            }

            if (body.getName().equalsIgnoreCase("Saturn")) {
                this.saturnBody = body;
            }
        }

        // Synchronize ring position with Saturn's physical translation
        if (this.saturnBody != null && this.saturnRingVisual != null) {
            this.saturnRingVisual.setTranslateX(this.saturnBody.getPosition().x() * POS_SCALE);
            this.saturnRingVisual.setTranslateY(this.saturnBody.getPosition().y() * POS_SCALE);
            this.saturnRingVisual.setTranslateZ(this.saturnBody.getPosition().z() * POS_SCALE);
        }
    }

    /**
     * Generates the 3D meshes, textures, and orbital paths for a newly introduced body.
     *
     * @param body the physical entity requiring visual representation.
     */
    private void createVisualsFor(CelestialBody body) {
        if (body instanceof Kinematic) {
            double e = UniverseFactory.getEccentricity(body.getName());
            double iDeg = UniverseFactory.getInclination(body.getName());

            // Mathematical Fix: using magnitude() instead of the removed norm() method
            double currentDistScaled = body.getPosition().magnitude() * POS_SCALE;
            double a = currentDistScaled / (1.0 - e);
            double b = a * Math.sqrt(1 - (e * e));
            double c = a * e;

            Ellipse orbit = new Ellipse(a, b);
            orbit.setStroke(Color.rgb(135, 206, 250, 0.25));
            orbit.setStrokeWidth(1);
            orbit.setFill(null);
            orbit.setTranslateX(-c);
            orbit.setRotationAxis(Rotate.X_AXIS);
            orbit.setRotate(90 + iDeg);

            this.worldGroup.getChildren().addFirst(orbit);
        }

        Sphere sphere = new Sphere();
        sphere.setUserData(body);
        PhongMaterial material = new PhongMaterial();
        sphere.setRotationAxis(Rotate.Y_AXIS);

        String name = body.getName().toLowerCase();
        String texturePath = "/" + name + ".jpg";

        try {
            InputStream stream = getClass().getResourceAsStream(texturePath);
            if (stream != null) {
                Image texture = new Image(stream);
                material.setDiffuseMap(texture);
                if (name.equals("sun")) {
                    material.setSelfIlluminationMap(texture);
                }
            } else {
                material.setDiffuseColor(getFallbackColor(name));
            }
        } catch (Exception e) {
            System.err.println("Texture missing for: " + name);
            material.setDiffuseColor(getFallbackColor(name));
        }

        sphere.setMaterial(material);
        sphere.setRadius(getVisualRadius(body));

        this.visualObjects.put(body, sphere);
        this.worldGroup.getChildren().add(sphere);

        if (name.equals("saturn")) {
            this.saturnRingVisual = new SaturnRing(42, 80, 100, "/saturn_ring.png");
            this.saturnRingVisual.setRotationAxis(Rotate.X_AXIS);
            this.saturnRingVisual.setRotate(25);
            this.worldGroup.getChildren().add(this.saturnRingVisual);
        }
    }

    /**
     * Maps physical domain models to arbitrary visual scalar radii.
     *
     * @param body the entity to scale.
     * @return the visual radius scalar.
     */
    private double getVisualRadius(CelestialBody body) {
        String name = body.getName().toLowerCase();
        if (body instanceof Star) return 80;
        if (name.contains("jupiter")) return 35;
        if (name.contains("saturn"))  return 30;
        if (name.contains("uranus"))  return 20;
        if (name.contains("neptune")) return 20;
        if (name.contains("earth"))   return 12;
        if (name.contains("venus"))   return 11;
        if (name.contains("mars"))    return 8;
        if (name.contains("mercury")) return 6;
        return 5;
    }

    /**
     * Resolves a flat fallback color if high-fidelity textures are unavailable.
     *
     * @param name the lowercase identifier of the body.
     * @return the corresponding JavaFX color.
     */
    private Color getFallbackColor(String name) {
        return switch (name) {
            case "sun" -> Color.ORANGE;
            case "mercury" -> Color.DARKGRAY;
            case "venus" -> Color.LIGHTYELLOW;
            case "earth" -> Color.DODGERBLUE;
            case "mars" -> Color.RED;
            case "jupiter" -> Color.BEIGE;
            case "saturn" -> Color.GOLD;
            case "uranus" -> Color.LIGHTBLUE;
            case "neptune" -> Color.DARKBLUE;
            default -> Color.GRAY;
        };
    }
}