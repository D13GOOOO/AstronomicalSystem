package astronomicalsystem.view;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.engine.TrajectoryPredictor;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.SimulationObserver;
import astronomicalsystem.model.Star;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
import javafx.application.Platform;
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
 * Furthermore, this renderer employs predictive trajectory tracking. Rather than drawing static mathematical 
 * circles, it actively consults the physics engine to project the future paths of all moving bodies. To ensure 
 * 60 FPS graphics, this predictive calculation is offloaded to a dedicated background thread, generating 
 * dynamic 3D trails that warp and bend in real-time without causing interface lag.
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
     * An absolute safety boundary that acts as a virtual cage for the graphics engine to prevent 
     * coordinate overflow crashes when objects are ejected into deep space.
     */
    private static final double MAX_RENDER_COORD = 500_000.0;
    
    /**
     * Limits the frequency of predictive trajectory recalculations to conserve CPU resources 
     * while maintaining a smooth and responsive graphical interface.
     */
    private static final int PREDICTION_REFRESH_RATE = 60; 

    /**
     * The primary container holding all three dimensional objects within the JavaFX scene graph.
     */
    private final Group worldGroup;

    /**
     * A mapping linking each physical celestial body to its visual spherical representation.
     */
    private final Map<CelestialBody, Sphere> visualObjects;

    /**
     * A mapping linking each physical celestial body to its dynamically generated orbital trail mesh.
     */
    private final Map<CelestialBody, Node> orbitObjects;
    
    /**
     * A mapping linking each physical celestial body to its personal velocity vector arrow.
     */
    private final Map<CelestialBody, VectorArrow3D> velocityArrows;

    /**
     * A mapping linking each physical celestial body to its personal gravitational vector arrow.
     */
    private final Map<CelestialBody, VectorArrow3D> gravityArrows;

    /**
     * The performance monitoring utility used to track and log rendering frame times.
     */
    private final SimulationPerformanceMonitor performanceMonitor;

    /**
     * A reference to the specific celestial body acting as Saturn, used to correctly anchor its rings.
     */
    private CelestialBody saturnBody;

    /**
     * The custom generated three dimensional ring system attached to the Saturn celestial body.
     */
    private SaturnRing saturnRingVisual;

    /**
     * An internal counter tracking the number of rendered frames to schedule periodic asynchronous updates.
     */
    private int frameCounter = 0;

    /**
     * The specific celestial body currently selected for scientific HUD analysis.
     */
    private CelestialBody targetedBody;
    
    /**
     * A concurrency lock ensuring that a new predictive calculation thread is not spawned 
     * while an existing prediction is still being processed in the background.
     */
    private final AtomicBoolean isPredicting = new AtomicBoolean(false);

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
        this.velocityArrows = new HashMap<>();
        this.gravityArrows = new HashMap<>();
    }
    
    /**
     * Commands the rendering engine to lock its scientific HUD instruments onto a specific celestial body.
     * When a body is targeted, all tactical vector arrows across the universe are dynamically hidden 
     * to provide a clean, cinematic observation view. Passing null restores the tactical overview.
     *
     * @param target the specific planet or moon to analyze, or null to return to tactical overview.
     */
    public void setTargetBody(CelestialBody target) {
        this.targetedBody = target;
        
        Platform.runLater(() -> {
            boolean showArrows = (target == null);
            for (VectorArrow3D arrow : this.velocityArrows.values()) {
                if (arrow != null) arrow.setVisible(showArrows);
            }
            for (VectorArrow3D arrow : this.gravityArrows.values()) {
                if (arrow != null) arrow.setVisible(showArrows);
            }
        });
    }

    /**
     * Scans the currently tracked visual objects to locate the primary gravitational anchor, typically a star.
     *
     * @return the central star if found, or null if no star exists in the simulation.
     */
    private Star findCentralStar() {
        for (CelestialBody body : this.visualObjects.keySet()) {
            if (body instanceof Star star) {
                return star;
            }
        }
        return null;
    }

    /**
     * The primary action triggered every single time the physics engine finishes calculating a moment in time.
     * 
     * <b>Architecture Note:</b> Because the physics engine runs on an independent background thread, 
     * this method immediately wraps all graphical modifications inside a {@code Platform.runLater} call. 
     * This ensures the heavy mathematical calculations don't illegally touch the JavaFX scene graph, 
     * passing the final coordinate data back to the primary UI thread for safe drawing.
     *
     * @param step        the exact sequential frame number that the simulation just finished calculating.
     * @param bodies      the complete list containing every celestial body currently existing in the physical universe.
     * @param totalEnergy a diagnostic measurement of the total energy, ignored by the visual renderer but required by the contract.
     */
    @Override
    public void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy) {
        
        final List<CelestialBody> snapshotBodies = new ArrayList<>(bodies);
        
        Platform.runLater(() -> {
            try {
                long renderStart = System.nanoTime();
                frameCounter++;

                Set<CelestialBody> activeBodies = new HashSet<>(snapshotBodies);

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
                        
                        VectorArrow3D vArrow = this.velocityArrows.remove(body);
                        VectorArrow3D gArrow = this.gravityArrows.remove(body);
                        if (vArrow != null) this.worldGroup.getChildren().remove(vArrow);
                        if (gArrow != null) this.worldGroup.getChildren().remove(gArrow);
                        
                        if (body.equals(this.targetedBody)) {
                            setTargetBody(null);
                        }
                        
                        it.remove();

                        if (body.equals(this.saturnBody) && this.saturnRingVisual != null) {
                            this.worldGroup.getChildren().remove(this.saturnRingVisual);
                            this.saturnRingVisual = null;
                            this.saturnBody = null;
                        }
                    }
                }

                for (CelestialBody body : snapshotBodies) {
                    if (!this.visualObjects.containsKey(body)) {
                        createVisualsFor(body);
                    }

                    Sphere sphere = this.visualObjects.get(body);
                    if (sphere != null) {
                        double scale = SETTINGS.render().positionScale();
                        double tx = body.getPosition().x() * scale;
                        double ty = body.getPosition().y() * scale;
                        double tz = body.getPosition().z() * scale;

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
                    this.saturnRingVisual.setTranslateX(this.saturnBody.getPosition().x() * scale);
                    this.saturnRingVisual.setTranslateY(this.saturnBody.getPosition().y() * scale);
                    this.saturnRingVisual.setTranslateZ(this.saturnBody.getPosition().z() * scale);
                }
                
                if (this.targetedBody == null) {
                    double scale = SETTINGS.render().positionScale();
                    Star centralStar = findCentralStar();
                    
                    for (CelestialBody body : snapshotBodies) {
                        if (!(body instanceof Kinematic movingTarget)) continue;

                        VectorArrow3D vArrow = this.velocityArrows.get(body);
                        VectorArrow3D gArrow = this.gravityArrows.get(body);

                        if (vArrow != null && gArrow != null) {
                            Point3D scaledOrigin = new Point3D(
                                body.getPosition().x() * scale,
                                body.getPosition().y() * scale,
                                body.getPosition().z() * scale
                            );
                            
                            double visualRadius = getVisualRadius(body);

                            vArrow.update(scaledOrigin, movingTarget.getVelocity(), 10.0, visualRadius);

                            if (centralStar != null && !body.equals(centralStar)) {
                                Point3D starPos = centralStar.getPosition();
                                Point3D targetPos = body.getPosition();
                                
                                double dx = starPos.x() - targetPos.x();
                                double dy = starPos.y() - targetPos.y();
                                double dz = starPos.z() - targetPos.z();
                                
                                double distSq = dx * dx + dy * dy + dz * dz;
                                
                                if (distSq > 0) {
                                    double forceMultiplier = (1.0 / distSq) * 50000000.0;
                                    Point3D gravityVector = new Point3D(dx, dy, dz).normalize().scale(forceMultiplier);
                                    gArrow.update(scaledOrigin, gravityVector, 1.0, visualRadius);
                                    gArrow.setVisible(true);
                                } else {
                                    gArrow.setVisible(false);
                                }
                            } else {
                                gArrow.setVisible(false);
                            }
                        }
                    }
                }

                if ((frameCounter % PREDICTION_REFRESH_RATE == 0 || frameCounter == 1) && !isPredicting.get()) {
                    triggerAsyncPrediction(snapshotBodies);
                }

                if (this.performanceMonitor != null) {
                    this.performanceMonitor.recordRender(System.nanoTime() - renderStart);
                }
            } catch (Throwable t) {
                System.err.println("Render Engine Guard caught an error: " + t.getMessage());
            }
        });
    }

    /**
     * Retrieves the visual size of a specific celestial body to assist external classes in accurately 
     * placing UI elements or calculating camera distances.
     *
     * @param body the physical entity to measure.
     * @return the calculated numerical radius used for the graphical three dimensional sphere.
     */
    public double getTargetVisualRadius(CelestialBody body) {
        if (body == null) return 0;
        return getVisualRadius(body);
    }

    /**
     * Spawns a lightweight background thread to consult the Trajectory Predictor. 
     * This ensures the heavy calculations for future steps do not block the JavaFX rendering pipeline.
     * Once the calculations are complete and the new 3D meshes are generated, they are injected back into the 
     * main UI thread for display.
     *
     * @param snapshotBodies a thread-safe copy of the universe to base the prediction on.
     */
    private void triggerAsyncPrediction(List<CelestialBody> snapshotBodies) {
        isPredicting.set(true);
        double scale = SETTINGS.render().positionScale();

        Thread predictionThread = new Thread(() -> {
            try {
                Map<CelestialBody, List<Point3D>> predictions = TrajectoryPredictor.calculate(snapshotBodies, 40000, 1.0, 3);
                
                Map<CelestialBody, MeshView> newMeshes = new HashMap<>();
                
                for (Map.Entry<CelestialBody, List<Point3D>> entry : predictions.entrySet()) {
                    CelestialBody body = entry.getKey();
                    List<Point3D> path = entry.getValue();
                    
                    if (path.size() >= 2) {
                        MeshView newTrail = createDynamicTrailMesh(path, scale);
                        newMeshes.put(body, newTrail);
                    }
                }

                Platform.runLater(() -> {
                    for (Map.Entry<CelestialBody, MeshView> entry : newMeshes.entrySet()) {
                        CelestialBody body = entry.getKey();
                        MeshView newTrail = entry.getValue();

                        if (visualObjects.containsKey(body)) {
                            Node oldOrbit = this.orbitObjects.remove(body);
                            if (oldOrbit != null) {
                                this.worldGroup.getChildren().remove(oldOrbit);
                            }

                            this.worldGroup.getChildren().add(0, newTrail);
                            this.orbitObjects.put(body, newTrail);
                        }
                    }
                    isPredicting.set(false);
                });

            } catch (Exception e) {
                System.err.println("Prediction Thread Error: " + e.getMessage());
                isPredicting.set(false);
            }
        }, "Trajectory-Oracle-Thread");
        
        predictionThread.setDaemon(true);
        predictionThread.start();
    }

    /**
     * Constructs the initial physical three dimensional spheres, paints the image textures, 
     * and initializes the personal vector arrows for a newly introduced body.
     *
     * @param body the physical entity that has just entered the simulation and requires visual representation.
     */
    private void createVisualsFor(CelestialBody body) {
        String name = body.getName().toLowerCase();

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
        
        VectorArrow3D vArrow = new VectorArrow3D(Color.LIMEGREEN);
        VectorArrow3D gArrow = new VectorArrow3D(Color.ORANGERED);
        
        if (this.targetedBody == null) {
            vArrow.setVisible(true);
            gArrow.setVisible(true);
        } else {
            vArrow.setVisible(false);
            gArrow.setVisible(false);
        }
        
        this.velocityArrows.put(body, vArrow);
        this.gravityArrows.put(body, gArrow);
        this.worldGroup.getChildren().addAll(vArrow, gArrow);

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
     * Converts a series of calculated future coordinates into a solid, flat 3D ribbon stretching through space.
     * 
     * This avoids drawing standard JavaFX lines, which are prone to crashing at massive scales. By mathematically 
     * extruding the points into a connected triangle mesh, it creates a stable, high-performance visual trail.
     *
     * @param path  the ordered list of future physical coordinates provided by the trajectory predictor.
     * @param scale the visual scaling factor to translate physical meters into on-screen pixels.
     * @return a fully constructed, semi-transparent 3D ribbon representing the predicted path.
     */
    private MeshView createDynamicTrailMesh(List<Point3D> path, double scale) {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0f, 0f);

        int pointsCount = path.size();
        float[] points = new float[pointsCount * 2 * 3];
        int[] faces = new int[(pointsCount - 1) * 2 * 6];

        float halfThickness = 0.5f;
        int pIndex = 0;
        int fIndex = 0;

        for (int i = 0; i < pointsCount; i++) {
            Point3D p = path.get(i);
            
            float x = (float) Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, p.x() * scale));
            float y = (float) Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, p.y() * scale));
            float z = (float) Math.max(-MAX_RENDER_COORD, Math.min(MAX_RENDER_COORD, p.z() * scale));

            points[pIndex++] = x;
            points[pIndex++] = y;
            points[pIndex++] = z - halfThickness;

            points[pIndex++] = x;
            points[pIndex++] = y;
            points[pIndex++] = z + halfThickness;

            if (i < pointsCount - 1) {
                int currentInner = i * 2;
                int currentOuter = i * 2 + 1;
                int nextInner = (i + 1) * 2;
                int nextOuter = (i + 1) * 2 + 1;

                faces[fIndex++] = currentInner; faces[fIndex++] = 0;
                faces[fIndex++] = currentOuter; faces[fIndex++] = 0;
                faces[fIndex++] = nextInner;    faces[fIndex++] = 0;

                faces[fIndex++] = currentOuter; faces[fIndex++] = 0;
                faces[fIndex++] = nextOuter;    faces[fIndex++] = 0;
                faces[fIndex++] = nextInner;    faces[fIndex++] = 0;
            }
        }

        mesh.getPoints().addAll(points);
        mesh.getFaces().addAll(faces);

        MeshView trailView = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.rgb(135, 206, 250, 0.4));
        trailView.setMaterial(material);
        trailView.setCullFace(CullFace.NONE);

        return trailView;
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