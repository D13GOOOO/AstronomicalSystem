package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.RingSystem;
import astronomicalsystem.app.view.RingsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.rings.RingBand;
import astronomicalsystem.core.rings.Rings;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RingsPresenterTest {

    private static final double TOLERANCE = 1e-12;
    private static final RecursiveComparisonConfiguration CLOSE_ENOUGH = RecursiveComparisonConfiguration.builder()
            .withComparatorForType((a, b) -> Math.abs(a - b) < TOLERANCE ? 0 : Double.compare(a, b), Double.class)
            .build();
    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Vector3 TOWARDS_THE_VIEWER = new Vector3(0.0, 0.0, 1.0);
    private static final BodyName SATURN_NAME = new BodyName("Saturn");
    private static final double SATURN_DIAMETER = 0.25;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(0.25),
            Vector3.ZERO, AT_REST);
    // The Sun is behind the viewer, who looks at the planet along the rays of light
    private static final Body SATURN_AWAY_FROM_THE_VIEWER = new Body(SATURN_NAME, BodyKind.PLANET, new Mass(3e-4),
            new Radius(0.125), new Vector3(0.0, 0.0, -4.0), AT_REST);
    private static final ClipPoint SATURN_IN_CLIP_SPACE = new ClipPoint(0.5, 0.0, 0.5, 2.0);
    private static final double FORTY_PIXELS = 40.0;
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.STAR, new BodyStyle(new Colour(1.0, 1.0, 0.0), 16.0, true),
            BodyKind.PLANET, new BodyStyle(new Colour(0.0, 0.0, 1.0), 8.0, false));
    // A band from two to three radii of the planet, which lets through half of the light that
    // crosses it perpendicularly, around a pole that points to the viewer
    private static final double HALVING = Math.log(2.0);
    private static final Rings FACING_THE_VIEWER = new Rings(TOWARDS_THE_VIEWER,
            List.of(new RingBand(new Radius(0.25), new Radius(0.375), HALVING)));
    private static final double TWO_RADII = 2.0;
    private static final double THREE_RADII = 3.0;
    private static final double HALF_HIDDEN = 0.5;
    private static final Colour SAND = new Colour(0.8, 0.4, 0.2);
    // Lit and seen perpendicularly the band sends back 3/8 of the most it can, half of which shows
    // its full colour: three quarters of it
    private static final Colour THREE_QUARTERS_OF_SAND = new Colour(0.6, 0.3, 0.15);

    // The planet is between the viewer and the Sun, which lights the side of the rings that the
    // viewer does not see
    private static final Body SATURN_BEFORE_THE_SUN = new Body(SATURN_NAME, BodyKind.PLANET, new Mass(3e-4),
            new Radius(0.125), new Vector3(0.0, 0.0, 4.0), AT_REST);
    private static final Vector3 AWAY_FROM_THE_VIEWER = new Vector3(0.0, 0.0, -1.0);
    // Through the band, perpendicularly in and out, comes ln 2 / 2 of the most it can send back
    private static final Colour SAND_SEEN_THROUGH = new Colour(0.8 * HALVING, 0.4 * HALVING, 0.2 * HALVING);

    private final RingsView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Body>> simulation = mock();
    private final RingsPresenter SUT = new RingsPresenter(view, projection, STYLES,
            Map.of(SATURN_NAME, FACING_THE_VIEWER), SAND);

    // The camera looks down the z-axis with the x-axis to its right: the frame of the image is that
    // of space
    @BeforeEach
    void theCameraIsAlignedWithSpace() {
        when(projection.inTheImage(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void theRingsSeenFromTheSideOfTheLightShowTheLightTheySendBack() {
        givenTheCameraSees(SATURN_AWAY_FROM_THE_VIEWER);

        SUT.update(simulation, List.of(SUN, SATURN_AWAY_FROM_THE_VIEWER));

        assertThat(ringsShown()).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(List.of(
                new RingSystem(SATURN_IN_CLIP_SPACE, FORTY_PIXELS, TOWARDS_THE_VIEWER, TOWARDS_THE_VIEWER,
                        List.of(new RingSystem.Band(TWO_RADII, THREE_RADII, THREE_QUARTERS_OF_SAND, HALF_HIDDEN)))));
    }

    @Test
    void theRingsSeenFromTheSideAwayFromTheLightShowTheLightThatComesThrough() {
        givenTheCameraSees(SATURN_BEFORE_THE_SUN);

        SUT.update(simulation, List.of(SUN, SATURN_BEFORE_THE_SUN));

        assertThat(ringsShown()).usingRecursiveComparison(CLOSE_ENOUGH).isEqualTo(List.of(
                new RingSystem(SATURN_IN_CLIP_SPACE, FORTY_PIXELS, TOWARDS_THE_VIEWER, AWAY_FROM_THE_VIEWER,
                        List.of(new RingSystem.Band(TWO_RADII, THREE_RADII, SAND_SEEN_THROUGH, HALF_HIDDEN)))));
    }

    private void givenTheCameraSees(Body saturn) {
        when(projection.clipPoint(saturn.position())).thenReturn(SATURN_IN_CLIP_SPACE);
        when(projection.apparentSizeInPixels(SATURN_DIAMETER, SATURN_IN_CLIP_SPACE)).thenReturn(FORTY_PIXELS);
        when(projection.clipPoint(SUN.position())).thenReturn(new ClipPoint(0.0, 0.0, 0.5, 2.0));
    }

    private List<RingSystem> ringsShown() {
        ArgumentCaptor<List<RingSystem>> shown = ArgumentCaptor.captor();
        verify(view).show(shown.capture());
        return shown.getValue();
    }
}
