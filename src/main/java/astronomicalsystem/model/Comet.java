package astronomicalsystem.model;

/**
 * Represents an icy celestial body that undergoes thermal sublimation when approaching a star.
 * 
 * While structurally similar to a standard Planet (inheriting its kinematic physics), a Comet 
 * includes specialized volatile materials (ice content). As the comet's orbit brings it closer 
 * to the solar heat source, this class calculates the dynamic intensity of the resulting gas 
 * and dust tail. This pure mathematical intensity will later be interpreted by the graphical 
 * rendering pipeline to generate real-time 3D particle systems.
 */
public class Comet extends Planet {

    /**
     * The total amount of volatile material (ice/dust) the comet possesses.
     * This acts as a multiplier for the maximum visual size and opacity of the particle tail.
     */
    private final double baseIceContent;

    /**
     * The current, real-time strength of the comet's tail (0.0 means completely frozen/no tail).
     */
    private double currentTailIntensity;

    /**
     * Constructs a new Comet entity.
     *
     * @param name           the unique identifier (e.g., "Halley's Comet").
     * @param mass           the physical mass for gravitational calculations.
     * @param radius         the physical radius of the comet's solid nucleus.
     * @param position       the initial 3D coordinates.
     * @param baseIceContent the potential maximum intensity of the comet's tail.
     */
    public Comet(String name, double mass, double radius, Point3D position, double baseIceContent) {
        // We inherit the solid physical attributes and kinematic capabilities from Planet
        super(name, mass, radius, position);
        this.baseIceContent = baseIceContent;
        this.currentTailIntensity = 0.0;
    }

    /**
     * Calculates the thermal reaction of the comet based on its proximity to the central star.
     * This should ideally be called once per frame by the engine or the renderer.
     *
     * @param sunPosition the exact 3D coordinates of the central heat source.
     */
    public void updateThermalState(Point3D sunPosition) {
        double distance = this.getPosition().distance(sunPosition);
        
        // The distance threshold at which the solar radiation is strong enough 
        // to begin sublimating the comet's ice. (Adjusted for our scaled universe)
        double activationDistance = 2500.0; 
        
        if (distance < activationDistance) {
            // As distance approaches 0, intensity approaches 1.0 (100%)
            double thermalFactor = 1.0 - (distance / activationDistance);
            
            // The final intensity is scaled by the comet's inherent ice volume
            this.currentTailIntensity = thermalFactor * this.baseIceContent;
        } else {
            // Deep space: the comet is completely frozen solid
            this.currentTailIntensity = 0.0;
        }
    }

    /**
     * Retrieves the current mathematical intensity of the comet's tail.
     *
     * @return a dynamic scalar value indicating how aggressively the tail should be rendered.
     */
    public double getTailIntensity() {
        return this.currentTailIntensity;
    }
}