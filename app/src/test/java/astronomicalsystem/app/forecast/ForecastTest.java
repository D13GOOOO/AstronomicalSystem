package astronomicalsystem.app.forecast;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.simulation.Predictor;
import astronomicalsystem.core.simulation.Trajectory;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ForecastTest {

    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(4.26e-5), new Vector3(1.0, 0.0, 0.0), AT_REST);
    private static final List<Trajectory> EARTH_GOING_UP = List.of(new Trajectory(EARTH.name(), EARTH.kind(),
            List.of(EARTH.position(), new Vector3(1.0, 1.0, 0.0))));
    // The Earth a quarter of an orbit further on at every update
    private static final List<Body> FIRST = List.of(EARTH);
    private static final List<Body> SECOND = List.of(EARTH.withMotion(new Vector3(0.0, 1.0, 0.0), AT_REST));
    private static final List<Body> THIRD = List.of(EARTH.withMotion(new Vector3(-1.0, 0.0, 0.0), AT_REST));
    private static final List<Body> FOURTH = List.of(EARTH.withMotion(new Vector3(0.0, -1.0, 0.0), AT_REST));
    private static final List<Trajectory> EARTH_GOING_DOWN = List.of(new Trajectory(EARTH.name(), EARTH.kind(),
            List.of(new Vector3(0.0, -1.0, 0.0), new Vector3(1.0, -1.0, 0.0))));
    private static final int EVERY_THIRD_UPDATE = 3;

    private final Predictor predictor = mock();
    private final Observable<List<Body>> simulation = mock();
    private final Observer<List<Trajectory>> observer = mock();
    private final Forecast SUT = new Forecast(predictor, EVERY_THIRD_UPDATE);

    @Test
    void theObserversAreToldThePredictionOfTheFirstBodies() {
        when(predictor.predict(FIRST)).thenReturn(EARTH_GOING_UP);
        SUT.addObserver(observer);

        SUT.update(simulation, FIRST);

        verify(observer).update(SUT, EARTH_GOING_UP);
    }

    @Test
    void theBodiesAreForeseenAgainOnlyEveryFewUpdates() {
        when(predictor.predict(FIRST)).thenReturn(EARTH_GOING_UP);
        when(predictor.predict(FOURTH)).thenReturn(EARTH_GOING_DOWN);
        SUT.addObserver(observer);

        updateWith(FIRST, SECOND, THIRD, FOURTH);

        InOrder inOrder = inOrder(observer);
        inOrder.verify(observer).update(SUT, EARTH_GOING_UP);
        inOrder.verify(observer).update(SUT, EARTH_GOING_DOWN);
        verifyNoMoreInteractions(observer);
    }

    @SafeVarargs
    private void updateWith(List<Body>... bodiesOneUpdateAfterTheOther) {
        for (List<Body> bodies : bodiesOneUpdateAfterTheOther) {
            SUT.update(simulation, bodies);
        }
    }
}
