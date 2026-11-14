package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
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

    private final SceneView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Body>> simulation = mock();
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
}
