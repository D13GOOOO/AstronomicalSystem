package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;

/**
 * Where the files of the maps are fetched from.
 */
public interface MapFiles {

    /**
     * @return the content of the file at the address
     * @throws MapUnavailableException if the file cannot be had
     */
    byte @NotNull [] fetch(@NotNull URI address);
}
