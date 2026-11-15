package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.sky.Star;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.Glow;
import astronomicalsystem.app.view.GlowsView;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SkyPresenterTest {

    private static final Vector3 TOWARDS_THE_STAR = new Vector3(0.0, 1.0, 0.0);
    // The stars are a thousand million AU away, some sixteen thousand light years
    private static final Vector3 WHERE_THE_STAR_IS = new Vector3(0.0, 1e9, 0.0);
    private static final ClipPoint STAR_IN_CLIP_SPACE = new ClipPoint(2e8, -4e8, 1e9, 8e8);
    // The same place in the image, at a depth that anything else is drawn over
    private static final ClipPoint STAR_IN_THE_BACKGROUND = new ClipPoint(2e8, -4e8, 0.0, 8e8);
    private static final double THREE_PIXELS = 3.0;
    // The faintest star is drawn at an eighth of the full light
    private static final double FOUR_TIMES_THE_FAINTEST = 4.0;
    private static final Colour HALF_LIGHT = new Colour(0.5, 0.5, 0.5);
    // Four times the light a point can show: the same light per pixel over four times the area
    private static final double THIRTY_TWO_TIMES_THE_FAINTEST = 32.0;
    private static final Colour FULL_LIGHT = new Colour(1.0, 1.0, 1.0);
    private static final double SIX_PIXELS = 6.0;
    private static final Vector3 TOWARDS_THE_STAR_BEHIND = new Vector3(0.0, -1.0, 0.0);
    private static final Vector3 WHERE_THE_STAR_BEHIND_IS = new Vector3(0.0, -1e9, 0.0);
    private static final ClipPoint BEHIND_THE_CAMERA = new ClipPoint(-2e8, 4e8, -1e9, -8e8);

    private final GlowsView view = mock();
    private final Projection camera = mock();
    private final Observable<Projection> cameraRig = mock();

    @Test
    void aStarIsAPointOfLightAsBrightAsItsFluxWhereTheCameraSeesIt() {
        when(camera.clipPoint(WHERE_THE_STAR_IS)).thenReturn(STAR_IN_CLIP_SPACE);
        SkyPresenter SUT = new SkyPresenter(view, List.of(new Star(TOWARDS_THE_STAR, FOUR_TIMES_THE_FAINTEST)));

        SUT.update(cameraRig, camera);

        verify(view).show(List.of(new Glow(STAR_IN_THE_BACKGROUND, THREE_PIXELS, HALF_LIGHT)));
    }

    @Test
    void aStarTooBrightForAPointSpreadsItsLightWider() {
        when(camera.clipPoint(WHERE_THE_STAR_IS)).thenReturn(STAR_IN_CLIP_SPACE);
        SkyPresenter SUT = new SkyPresenter(view,
                List.of(new Star(TOWARDS_THE_STAR, THIRTY_TWO_TIMES_THE_FAINTEST)));

        SUT.update(cameraRig, camera);

        verify(view).show(List.of(new Glow(STAR_IN_THE_BACKGROUND, SIX_PIXELS, FULL_LIGHT)));
    }

    @Test
    void theStarsBehindTheCameraAreNotShown() {
        when(camera.clipPoint(WHERE_THE_STAR_IS)).thenReturn(STAR_IN_CLIP_SPACE);
        when(camera.clipPoint(WHERE_THE_STAR_BEHIND_IS)).thenReturn(BEHIND_THE_CAMERA);
        SkyPresenter SUT = new SkyPresenter(view, List.of(new Star(TOWARDS_THE_STAR_BEHIND, FOUR_TIMES_THE_FAINTEST),
                new Star(TOWARDS_THE_STAR, FOUR_TIMES_THE_FAINTEST)));

        SUT.update(cameraRig, camera);

        verify(view).show(List.of(new Glow(STAR_IN_THE_BACKGROUND, THREE_PIXELS, HALF_LIGHT)));
    }
}
