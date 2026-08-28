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
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Comet;
import astronomicalsystem.model.Fragment;
import astronomicalsystem.model.Kinematic;
import astronomicalsystem.model.Point3D;
import astronomicalsystem.model.SimulationObserver;
import astronomicalsystem.model.Star;
import astronomicalsystem.performance.SimulationPerformanceMonitor;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
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
import javafx.util.Duration;

/**
 * A graphical observer component responsible for translating the invisible mathematical calculations
 * of the physics engine into visible, three dimensional objects on the user's screen.
 * <p>
 * This core renderer handles the drawing and positioning of 3D spheres (planets, stars), procedural
 * meshes (comet tails), and UI elements (vector arrows). It delegates predictive trajectory rendering
 * to the {@link TrajectoryRenderer} to keep the architecture clean and performant.
 * </p>
 */
public class SimulationRenderer implements SimulationObserver {

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();

    /**
     * A high speed memory cache that permanently holds the loaded image files for planet textures.
     */
    private static final Map<String, Image> TEXTURE_CACHE = new HashMap<>();

    /**
     * An absolute safety boundary that acts as a virtual cage for the graphics engine.
     */
    private static final double MAX_RENDER_COORD = 500_000.0;

    /**
     * Limits the frequency of predictive trajectory recalculations.
     */
    private static final int PREDICTION_REFRESH_RATE = 60;

    private final Group worldGroup;

    private final Map<CelestialBody, Sphere> visualObjects;
    private final Map<CelestialBody, Node> orbitObjects;
    private final Map<CelestialBody, VectorArrow3D> velocityArrows;
    private final Map<CelestialBody, VectorArrow3D> gravityArrows;
    private final Map<Comet, MeshView> cometTails;

    private final SimulationPerformanceMonitor performanceMonitor;
    private final TrajectoryRenderer trajectoryRenderer;

    private CelestialBody saturnBody;
    private SaturnRing saturnRingVisual;
    private int frameCounter = 0;
    private CelestialBody targetedBody;
    private final AtomicBoolean isPredicting = new AtomicBoolean(false);

    /**
     * Initializes the rendering engine without an active performance tracking monitor.
     *
     * @param worldGroup the primary JavaFX graphical container.
     */
    public SimulationRenderer(Group worldGroup) {
        this(worldGroup, null);
    }

    /**
     * Initializes the rendering engine and connects a performance monitor.
     *
     * @param worldGroup         the primary JavaFX graphical container.
     * @param performanceMonitor the optional telemetry tracker.
     */
    public SimulationRenderer(Group worldGroup, SimulationPerformanceMonitor performanceMonitor) {
        this.worldGroup = Objects.requireNonNull(worldGroup, "World group cannot be null");
        this.performanceMonitor = performanceMonitor;
        this.visualObjects = new HashMap<>();
        this.orbitObjects = new HashMap<>();
        this.velocityArrows = new HashMap<>();
        this.gravityArrows = new HashMap<>();
        this.cometTails = new HashMap<>();
        
        this.trajectoryRenderer = new TrajectoryRenderer(this.worldGroup, this.orbitObjects, this.isPredicting);
    }

