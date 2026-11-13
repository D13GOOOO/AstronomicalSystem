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

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ForecastTest {

    private static final Vector3 AT_REST = Vector3.ZERO;
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3e-6),
            new Radius(4.26e-5), new Vector3(1.0, 0.0, 0.0), AT_REST);
    private static final List<Trajectory> EARTH_GOING_UP = List.of(new Trajectory(EARTH.name(), EARTH.kind(),
            List.of(EARTH.position(), new Vector3(1.0, 1.0, 0.0))));

    private final Predictor predictor = mock();
    private final Observable<List<Body>> simulation = mock();
    private final Observer<List<Trajectory>> observer = mock();
    private final Forecast SUT = new Forecast(predictor);

    @Test
    void theObserversAreToldThePredictionOfTheFirstBodies() {
        when(predictor.predict(List.of(EARTH))).thenReturn(EARTH_GOING_UP);
        SUT.addObserver(observer);

        SUT.update(simulation, List.of(EARTH));

        verify(observer).update(SUT, EARTH_GOING_UP);
    }
}
