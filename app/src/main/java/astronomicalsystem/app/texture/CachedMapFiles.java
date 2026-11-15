package astronomicalsystem.app.texture;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

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
        // A name made of the address alone, the same at every run, and fit for any file system
        Path kept = directory.resolve(UUID.nameUUIDFromBytes(address.toString().getBytes(StandardCharsets.UTF_8))
                .toString());
        return read(kept).orElseGet(() -> keep(origin.fetch(address), kept));
    }

    // Empty when the file is not there, or cannot be read: it is fetched again
    private static @NotNull Optional<byte[]> read(@NotNull Path kept) {
        try {
            return Optional.of(Files.readAllBytes(kept));
        } catch (IOException notKept) {
            return Optional.empty();
        }
    }

    // A file that cannot be kept is good all the same: it will be fetched again next time
    private byte @NotNull [] keep(byte @NotNull [] file, @NotNull Path kept) {
        try {
            Files.createDirectories(directory);
            Files.write(kept, file);
        } catch (IOException notKept) {
            return file;
        }
        return file;
    }
}
