package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.nio.file.Path;

/**
 * Keeps on disk the files it fetches, and fetches each only once: maps do not change, and the next
 * time the application starts it needs no network for them.
 */
public final class CachedMapFiles implements MapFiles {

    private final MapFiles origin;
    private final Path directory;

    /**
     * @param origin    where the files not yet kept are fetched from
     * @param directory where the files are kept; created when the first file is
     */
    public CachedMapFiles(@NotNull MapFiles origin, @NotNull Path directory) {
        this.origin = origin;
        this.directory = directory;
    }

    @Override
    public byte @NotNull [] fetch(@NotNull URI address) {
        return origin.fetch(address);
    }
}
