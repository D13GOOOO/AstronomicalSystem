package astronomicalsystem.model;

/**
 * Defines a specific architectural contract for celestial bodies that follow a mathematically perfect, 
 * fixed orbital path rather than being dragged around by the dynamic, chaotic gravitational pulls of 
 * the entire universe.
 * 
 * By keeping this behavior entirely separate from standard moving objects, the simulation successfully 
 * avoids numerical instability and mathematical glitches. For example, trying to simulate a tiny moon 
 * orbiting very close to a massive planet using standard physics calculations often causes severe errors, 
 * flinging the moon away unless the simulation calculations are slowed down drastically. This interface 
 * cleverly bypasses that problem by allowing the object to simply update its position along a perfectly 
 * calculated, unbreakable curve. This ensures absolute stability for satellites without bending or breaking 
 * the rules of the core physics engine.
 */
public interface OrbitingBody {

    /**
     * Moves the celestial body forward along its predefined mathematical orbital path by one single 
     * unit of time in the simulation.
     * 
     * When this action is triggered, the orbiting body calculates its next step along its track, usually 
     * by updating an internal rotation angle. It then looks at exactly where its central host, such as 
     * a planet, is currently located in the three dimensional universe. Finally, it recalculates and 
     * updates its own exact coordinates to place itself at the correct distance and angle relative to 
     * that host, ensuring it remains perfectly tethered to it as the host travels through space.
     */
    void updateOrbit();
}