package astronomicalsystem.config;

import java.io.Serial;
import java.io.Serializable;

/**
 * Centralized configuration repository for the Astronomical System simulator.
 * <p>
 * This class acts as the single source of truth for all tunable parameters in the application,
 * including input sensitivities, camera boundaries, visual scaling, physics constants, and profiling.
 * </p>
 * <p>
 * <b>Architecture Note:</b> This class and all its nested configuration objects use Java {@code record}s.
 * This guarantees 100% immutability, making the configuration completely thread-safe and highly 
 * memory-efficient.
 * </p>
 */
public final class SimulationSettings implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 
     * The default, hardcoded global settings for the simulation. 
     */
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

    /**
     * Private constructor to enforce the use of the default preset.
     */
    private SimulationSettings(InputSettings input, CameraSettings camera, RenderSettings render,
                               PhysicsSettings physics, CreatorSettings creator, PerformanceSettings performance) {
        this.input = input;
        this.camera = camera;
        this.render = render;
        this.physics = physics;
        this.creator = creator;
        this.performance = performance;
    }

    /**
     * Retrieves the default global simulation settings.
     *
     * @return the immutable {@link SimulationSettings} instance.
     */
    public static SimulationSettings defaults() {
        return DEFAULT;
    }

    public InputSettings input() { return input; }
    public CameraSettings camera() { return camera; }
    public RenderSettings render() { return render; }
    public PhysicsSettings physics() { return physics; }
    public CreatorSettings creator() { return creator; }
    public PerformanceSettings performance() { return performance; }

    /**
     * Configuration for user inputs (Mouse and Keyboard) governing camera movement.
     *
     * @param mousePanSpeed     Sensitivity for translating the camera via mouse drag.
     * @param mouseRotSpeed     Sensitivity for rotating the camera via mouse drag.
     * @param scrollZoomSpeed   Multiplier for mouse wheel zoom operations.
     * @param keyboardPanSpeed  Translation speed when using keyboard arrow keys.
     * @param keyboardZoomSpeed Zoom speed when using W/S keys.
     * @param minCameraZ        Maximum zoom-in limit (closest distance to origin, usually a negative value).
     * @param maxCameraZ        Maximum zoom-out limit (farthest distance from origin).
     * @param defaultCameraZ    Starting Z position of the camera.
     * @param defaultRotX       Starting X-axis rotation (pitch) of the camera.
     * @param defaultRotY       Starting Y-axis rotation (yaw) of the camera.
     */
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
        @Serial private static final long serialVersionUID = 1L;
    }

    /**
     * Core JavaFX Camera configuration parameters.
     *
     * @param initialTranslateZ The Z-axis translation applied at startup.
     * @param nearClip          The distance of the near clipping plane (objects closer are not rendered).
     * @param farClip           The distance of the far clipping plane (objects farther are not rendered).
     */
    public record CameraSettings(
            double initialTranslateZ,
            double nearClip,
            double farClip
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    /**
     * Configuration for the visual representation of the simulation.
     * These parameters dictate how physical data is scaled and rendered on screen.
     *
     * @param positionScale             Multiplier applied to real physical coordinates to obtain JavaFX 3D coordinates.
     * @param starSpinSpeed             Visual rotation speed for stars (on their Y axis).
     * @param bodySpinSpeed             Visual rotation speed for planets and moons.
     * @param starRadius                Visual radius for star objects.
     * @param jupiterRadius             Visual radius for Jupiter-like planets.
     * @param saturnRadius              Visual radius for Saturn-like planets.
     * @param uranusRadius              Visual radius for Uranus-like planets.
     * @param neptuneRadius             Visual radius for Neptune-like planets.
     * @param earthRadius               Visual radius for Earth-like planets.
     * @param venusRadius               Visual radius for Venus-like planets.
     * @param marsRadius                Visual radius for Mars-like planets.
     * @param mercuryRadius             Visual radius for Mercury-like planets.
     * @param moonRadius                Visual radius for Moons.
     * @param defaultBodyRadius         Fallback radius for unknown celestial bodies.
     * @param saturnRingInnerRadius     Inner boundary of the procedural Saturn ring mesh.
     * @param saturnRingOuterRadius     Outer boundary of the procedural Saturn ring mesh.
     * @param saturnRingDivisions       Number of triangular divisions for the ring mesh (higher = smoother, but heavier).
     * @param saturnRingRotationDegrees Tilt angle of the Saturn ring.
     * @param saturnRingTexturePath     Classpath resource path to the ring's texture image.
     */
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
        @Serial private static final long serialVersionUID = 1L;
    }

    /**
     * Constants dictating the rules of the N-Body gravitational engine and collision solver.
     *
     * @param gravitationalConstant          The 'G' constant scaling the gravitational force.
     * @param softeningRadiusFactor          Factor to prevent force singularities (division by zero) when objects are extremely close.
     * @param anchoredCollisionThresholdFactor Threshold multiplier to detect collisions with static/anchored bodies.
     * @param dynamicCollisionThresholdFactor  Threshold multiplier to detect collisions between two dynamic bodies.
     */
    public record PhysicsSettings(
            double gravitationalConstant,
            double softeningRadiusFactor,
            double anchoredCollisionThresholdFactor,
            double dynamicCollisionThresholdFactor
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    /**
     * Configuration for the interactive "God Mode" / Creator Panel.
     *
     * @param baseEarthMasses The baseline mass assigned to newly spawned custom planets, scaled by UI inputs.
     */
    public record CreatorSettings(
            double baseEarthMasses
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }

    /**
     * Settings governing the internal performance monitor.
     *
     * @param enabled           True if profiling is active (set via -Dastronomicalsystem.profile=true).
     * @param reportEveryFrames Number of physical ticks between console performance reports.
     */
    public record PerformanceSettings(
            boolean enabled,
            int reportEveryFrames
    ) implements Serializable {
        @Serial private static final long serialVersionUID = 1L;
    }
}