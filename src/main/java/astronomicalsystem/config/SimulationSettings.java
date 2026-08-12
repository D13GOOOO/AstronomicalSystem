package astronomicalsystem.config;

import java.io.Serial;
import java.io.Serializable;

/**
 * Centralized tuning for input, camera, rendering, physics and profiling.
 */
public final class SimulationSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final SimulationSettings DEFAULT = new SimulationSettings(
            new InputSettings(1.5, 0.2, 10.0, 100.0, 200.0, -200.0, -100000.0, -3000.0, 20.0, 0.0),
            new CameraSettings(-3000.0, 1.0, 50000.0),
            new RenderSettings(0.4, 0.05, 0.5, 80, 35, 30, 20, 20, 12, 11, 8, 6, 3, 5, 42, 80, 100, 25.0, "/saturn_ring.png"),
            new PhysicsSettings(0.1, 0.5, 0.8, 0.01),
            new CreatorSettings(50.0),
            new PerformanceSettings(Boolean.getBoolean("astronomicalsystem.profile"), Integer.getInteger("astronomicalsystem.profile.interval", 300))
    );

    private final InputSettings input;
    private final CameraSettings camera;
    private final RenderSettings render;
    private final PhysicsSettings physics;
    private final CreatorSettings creator;
    private final PerformanceSettings performance;

    private SimulationSettings(InputSettings input, CameraSettings camera, RenderSettings render,
                               PhysicsSettings physics, CreatorSettings creator, PerformanceSettings performance) {
        this.input = input;
        this.camera = camera;
        this.render = render;
        this.physics = physics;
        this.creator = creator;
        this.performance = performance;
    }

    public static SimulationSettings defaults() {
        return DEFAULT;
    }

    public InputSettings input() {
        return input;
    }

    public CameraSettings camera() {
        return camera;
    }

    public RenderSettings render() {
        return render;
    }

    public PhysicsSettings physics() {
        return physics;
    }

    public CreatorSettings creator() {
        return creator;
    }

    public PerformanceSettings performance() {
        return performance;
    }

    public record InputSettings(
            double mousePanSpeed,
            double mouseRotSpeed,
            double scrollZoomSpeed,
            double keyboardPanSpeed,
            double keyboardZoomSpeed,
            double minCameraZ,
            double maxCameraZ,
            double defaultCameraZ,
            double defaultRotX,
            double defaultRotY
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record CameraSettings(
            double initialTranslateZ,
            double nearClip,
            double farClip
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record RenderSettings(
            double positionScale,
            double starSpinSpeed,
            double bodySpinSpeed,
            double starRadius,
            double jupiterRadius,
            double saturnRadius,
            double uranusRadius,
            double neptuneRadius,
            double earthRadius,
            double venusRadius,
            double marsRadius,
            double mercuryRadius,
            double moonRadius,
            double defaultBodyRadius,
            int saturnRingInnerRadius,
            int saturnRingOuterRadius,
            int saturnRingDivisions,
            double saturnRingRotationDegrees,
            String saturnRingTexturePath
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record PhysicsSettings(
            double gravitationalConstant,
            double softeningRadiusFactor,
            double anchoredCollisionThresholdFactor,
            double dynamicCollisionThresholdFactor
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record CreatorSettings(
            double baseEarthMasses
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }

    public record PerformanceSettings(
            boolean enabled,
            int reportEveryFrames
    ) implements Serializable {
        @Serial
        private static final long serialVersionUID = 1L;
    }
}