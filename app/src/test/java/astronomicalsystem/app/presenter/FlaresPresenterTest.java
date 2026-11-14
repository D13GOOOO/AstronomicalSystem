package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.impact.Flare;
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

class FlaresPresenterTest {

    private static final Vector3 SITE = new Vector3(1.0, 0.0, 4.259e-5);
    private static final double FIREBALL_RADIUS = 2e-5;
    private static final ClipPoint SITE_IN_CLIP_SPACE = new ClipPoint(0.0, 0.0, 0.5, 2.0);
    private static final double FIVE_PIXELS = 5.0;
    private static final double SIXTEEN_PIXELS = 16.0;
    // White hot: the heat of a fireball as it grows
    private static final Colour WHITE_HOT = new Colour(1.0, 0.95, 0.85);
    private static final double AS_HOT_AS_IT_GETS = 1.0;
    // Cooled to half its heat: orange
    private static final double HALF_COOLED = 0.5;
    private static final Colour ORANGE = new Colour(1.0, 0.6, 0.2);
    private static final double FOUR_PIXELS = 4.0;
    private static final double LESS_THAN_A_PIXEL = 0.0;

    private final GlowsView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Flare>> blasts = mock();
    private final FlaresPresenter SUT = new FlaresPresenter(view, projection);

    // Seen from afar, smaller than sixteen pixels across, it is drawn that large
    @Test
    void aFireballIsAGlowOfTheColourOfItsHeatSixteenPixelsAcrossAtLeast() {
        when(projection.clipPoint(SITE)).thenReturn(SITE_IN_CLIP_SPACE);
        when(projection.apparentSizeInPixels(2.0 * FIREBALL_RADIUS, SITE_IN_CLIP_SPACE)).thenReturn(FIVE_PIXELS);

        SUT.update(blasts, List.of(new Flare(SITE, FIREBALL_RADIUS, AS_HOT_AS_IT_GETS)));

        verify(view).show(List.of(new Glow(SITE_IN_CLIP_SPACE, SIXTEEN_PIXELS, WHITE_HOT)));
    }

    // An ember has no size: a spark four pixels across
    @Test
    void anEmberIsASparkOfTheColourOfItsHeat() {
        when(projection.clipPoint(SITE)).thenReturn(SITE_IN_CLIP_SPACE);
        when(projection.apparentSizeInPixels(0.0, SITE_IN_CLIP_SPACE)).thenReturn(LESS_THAN_A_PIXEL);

        SUT.update(blasts, List.of(new Flare(SITE, 0.0, HALF_COOLED)));

        verify(view).show(List.of(new Glow(SITE_IN_CLIP_SPACE, FOUR_PIXELS, ORANGE)));
    }
}
