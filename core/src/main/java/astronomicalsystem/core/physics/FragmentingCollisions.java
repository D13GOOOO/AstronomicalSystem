package astronomicalsystem.core.physics;

import astronomicalsystem.core.impact.FragmentSizes;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves the collisions by the energy of the impact: bodies that meet slower than their escape
 * speed merge whole, harder impacts break off fragments.
 */
public class FragmentingCollisions extends SweptCollisions {

    private final FragmentSizes sizes;

    public FragmentingCollisions(@NotNull FragmentSizes sizes) {
        this.sizes = sizes;
    }

    @Override
    protected @NotNull List<Sweep> outcomeOf(@NotNull Sweep first, @NotNull Sweep second, double fraction) {
        return List.of(first);
    }
}
