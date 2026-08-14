package astronomicalsystem.view;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.factory.UniverseFactory;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.Moon;
import astronomicalsystem.model.SimulationObserver;
import astronomicalsystem.model.Star;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Sphere;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;

/**
 * An observer implementation responsible for translating physical simulation
 * state into 3D visual representations.
 * <p>
 * This class maintains the mapping between backend domain models and frontend
 * JavaFX geometry. It receives periodic state updates and applies spatial
 * transformations, ensuring strict separation between physics processing
 * and the graphics pipeline.
 * </p>
 * <p>
 * <b>Performance Optimization:</b> Utilizes a static texture cache to prevent redundant 
 * disk I/O when spawning multiple identical bodies (e.g., custom planets).
 * </p>
 */
public class SimulationRenderer implements SimulationObserver {

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();
    
    /**
     * Cache for loaded textures to minimize memory footprint and disk I/O during runtime spawns.
     */
    private static final Map<String, Image> TEXTURE_CACHE = new HashMap<>();

    private static final double MAX_RENDER_COORD = 500_000.0;

    private final Group worldGroup;
    private final Map<CelestialBody, Sphere> visualObjects;
    private final Map<CelestialBody, Node> orbitObjects;
    private final SimulationPerformanceMonitor performanceMonitor;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;

    /**
     * Initializes the rendering engine without performance telemetry.
     *
     * @param worldGroup the JavaFX group node acting as the coordinate system root.
     */
    public SimulationRenderer(Group worldGroup) {
        this(worldGroup, null);
    }

