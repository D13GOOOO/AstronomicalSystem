package astronomicalsystem.view;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.Moon;
import astronomicalsystem.model.SimulationObserver;
import astronomicalsystem.model.Star;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
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

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();

    private final Group worldGroup;
    private final Map<CelestialBody, Sphere> visualObjects;
    private final SimulationPerformanceMonitor performanceMonitor;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;

    /**
     * Initializes the rendering engine and binds it to the primary 3D scene group.
     *
     * @param worldGroup the JavaFX group node acting as the coordinate system root.
     */
    public SimulationRenderer(Group worldGroup) {
        this(worldGroup, null);
    }

    public SimulationRenderer(Group worldGroup, SimulationPerformanceMonitor performanceMonitor) {
        this.worldGroup = worldGroup;
        this.performanceMonitor = performanceMonitor;
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
        long renderStart = System.nanoTime();

        Set<CelestialBody> activeBodies = new HashSet<>(bodies);

        Iterator<Map.Entry<CelestialBody, Sphere>> it = this.visualObjects.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<CelestialBody, Sphere> entry = it.next();
            CelestialBody body = entry.getKey();

            if (!activeBodies.contains(body)) {
                this.worldGroup.getChildren().remove(entry.getValue());
                it.remove();

                if (body.equals(this.saturnBody) && this.saturnRingVisual != null) {
                    this.worldGroup.getChildren().remove(this.saturnRingVisual);
                    this.saturnRingVisual = null;
                    this.saturnBody = null;
                }
            }
        }

        for (CelestialBody body : bodies) {
            if (!this.visualObjects.containsKey(body)) {
                createVisualsFor(body);
            }

            Sphere sphere = this.visualObjects.get(body);
            sphere.setTranslateX(body.getPosition().x() * SETTINGS.render().positionScale());
            sphere.setTranslateY(body.getPosition().y() * SETTINGS.render().positionScale());
            sphere.setTranslateZ(body.getPosition().z() * SETTINGS.render().positionScale());

            if (body instanceof Star) {
                sphere.setRotate(sphere.getRotate() + SETTINGS.render().starSpinSpeed());
            } else {
                sphere.setRotate(sphere.getRotate() + SETTINGS.render().bodySpinSpeed());
            }

            if (body.getName().equalsIgnoreCase("Saturn")) {
                this.saturnBody = body;
            }
        }

        if (this.saturnBody != null && this.saturnRingVisual != null) {
            this.saturnRingVisual.setTranslateX(this.saturnBody.getPosition().x() * SETTINGS.render().positionScale());
            this.saturnRingVisual.setTranslateY(this.saturnBody.getPosition().y() * SETTINGS.render().positionScale());
            this.saturnRingVisual.setTranslateZ(this.saturnBody.getPosition().z() * SETTINGS.render().positionScale());
        }

        if (this.performanceMonitor != null) {
            this.performanceMonitor.recordRender(System.nanoTime() - renderStart);
        }
    }

    /**
     * Generates the 3D meshes, textures, and orbital paths for a newly introduced body.
     *
     * @param body the physical entity requiring visual representation.
     */
    private void createVisualsFor(CelestialBody body) {
        if (body instanceof Kinematic && !(body instanceof Moon)) {
            double e = UniverseFactory.getEccentricity(body.getName());
            double iDeg = UniverseFactory.getInclination(body.getName());

            double currentDistScaled = body.getPosition().magnitude() * SETTINGS.render().positionScale();
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
            material.setDiffuseColor(getFallbackColor(name));
        }

        sphere.setMaterial(material);
        sphere.setRadius(getVisualRadius(body));

        this.visualObjects.put(body, sphere);
        this.worldGroup.getChildren().add(sphere);

        if (name.equals("saturn")) {
            this.saturnRingVisual = new SaturnRing(
                    SETTINGS.render().saturnRingInnerRadius(),
                    SETTINGS.render().saturnRingOuterRadius(),
                    SETTINGS.render().saturnRingDivisions(),
                    SETTINGS.render().saturnRingTexturePath()
            );
            this.saturnRingVisual.setRotationAxis(Rotate.X_AXIS);
            this.saturnRingVisual.setRotate(SETTINGS.render().saturnRingRotationDegrees());
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
        if (body instanceof Star) return SETTINGS.render().starRadius();
        if (name.contains("jupiter")) return SETTINGS.render().jupiterRadius();
        if (name.contains("saturn"))  return SETTINGS.render().saturnRadius();
        if (name.contains("uranus"))  return SETTINGS.render().uranusRadius();
        if (name.contains("neptune")) return SETTINGS.render().neptuneRadius();
        if (name.contains("earth"))   return SETTINGS.render().earthRadius();
        if (name.contains("venus"))   return SETTINGS.render().venusRadius();
        if (name.contains("mars"))    return SETTINGS.render().marsRadius();
        if (name.contains("mercury")) return SETTINGS.render().mercuryRadius();
        if (name.contains("moon"))    return SETTINGS.render().moonRadius();
        return SETTINGS.render().defaultBodyRadius();
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
            case "moon" -> Color.LIGHTGRAY;
            default -> Color.GRAY;
        };
    }
}