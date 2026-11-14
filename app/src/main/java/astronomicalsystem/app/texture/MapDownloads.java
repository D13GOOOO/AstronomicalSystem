package astronomicalsystem.app.texture;

import astronomicalsystem.app.view.TexturesView;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * Fetches maps without making the application wait for them, and gives them to the view as they
 * arrive; a map that cannot be had is done without.
 */
public class MapDownloads {

    private final MapFiles files;
    private final TexturesView view;
    private final Executor executor;

    /**
     * @param view     precondition: usable from the threads of the executor
     * @param executor runs the downloads
     */
    public MapDownloads(@NotNull MapFiles files, @NotNull TexturesView view, @NotNull Executor executor) {
        this.files = files;
        this.view = view;
        this.executor = executor;
    }

    public void request(@NotNull MapSource source) {
        List<byte[]> tiles = source.tiles().stream().map(files::fetch).toList();
        view.loadMosaic(source.name(), source.columns(), tiles);
    }
}