    /**
     * Initializes the rendering engine and binds it to the primary 3D scene group.
     *
     * @param worldGroup         the JavaFX group node acting as the coordinate system root (must not be null).
     * @param performanceMonitor the telemetry tracker for rendering times (can be null).
     * @throws NullPointerException if {@code worldGroup} is null.
     */
    public SimulationRenderer(Group worldGroup, SimulationPerformanceMonitor performanceMonitor) {
        this.worldGroup = Objects.requireNonNull(worldGroup, "World group cannot be null");
        this.performanceMonitor = performanceMonitor;
        this.visualObjects = new HashMap<>();
        this.orbitObjects = new HashMap<>();
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
        try {
            long renderStart = System.nanoTime();

            Set<CelestialBody> activeBodies = new HashSet<>(bodies);

            Iterator<Map.Entry<CelestialBody, Sphere>> it = this.visualObjects.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<CelestialBody, Sphere> entry = it.next();
                CelestialBody body = entry.getKey();

                if (!activeBodies.contains(body)) {
                    this.worldGroup.getChildren().remove(entry.getValue());
                    
                    Node orbit = this.orbitObjects.remove(body);
                    if (orbit != null) {
                        this.worldGroup.getChildren().remove(orbit);
                    }
                    
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
                if (sphere != null) {
                    double scale = SETTINGS.render().positionScale();
                    double tx = body.getPosition().x() * scale;
                    double ty = body.getPosition().y() * scale;
                    double tz = body.getPosition().z() * scale;

                    if (!Double.isFinite(tx)) tx = 0.0;
                    if (!Double.isFinite(ty)) ty = 0.0;
                    if (!Double.isFinite(tz)) tz = 0.0;

                    tx = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, tx));
                    ty = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, ty));
                    tz = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, tz));

                    sphere.setTranslateX(tx);
                    sphere.setTranslateY(ty);
                    sphere.setTranslateZ(tz);

                    if (body instanceof Star) {
                        sphere.setRotate(sphere.getRotate() + SETTINGS.render().starSpinSpeed());
                    } else {
                        sphere.setRotate(sphere.getRotate() + SETTINGS.render().bodySpinSpeed());
                    }
                }

                if (body.getName().equalsIgnoreCase("Saturn")) {
                    this.saturnBody = body;
                }
            }

            if (this.saturnBody != null && this.saturnRingVisual != null) {
                double scale = SETTINGS.render().positionScale();
                double rtx = this.saturnBody.getPosition().x() * scale;
                double rty = this.saturnBody.getPosition().y() * scale;
                double rtz = this.saturnBody.getPosition().z() * scale;

                if (!Double.isFinite(rtx)) rtx = 0.0;
                if (!Double.isFinite(rty)) rty = 0.0;
                if (!Double.isFinite(rtz)) rtz = 0.0;

                rtx = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, rtx));
                rty = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, rty));
                rtz = Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, rtz));

                this.saturnRingVisual.setTranslateX(rtx);
                this.saturnRingVisual.setTranslateY(rty);
                this.saturnRingVisual.setTranslateZ(rtz);
            }

            if (this.performanceMonitor != null) {
                this.performanceMonitor.recordRender(System.nanoTime() - renderStart);
            }
        } catch (Throwable t) {
            System.err.println("Render Engine Guard caught an error: " + t.getMessage());
        }
    }

    /**
     * Generates the 3D meshes, textures, and orbital paths for a newly introduced body.
     *
     * @param body the physical entity requiring visual representation.
     */
    private void createVisualsFor(CelestialBody body) {
        String name = body.getName().toLowerCase();

        if (body instanceof Kinematic && !(body instanceof Moon)) {
            double e = 0.0;
            double iDeg = 0.0;

            if (!name.contains("custom")) {
                try {
                    e = UniverseFactory.getEccentricity(body.getName());
                    iDeg = UniverseFactory.getInclination(body.getName());
                } catch (Exception ex) {
                    e = 0.0;
                    iDeg = 0.0;
                }
            }

            double currentDistScaled = body.getPosition().magnitude() * SETTINGS.render().positionScale();
            
            if (currentDistScaled > 0.1 && currentDistScaled < MAX_RENDER_COORD) {
                double a = currentDistScaled / (1.0 - e);
                double b = a * Math.sqrt(Math.max(0, 1 - (e * e)));
                double c = a * e;

                if (Double.isFinite(a) && Double.isFinite(b) && a < MAX_RENDER_COORD && b < MAX_RENDER_COORD) {
                    MeshView orbit = create3DOrbitMesh(a, b);
                    orbit.setTranslateX(-c);
                    orbit.setRotationAxis(Rotate.X_AXIS);
                    orbit.setRotate(iDeg);

                    this.worldGroup.getChildren().add(0, orbit);
                    this.orbitObjects.put(body, orbit);
                }
            }
        }

        Sphere sphere = new Sphere(getVisualRadius(body), 32);
        sphere.setUserData(body);
        PhongMaterial material = new PhongMaterial();
        sphere.setRotationAxis(Rotate.Y_AXIS);

        String texturePath = "/" + name + ".jpg";

        try {
            Image texture = TEXTURE_CACHE.get(texturePath);
            if (texture == null) {
                try (InputStream stream = getClass().getResourceAsStream(texturePath)) {
                    if (stream != null) {
                        texture = new Image(stream);
                        TEXTURE_CACHE.put(texturePath, texture);
                    }
                }
            }

            if (texture != null) {
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

    private MeshView create3DOrbitMesh(double radiusA, double radiusB) {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0f, 0f);

        int divisions = 120; 
        float halfThickness = 1.0f; 

        float[] points = new float[divisions * 2 * 3];
        int[] faces = new int[divisions * 2 * 6];

        float innerA = (float)(radiusA - halfThickness);
        float outerA = (float)(radiusA + halfThickness);
        float innerB = (float)(radiusB - halfThickness);
        float outerB = (float)(radiusB + halfThickness);

        double angleStep = (Math.PI * 2.0) / divisions;

        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < divisions; i++) {
            double angle = i * angleStep;
            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            points[pIndex++] = innerA * cos;
            points[pIndex++] = 0;
            points[pIndex++] = innerB * sin;

            points[pIndex++] = outerA * cos;
            points[pIndex++] = 0;
            points[pIndex++] = outerB * sin;

            int currentInner = i * 2;
            int currentOuter = i * 2 + 1;
            int nextInner = ((i + 1) % divisions) * 2;
            int nextOuter = ((i + 1) % divisions) * 2 + 1;

            faces[fIndex++] = currentInner; faces[fIndex++] = 0;
            faces[fIndex++] = currentOuter; faces[fIndex++] = 0;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;

            faces[fIndex++] = currentOuter; faces[fIndex++] = 0;
            faces[fIndex++] = nextOuter;    faces[fIndex++] = 0;
            faces[fIndex++] = nextInner;    faces[fIndex++] = 0;
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        MeshView orbitView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.rgb(135, 206, 250, 0.4));
        orbitView.setMaterial(material);
        orbitView.setCullFace(CullFace.NONE);

        return orbitView;
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
        
        if (name.contains("custom")) {
            return SETTINGS.render().defaultBodyRadius() * body.getRadius();
        }
        
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