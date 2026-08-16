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
 * A graphical observer component responsible for translating the invisible mathematical calculations 
 * of the physics engine into visible, three dimensional objects on the user's screen.
 * 
 * This class acts as the ultimate bridge between the raw physics data and the JavaFX rendering pipeline. 
 * It continuously listens for updates from the physics engine. Whenever the engine completes a calculation 
 * step, this renderer takes the exact coordinates of every planet, scales them down to fit inside the 
 * visible screen area, and moves the corresponding 3D spheres to those new locations.
 * 
 * To ensure maximum performance and prevent the simulation from stuttering when the user creates dozens 
 * of custom planets, this class utilizes a static memory cache for image textures. This guarantees that 
 * the application only reads an image file from the hard drive once, reusing the same loaded image in 
 * memory for every identical planet spawned thereafter.
 */
public class SimulationRenderer implements SimulationObserver {

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();
    
    /**
     * A high speed memory cache that permanently holds the loaded image files for planet textures. 
     * This prevents the severe performance penalty of constantly reading from the hard drive every 
     * time a new celestial body is created dynamically by the user.
     */
    private static final Map<String, Image> TEXTURE_CACHE = new HashMap<>();

    /**
     * An absolute safety boundary that acts as a virtual cage for the graphics engine. 
     * If the physics engine calculates an explosive slingshot effect that shoots a planet out into 
     * infinite space, the renderer will clamp the visual object to this maximum distance. This prevents 
     * massive numbers from reaching the graphics card, completely avoiding a native overflow crash.
     */
    private static final double MAX_RENDER_COORD = 500_000.0;

    private final Group worldGroup;
    private final Map<CelestialBody, Sphere> visualObjects;
    private final Map<CelestialBody, Node> orbitObjects;
    private final SimulationPerformanceMonitor performanceMonitor;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;

    /**
     * Initializes the rendering engine and attaches it to the main three dimensional space without 
     * connecting an active performance tracking monitor.
     *
     * @param worldGroup the primary JavaFX graphical container where all 3D objects will be placed and drawn.
     */
    public SimulationRenderer(Group worldGroup) {
        this(worldGroup, null);
    }

    /**
     * Initializes the rendering engine, attaches it to the main three dimensional space, and connects 
     * a dedicated performance monitor to track exactly how long it takes to draw each visual frame.
     *
     * @param worldGroup         the primary JavaFX graphical container acting as the root of the universe. Must not be null.
     * @param performanceMonitor the optional telemetry tracker used to log rendering speeds.
     * @throws NullPointerException if the provided world group is completely missing or uninitialized.
     */
    public SimulationRenderer(Group worldGroup, SimulationPerformanceMonitor performanceMonitor) {
        this.worldGroup = Objects.requireNonNull(worldGroup, "World group cannot be null");
        this.performanceMonitor = performanceMonitor;
        this.visualObjects = new HashMap<>();
        this.orbitObjects = new HashMap<>();
    }

    /**
     * The primary action triggered every single time the physics engine finishes calculating a moment in time.
     * 
     * This massive synchronization method performs three main tasks. First, it compares the current list of 
     * living planets against its own list of drawn shapes. If a planet has been destroyed in a physical collision, 
     * its 3D sphere and glowing orbit track are permanently erased from the screen. Second, if a brand new planet 
     * has been spawned, it orders the creation of new 3D shapes for it. Third, it takes the mathematical 
     * coordinates of every surviving planet, scales them to visual proportions, and safely moves the graphical 
     * spheres. It heavily utilizes the maximum render coordinate safety net to ensure that no object can ever 
     * fly far enough away to crash the underlying graphical matrices.
     *
     * @param step        the exact sequential frame number that the simulation just finished calculating.
     * @param bodies      the complete list containing every celestial body currently existing in the physical universe.
     * @param totalEnergy a diagnostic measurement of the total energy, ignored by the visual renderer but required by the contract.
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
     * Constructs the physical three dimensional shapes, paints the image textures, and calculates the 
     * orbital paths for a newly introduced celestial body.
     * 
     * This method looks at the name of the new body to find the corresponding image file. If the body 
     * is a moving object and not a simple moon, it mathematically extracts its orbital shape from the 
     * universe factory and draws a massive three dimensional ring in space to represent its trajectory. 
     * Finally, if the newly spawned body happens to be Saturn, it triggers the generation of a specialized, 
     * highly detailed procedural ring system and attaches it to the planet.
     *
     * @param body the physical entity that has just entered the simulation and requires visual representation.
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

    /**
     * Mathematically generates a massive, incredibly thin three dimensional ring to visually represent 
     * the orbital path of a planet. 
     * 
     * This specifically builds a three dimensional mesh of triangles rather than using a standard two 
     * dimensional line shape. This is an extremely important architectural workaround. The standard two 
     * dimensional line drawer inside JavaFX suffers from a fatal flaw that crashes the entire application 
     * when asked to draw a shape that is too large. By using three dimensional triangles instead, this 
     * method completely bypasses that broken engine and sends the orbital shape directly to the graphics 
     * card, ensuring the application remains perfectly stable even when drawing orbits that are millions 
     * of units wide.
     *
     * @param radiusA the primary horizontal radius of the elliptical orbital path.
     * @param radiusB the secondary depth radius of the elliptical orbital path.
     * @return a fully constructed, semi transparent three dimensional mesh representing the orbital track.
     */
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
     * Translates the true physical mass or identity of a celestial body into a manageable graphical size. 
     * 
     * Because the real universe is mostly empty space, drawing planets to their true, exact scale would 
     * make them microscopic and completely invisible on a computer screen compared to the sun. This method 
     * acts as an aesthetic director, looking up the name of the planet and applying custom, artificially 
     * inflated sizes so that every planet looks beautiful, distinct, and easily clickable within the simulation.
     *
     * @param body the physical entity whose visual size needs to be determined.
     * @return the calculated numerical radius to be used exclusively for the graphical three dimensional sphere.
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
     * Operates as a visual safety net in the event of missing files. 
     * 
     * If the application attempts to load an image texture for a planet but the file has been deleted, 
     * renamed, or corrupted, this method catches the error and provides a solid, thematic backup paint 
     * color. This ensures the simulation can continue running smoothly and the planet remains clearly 
     * visible to the user rather than becoming totally transparent.
     *
     * @param name the lowercase string name of the celestial body that failed to load an image.
     * @return a solid JavaFX color roughly matching the real world visual identity of that specific planet.
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