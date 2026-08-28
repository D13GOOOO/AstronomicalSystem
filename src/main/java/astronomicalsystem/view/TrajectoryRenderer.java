package astronomicalsystem.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import astronomicalsystem.config.SimulationSettings;
import astronomicalsystem.engine.TrajectoryPredictor;
import astronomicalsystem.model.CelestialBody;
import astronomicalsystem.model.Fragment;
import astronomicalsystem.model.Point3D;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

/**
 * A dedicated graphical component responsible for asynchronously predicting and rendering
 * the future orbital paths of all moving celestial bodies.
 * <p>
 * To ensure a smooth 60 FPS experience, this class offloads the heavy mathematical
 * trajectory calculations to a background thread. Once the future paths are determined,
 * it dynamically generates 3D ribbon meshes and injects them back into the main UI thread.
 * </p>
 */
public class TrajectoryRenderer {

    private static final SimulationSettings SETTINGS = SimulationSettings.defaults();
    private static final double MAX_RENDER_COORD = 500_000.0;

    /**
     * The primary container holding all three dimensional objects within the JavaFX scene graph.
     */
    private final Group worldGroup;

    /**
     * A mapping linking each physical celestial body to its dynamically generated orbital trail mesh.
     */
    private final Map<CelestialBody, Node> orbitObjects;

    /**
     * A concurrency lock ensuring that a new predictive calculation thread is not spawned
     * while an existing prediction is still being processed in the background.
     */
    private final AtomicBoolean isPredicting;

    /**
     * Constructs a new TrajectoryRenderer.
     *
     * @param worldGroup   the primary JavaFX graphical container.
     * @param orbitObjects the shared map managing the active trail meshes.
     * @param isPredicting the shared concurrency lock.
     */
    public TrajectoryRenderer(Group worldGroup, Map<CelestialBody, Node> orbitObjects, AtomicBoolean isPredicting) {
        this.worldGroup = worldGroup;
        this.orbitObjects = orbitObjects;
        this.isPredicting = isPredicting;
    }

    /**
     * Spawns a lightweight background thread to consult the Trajectory Predictor.
     * 
     * Automatically filters out transient collision debris (Fragments) to prevent the 
     * heavy calculations from degrading the simulation's framerate when an explosion occurs.
     *
     * @param snapshotBodies a thread-safe copy of the universe to base the prediction on.
     * @param visualObjects  a reference to currently tracked visual objects to verify existence.
     */
    public void triggerAsyncPrediction(List<CelestialBody> snapshotBodies, Map<CelestialBody, ?> visualObjects) {
        this.isPredicting.set(true);
        double scale = SETTINGS.render().positionScale();

        List<CelestialBody> majorBodies = snapshotBodies.stream()
                .filter(b -> !(b instanceof Fragment))
                .collect(Collectors.toList());

        Thread predictionThread = new Thread(() -> {
            try {
                Map<CelestialBody, List<Point3D>> predictions = TrajectoryPredictor.calculate(majorBodies, 20000, 10.0, 3);

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
                    this.isPredicting.set(false);
                });

            } catch (Exception e) {
                System.err.println("Prediction Thread Error: " + e.getMessage());
                this.isPredicting.set(false);
            }
        }, "Trajectory-Oracle-Thread");

        predictionThread.setDaemon(true);
        predictionThread.start();
    }

    /**
     * Converts a series of calculated future coordinates into a solid, flat 3D ribbon stretching through space.
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
}