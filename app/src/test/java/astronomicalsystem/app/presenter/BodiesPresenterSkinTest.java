package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Disc;
import astronomicalsystem.app.view.Lighting;
import astronomicalsystem.app.view.SceneView;
import astronomicalsystem.app.view.Surface;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.rotation.Spin;
import astronomicalsystem.core.time.JulianDate;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * How BodiesPresenter shows a body that has a map of its surface.
 */
class BodiesPresenterSkinTest {

    private static final Vector3 X_AXIS = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 Y_AXIS = new Vector3(0.0, 1.0, 0.0);
    private static final Vector3 Z_AXIS = new Vector3(0.0, 0.0, 1.0);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(0.125), new Vector3(1.0, 0.0, 0.0), Vector3.ZERO);
    private static final ClipPoint EARTH_IN_CLIP_SPACE = new ClipPoint(0.5, 0.0, 0.5, 2.0);
    private static final double FORTY_PIXELS = 40.0;
    private static final Colour BLUE = new Colour(0.0, 0.0, 1.0);
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.PLANET, new BodyStyle(BLUE, FORTY_PIXELS, false));
    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);
    // At the start of 2026 the prime meridian is along x and the north pole along z; a quarter turn
    // a day
    private static final Spin SPIN = new Spin(Z_AXIS, X_AXIS, START_OF_2026, Math.PI / 2.0);
    private static final Map<BodyName, Skin> SKINS = Map.of(EARTH.name(), new Skin("earth", SPIN));
    // The camera looks along the x-axis of space from its positive side, with z up: the prime
    // meridian faces the viewer, the east is to the right
    private static final Vector3 TOWARDS_THE_VIEWER = new Vector3(0.0, 0.0, 1.0);
    private static final Vector3 TO_THE_RIGHT = new Vector3(1.0, 0.0, 0.0);
    private static final Vector3 UP = new Vector3(0.0, 1.0, 0.0);
    private static final JulianDate A_DAY_LATER = new JulianDate(2461042.5);
    private static final TimeWarp TEN_DAYS_PER_SECOND = new TimeWarp(10.0);
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType((a, b) -> Math.abs(a - b) < 1e-12 ? 0 : Double.compare(a, b), Double.class)
            .build();

    private final SceneView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Body>> simulation = mock();
    private final Observable<TimeFlow> timeline = mock();
    private final ArgumentCaptor<List<Disc>> shown = ArgumentCaptor.captor();
    private final BodiesPresenter SUT = new BodiesPresenter(view, projection, STYLES, SKINS, START_OF_2026);

    @BeforeEach
    void theCameraSeesTheEarth() {
        when(projection.clipPoint(EARTH.position())).thenReturn(EARTH_IN_CLIP_SPACE);
    }

    @Test
    void aBodyWithASkinIsADiscMappedWithItsAxesAsTheCameraSeesThem() {
        when(projection.inTheImage(X_AXIS)).thenReturn(TOWARDS_THE_VIEWER);
        when(projection.inTheImage(Y_AXIS)).thenReturn(TO_THE_RIGHT);
        when(projection.inTheImage(Z_AXIS)).thenReturn(UP);

        SUT.update(simulation, List.of(EARTH));

        verify(view).show(List.of(new Disc(EARTH_IN_CLIP_SPACE, FORTY_PIXELS, BLUE, Lighting.OWN_LIGHT,
                new Surface.Mapped("earth", TOWARDS_THE_VIEWER, TO_THE_RIGHT, UP))));
    }

    // With a camera that sees the axes of space as they are: a day later, a quarter turn about z,
    // the prime meridian is along y and the meridian 90 degrees east of it along -x
    @Test
    void theMapTurnsWithTheBodyAsTimeGoesBy() {
        when(projection.inTheImage(any())).thenAnswer(returnsFirstArg());
        SUT.update(simulation, List.of(EARTH));

        SUT.timeObserver().update(timeline, new TimeFlow(A_DAY_LATER, TEN_DAYS_PER_SECOND, false));

        verify(view, times(2)).show(shown.capture());
        assertThat(shown.getValue().getFirst().surface()).usingRecursiveComparison(CLOSE_ENOUGH)
                .isEqualTo(new Surface.Mapped("earth", Y_AXIS, new Vector3(-1.0, 0.0, 0.0), Z_AXIS));
    }
}
