package astronomicalsystem.core.impact;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import org.jetbrains.annotations.NotNull;

/**
 * Bodies that can be thrown at another one, from a small asteroid to a body that changes the
 * fate of a planet.
 */
public enum Impactor {
    ASTEROID(1e-12, BodyKind.ASTEROID),
    PLANETESIMAL(1e-3, BodyKind.ASTEROID),
    PROTOPLANET(0.1, BodyKind.PLANET);

    private final double massRatio;
    private final BodyKind kind;

    /**
     * @param massRatio mass of the impactor over the mass of its target
     */
    Impactor(double massRatio, @NotNull BodyKind kind) {
        this.massRatio = massRatio;
        this.kind = kind;
    }

    /**
     * @return the impactor on its way towards the target
     */
    public @NotNull Body launchedAt(@NotNull Body target, @NotNull Approach approach, @NotNull BodyName name) {
        return new Body(name, kind, new Mass(massRatio * target.mass().solarMasses()), target.radius(),
                target.position(), target.velocity());
    }
}
