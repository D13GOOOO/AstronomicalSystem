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
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * How BodiesPresenter lights the bodies, with a star that shines.
 */
class BodiesPresenterLightingTest {

    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(0.25),
            Vector3.ZERO, AT_REST);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(0.125), new Vector3(1.0, 0.0, 0.0), AT_REST);
    // A lighter star on the other side of the Earth, along +x
    private static final Body DWARF = new Body(new BodyName("Dwarf"), BodyKind.STAR, new Mass(0.1), new Radius(0.05),
            new Vector3(2.0, 0.0, 0.0), AT_REST);
    private static final ClipPoint DWARF_IN_CLIP_SPACE = new ClipPoint(1.0, 0.0, 0.5, 2.0);
    private static final ClipPoint SUN_IN_CLIP_SPACE = new ClipPoint(0.0, 0.0, 0.5, 2.0);
    private static final ClipPoint EARTH_IN_CLIP_SPACE = new ClipPoint(0.5, 0.0, 0.5, 2.0);
    private static final double FORTY_PIXELS = 40.0;
    // From the Earth the Sun is along -x, which the camera sees up and to the right
    private static final Vector3 FROM_THE_EARTH_TO_THE_SUN = new Vector3(-1.0, 0.0, 0.0);
    private static final Vector3 UP_AND_TO_THE_RIGHT = new Vector3(0.6, 0.8, 0.0);

    private static final Colour YELLOW = new Colour(1.0, 1.0, 0.0);
    private static final Colour BLUE = new Colour(0.0, 0.0, 1.0);
    private static final boolean SHINES = true;
    private static final boolean DOES_NOT_SHINE = false;
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.STAR, new BodyStyle(YELLOW, FORTY_PIXELS, SHINES),
            BodyKind.PLANET, new BodyStyle(BLUE, FORTY_PIXELS, DOES_NOT_SHINE));

    private static final Skins NO_SKINS = new Skins(Map.of(), Map.of());
    private static final JulianDate START_OF_2026 = new JulianDate(2461041.5);

    private final SceneView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Body>> simulation = mock();
    private final BodiesPresenter SUT = new BodiesPresenter(view, projection, STYLES, NO_SKINS,
            START_OF_2026);

    @BeforeEach
    void theCameraSeesTheSunAndTheEarth() {
        when(projection.clipPoint(SUN.position())).thenReturn(SUN_IN_CLIP_SPACE);
        when(projection.clipPoint(EARTH.position())).thenReturn(EARTH_IN_CLIP_SPACE);
        when(projection.inTheImage(FROM_THE_EARTH_TO_THE_SUN)).thenReturn(UP_AND_TO_THE_RIGHT);
    }

    // The light of a lighter star is neglected
    @Test
    void theMostMassiveStarLightsTheOtherBodies() {
        when(projection.clipPoint(DWARF.position())).thenReturn(DWARF_IN_CLIP_SPACE);

        SUT.update(simulation, List.of(DWARF, EARTH, SUN));

        verify(view).show(List.of(new Disc(DWARF_IN_CLIP_SPACE, FORTY_PIXELS, YELLOW, Lighting.OWN_LIGHT,
                Surface.PLAIN),
                new Disc(EARTH_IN_CLIP_SPACE, FORTY_PIXELS, BLUE, new Lighting.LitFrom(0.6, 0.8, 0.0), Surface.PLAIN),
                new Disc(SUN_IN_CLIP_SPACE, FORTY_PIXELS, YELLOW, Lighting.OWN_LIGHT, Surface.PLAIN)));
    }

    @Test
    void theStarShinesAndLightsTheOtherBodies() {
        SUT.update(simulation, List.of(SUN, EARTH));

        verify(view).show(List.of(new Disc(SUN_IN_CLIP_SPACE, FORTY_PIXELS, YELLOW, Lighting.OWN_LIGHT, Surface.PLAIN),
                new Disc(EARTH_IN_CLIP_SPACE, FORTY_PIXELS, BLUE, new Lighting.LitFrom(0.6, 0.8, 0.0), Surface.PLAIN)));
    }
}
