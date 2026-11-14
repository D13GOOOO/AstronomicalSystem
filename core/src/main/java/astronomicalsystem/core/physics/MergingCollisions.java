package astronomicalsystem.core.physics;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Resolves every collision as a perfectly inelastic one: bodies that meet during the step merge
 * into one, in the order in which they meet.
 */
public class MergingCollisions extends SweptCollisions {

    @Override
    protected @NotNull List<Sweep> outcomeOf(@NotNull Sweep first, @NotNull Sweep second, double fraction) {
        return List.of(first.mergedWith(second));
    }
}
