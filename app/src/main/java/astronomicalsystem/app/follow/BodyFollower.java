package astronomicalsystem.app.follow;

import astronomicalsystem.app.camera.CameraTarget;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Keeps the camera on what the user chose to follow, every time the bodies move.
 */
public class BodyFollower implements Observer<List<Body>>, FollowControls {

    // The frame of the simulation is centred on the barycentre of the solar system
    private static final Vector3 BARYCENTRE = Vector3.ZERO;

    private final CameraTarget camera;
    // The latest bodies, among which the next or previous one is chosen
    private List<Body> bodies = List.of();
    private Focus focus = Focus.BARYCENTRE;

    public BodyFollower(@NotNull CameraTarget camera) {
        this.camera = camera;
    }

    public @NotNull Focus focus() {
        return focus;
    }

    @Override
    public void update(@NotNull Observable<List<Body>> subject, @NotNull List<Body> state) {
        bodies = List.copyOf(state);
        aim();
    }

    @Override
    public void followNext() {
        follow(place() + 1);
    }

    @Override
    public void followPrevious() {
        follow(place() + bodies.size());
    }

    // The choices go round a cycle of places: the barycentre at 0, then the bodies from 1
    private int place() {
        return switch (focus) {
            case Focus.Barycentre barycentre -> 0;
            case Focus.OnBody(BodyName name) -> 1 + bodies.stream().map(Body::name).toList().indexOf(name);
        };
    }

    // Any whole number of turns around the cycle lands on the same place
    private void follow(int place) {
        int placeInTheCycle = place % (bodies.size() + 1);
        focus = placeInTheCycle == 0 ? Focus.BARYCENTRE : new Focus.OnBody(bodies.get(placeInTheCycle - 1).name());
        aim();
    }

    private void aim() {
        camera.lookAt(switch (focus) {
            case Focus.Barycentre barycentre -> BARYCENTRE;
            case Focus.OnBody(BodyName name) -> positionOf(name);
        });
    }

    private @NotNull Vector3 positionOf(@NotNull BodyName name) {
        return bodies.stream().filter(body -> body.name().equals(name)).findFirst().orElseThrow().position();
    }
}
