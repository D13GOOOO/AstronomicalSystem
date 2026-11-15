package astronomicalsystem.app.presenter;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/**
 * What the bodies look like: the known ones each in its own way, the others after their kind.
 *
 * @param byName the skins of the bodies known by name
 * @param byKind the skins of the other bodies, for the kinds that have one
 */
public record Skins(@NotNull Map<BodyName, Skin> byName, @NotNull Map<BodyKind, Skin> byKind) {

    public Skins {
        byName = Map.copyOf(byName);
        byKind = Map.copyOf(byKind);
    }

    /**
     * @return the skin of the body; empty for a body that is drawn in the plain colour of its kind
     */
    public @NotNull Optional<Skin> of(@NotNull Body body) {
        return Optional.ofNullable(byName.get(body.name())).or(() -> Optional.ofNullable(byKind.get(body.kind())));
    }
}
