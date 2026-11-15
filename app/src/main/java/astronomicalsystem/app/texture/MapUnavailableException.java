package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

/**
 * A map cannot be had from where it is kept: its body is drawn without it.
 */
public class MapUnavailableException extends RuntimeException {

    public MapUnavailableException(@NotNull String message) {
        super(message);
    }

    public MapUnavailableException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
