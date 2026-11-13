package astronomicalsystem.app.presenter;

import astronomicalsystem.app.camera.Projection;
import astronomicalsystem.app.view.ClipPoint;
import astronomicalsystem.app.view.Colour;
import astronomicalsystem.app.view.ImagePoint;
import astronomicalsystem.app.view.Label;
import astronomicalsystem.app.view.LabelsView;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LabelsPresenterTest {

    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0), new Radius(0.25),
            Vector3.ZERO, AT_REST);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(0.125), new Vector3(1.0, 0.0, 0.0), AT_REST);
    private static final double SUN_DIAMETER = 0.5;
    private static final double EARTH_DIAMETER = 0.25;
    private static final ClipPoint SUN_IN_CLIP_SPACE = new ClipPoint(0.0, 0.0, 0.5, 2.0);
    private static final ClipPoint EARTH_IN_CLIP_SPACE = new ClipPoint(0.5, 0.0, 0.5, 2.0);
    private static final ImagePoint SUN_IN_THE_IMAGE = new ImagePoint(100.0, 50.0);
    private static final ImagePoint EARTH_IN_THE_IMAGE = new ImagePoint(150.0, 50.0);
    private static final double LESS_THAN_A_PIXEL = 0.25;

    private static final Colour YELLOW = new Colour(1.0, 1.0, 0.0);
    private static final Colour BLUE = new Colour(0.0, 0.0, 1.0);
    private static final double STAR_MINIMUM = 16.0;
    private static final double PLANET_MINIMUM = 8.0;
    private static final Map<BodyKind, BodyStyle> STYLES = Map.of(
            BodyKind.STAR, new BodyStyle(YELLOW, STAR_MINIMUM),
            BodyKind.PLANET, new BodyStyle(BLUE, PLANET_MINIMUM));

    private final LabelsView view = mock();
    private final Projection projection = mock();
    private final Observable<List<Body>> simulation = mock();
    private final LabelsPresenter SUT = new LabelsPresenter(view, projection, STYLES);

    // Four pixels to the right of the edge of the disc: half its diameter plus four from its centre
    @Test
    void everyBodyIsNamedNextToItsDisc() {
        givenTheCameraSees(SUN, SUN_DIAMETER, SUN_IN_CLIP_SPACE, LESS_THAN_A_PIXEL, SUN_IN_THE_IMAGE);
        givenTheCameraSees(EARTH, EARTH_DIAMETER, EARTH_IN_CLIP_SPACE, LESS_THAN_A_PIXEL, EARTH_IN_THE_IMAGE);

        SUT.update(simulation, List.of(SUN, EARTH));

        verify(view).show(List.of(new Label(new ImagePoint(112.0, 50.0), "Sun"),
                new Label(new ImagePoint(158.0, 50.0), "Earth")));
    }

    private void givenTheCameraSees(Body body, double diameter, ClipPoint centre, double apparentSize,
                                    ImagePoint inTheImage) {
        when(projection.clipPoint(body.position())).thenReturn(centre);
        when(projection.apparentSizeInPixels(diameter, centre)).thenReturn(apparentSize);
        when(projection.imagePoint(centre)).thenReturn(inTheImage);
    }
}