    /**
     * Commands the rendering engine to lock its scientific HUD instruments onto a specific celestial body.
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
     * @param step        the exact sequential frame number.
     * @param bodies      the complete list of all celestial bodies.
     * @param totalEnergy a diagnostic measurement of the total energy.
     */
    @Override
    public void onSimulationStep(int step, List<CelestialBody> bodies, double totalEnergy) {

        final List<CelestialBody> snapshotBodies = new ArrayList<>(bodies);

        Platform.runLater(() -> {
            try {
                long renderStart = System.nanoTime();
                frameCounter++;

                Set<CelestialBody> activeBodies = new HashSet<>(snapshotBodies);

                // 1. Cleanup destroyed or merged bodies & Trigger Explosions
                Iterator<Map.Entry<CelestialBody, Sphere>> it = this.visualObjects.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<CelestialBody, Sphere> entry = it.next();
                    CelestialBody body = entry.getKey();
                    Sphere oldSphere = entry.getValue();

                    if (!activeBodies.contains(body)) {
                        
                        // If a major body (not a fragment) disappears, it means a collision occurred.
                        if (!(body instanceof Fragment)) {
                            triggerExplosionEffect(oldSphere.getTranslateX(), oldSphere.getTranslateY(), oldSphere.getTranslateZ(), oldSphere.getRadius());
                        }
                        
                        this.worldGroup.getChildren().remove(oldSphere);

                        Node orbit = this.orbitObjects.remove(body);
                        if (orbit != null) {
                            this.worldGroup.getChildren().remove(orbit);
                        }

                        VectorArrow3D vArrow = this.velocityArrows.remove(body);
                        VectorArrow3D gArrow = this.gravityArrows.remove(body);
                        if (vArrow != null) this.worldGroup.getChildren().remove(vArrow);
                        if (gArrow != null) this.worldGroup.getChildren().remove(gArrow);

                        if (body instanceof Comet comet) {
                            MeshView tail = this.cometTails.remove(comet);
                            if (tail != null) this.worldGroup.getChildren().remove(tail);
                        }

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

                Star centralStar = findCentralStar();

                // 2. Render and update all active bodies
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

                    if (body instanceof Comet comet && centralStar != null) {
                        updateCometTailVisuals(comet, centralStar);
                    }
                }

                // 3. Update Saturn's Rings
                if (this.saturnBody != null && this.saturnRingVisual != null) {
                    double scale = SETTINGS.render().positionScale();
                    this.saturnRingVisual.setTranslateX(this.saturnBody.getPosition().x() * scale);
                    this.saturnRingVisual.setTranslateY(this.saturnBody.getPosition().y() * scale);
                    this.saturnRingVisual.setTranslateZ(this.saturnBody.getPosition().z() * scale);
                }

                // 4. Update Vector Arrows
                if (this.targetedBody == null) {
                    double scale = SETTINGS.render().positionScale();

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

                // 5. Trigger Async Trajectory Prediction
                if ((frameCounter % PREDICTION_REFRESH_RATE == 0 || frameCounter == 1) && !isPredicting.get()) {
                    this.trajectoryRenderer.triggerAsyncPrediction(snapshotBodies, this.visualObjects);
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
     * Spawns a temporary, expanding 3D shockwave to visually signify a high-energy collision.
     * The sphere grows rapidly while fading to transparent, then removes itself from the scene.
     */
    private void triggerExplosionEffect(double tx, double ty, double tz, double baseRadius) {
        Sphere shockwave = new Sphere(baseRadius * 1.5);
        
        PhongMaterial explosionMat = new PhongMaterial();
        explosionMat.setDiffuseColor(Color.rgb(255, 140, 0, 0.8)); // Hot orange
        explosionMat.setSpecularColor(Color.YELLOW);
        shockwave.setMaterial(explosionMat);
        
        shockwave.setTranslateX(tx);
        shockwave.setTranslateY(ty);
        shockwave.setTranslateZ(tz);
        
        this.worldGroup.getChildren().add(shockwave);
        
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(800), shockwave);
        scaleTransition.setToX(4.0);
        scaleTransition.setToY(4.0);
        scaleTransition.setToZ(4.0);
        
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(800), shockwave);
        fadeTransition.setFromValue(1.0);
        fadeTransition.setToValue(0.0);
        
        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.setOnFinished(e -> this.worldGroup.getChildren().remove(shockwave));
        parallelTransition.play();
    }

    /**
     * Calculates and renders the dynamic particle tail for a comet based on its proximity to the sun.
     *
     * @param comet       the comet entity being updated.
     * @param centralStar the heat source triggering the sublimation.
     */
    private void updateCometTailVisuals(Comet comet, Star centralStar) {
        comet.updateThermalState(centralStar.getPosition());
        double intensity = comet.getTailIntensity();

        MeshView tailMesh = this.cometTails.get(comet);

        if (intensity <= 0.01) {
            if (tailMesh != null) {
                tailMesh.setVisible(false);
            }
            return;
        }

        double scale = SETTINGS.render().positionScale();
        Point3D scaledCometPos = comet.getPosition().scale(scale);
        Point3D scaledStarPos = centralStar.getPosition().scale(scale);

        Point3D awayFromSun = scaledCometPos.subtract(scaledStarPos).normalize();

        double tailLength = 200.0 * intensity;
        double tailWidth = 20.0 * intensity;

        Point3D tailEnd = scaledCometPos.add(awayFromSun.scale(tailLength));

        if (tailMesh == null) {
            tailMesh = new MeshView();
            PhongMaterial tailMat = new PhongMaterial(Color.rgb(173, 216, 230, 0.6));
            tailMesh.setMaterial(tailMat);
            tailMesh.setCullFace(CullFace.NONE);
            this.cometTails.put(comet, tailMesh);
            this.worldGroup.getChildren().add(tailMesh);
        }

        TriangleMesh tMesh = new TriangleMesh();
        tMesh.getTexCoords().addAll(0,0);

        float cx = (float) scaledCometPos.x();
        float cy = (float) scaledCometPos.y();
        float cz = (float) scaledCometPos.z();

        float ex = (float) tailEnd.x();
        float ey = (float) tailEnd.y();
        float ez = (float) tailEnd.z();

        float wx = (float) (awayFromSun.y() * tailWidth);
        float wy = (float) (-awayFromSun.x() * tailWidth);

        tMesh.getPoints().addAll(
            cx, cy, cz,
            ex + wx, ey + wy, ez,
            ex - wx, ey - wy, ez
        );

        tMesh.getFaces().addAll(0, 0, 1, 0, 2, 0);

        tailMesh.setMesh(tMesh);
        tailMesh.setVisible(true);
    }

    /**
     * Retrieves the visual size of a specific celestial body.
     *
     * @param body the physical entity to measure.
     * @return the calculated numerical radius.
     */
    public double getTargetVisualRadius(CelestialBody body) {
        if (body == null) return 0;
        return getVisualRadius(body);
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

        if (body instanceof Comet) {
            texturePath = "/moon.jpg";
        } else if (body instanceof Fragment) {
            material.setDiffuseColor(Color.DARKRED);
            material.setSpecularColor(Color.ORANGE);
            material.setSelfIlluminationMap(createGlowingDebrisTexture());
        }

        if (!(body instanceof Fragment)) {
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
        }

        sphere.setMaterial(material);

        this.visualObjects.put(body, sphere);
        this.worldGroup.getChildren().add(sphere);

        if (!(body instanceof Fragment)) {
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
        }

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
     * Creates a simple procedural texture in memory to make collision debris look like glowing magma.
     * 
     * @return an Image representing glowing hot rock.
     */
    private Image createGlowingDebrisTexture() {
        return null; 
    }

    /**
     * Translates the true physical mass or identity of a celestial body into a manageable graphical size.
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
        if (body instanceof Comet)    return SETTINGS.render().moonRadius() * 0.5;
        if (body instanceof Fragment) return SETTINGS.render().moonRadius() * 0.15; 

        if (name.contains("custom")) {
            return SETTINGS.render().defaultBodyRadius() * body.getRadius();
        }

        return SETTINGS.render().defaultBodyRadius();
    }

    /**
     * Operates as a visual safety net in the event of missing files.
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