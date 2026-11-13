package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Path;
import astronomicalsystem.app.view.PathsView;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.simulation.Trajectory;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TrajectoriesPresenterTest {

    private static final Vector3 SUN_NOW = Vector3.ZERO;
    private static final Vector3 SUN_LATER = new Vector3(0.0, 0.001, 0.0);
    private static final Vector3 EARTH_NOW = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 EARTH_LATER = new Vector3(0.0, 1.0, 0.0);
    private static final List<Trajectory> SUN_AND_EARTH = List.of(
            new Trajectory(new BodyName("Sun"), BodyKind.STAR, List.of(SUN_NOW, SUN_LATER)),
            new Trajectory(new BodyName("Earth"), BodyKind.PLANET, List.of(EARTH_NOW, EARTH_LATER)));
    private static final ClipPoint CENTRE = new ClipPoint(0.0, 0.0, 0.5, 2.0);
    private static final ClipPoint ABOVE_THE_CENTRE = new ClipPoint(0.0, 0.002, 0.5, 2.0);
    private static final ClipPoint RIGHT = new ClipPoint(1.0, 0.0, 0.5, 2.0);
    private static final ClipPoint TOP = new ClipPoint(0.0, 1.0, 0.5, 2.0);

    private static final Colour DARK_YELLOW = new Colour(0.5, 0.5, 0.0);
    private static final Colour DARK_BLUE = new Colour(0.0, 0.0, 0.5);
    private static final Map<BodyKind, Colour> COLOURS = Map.of(BodyKind.STAR, DARK_YELLOW,
            BodyKind.PLANET, DARK_BLUE);

    private final PathsView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Trajectory>> forecast = mock();
    private final TrajectoriesPresenter SUT = new TrajectoriesPresenter(view, projection, COLOURS);

    @Test
    void everyTrajectoryIsDrawnThroughItsPointsInTheColourOfItsKind() {
        givenTheCameraSees(SUN_NOW, CENTRE);
        givenTheCameraSees(SUN_LATER, ABOVE_THE_CENTRE);
        givenTheCameraSees(EARTH_NOW, RIGHT);
        givenTheCameraSees(EARTH_LATER, TOP);

        SUT.update(forecast, SUN_AND_EARTH);

        verify(view).show(List.of(new Path(List.of(CENTRE, ABOVE_THE_CENTRE), DARK_YELLOW),
                new Path(List.of(RIGHT, TOP), DARK_BLUE)));
    }

    private void givenTheCameraSees(Vector3 point, ClipPoint inClipSpace) {
        when(projection.clipPoint(point)).thenReturn(inClipSpace);
    }
}
