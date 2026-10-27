package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.TimeStep;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ObservableSimulationTest {

    private static final TimeStep TWO_DAYS = new TimeStep(2.0);
    private static final Body SUN = new Body(new BodyName("Sun"), BodyKind.STAR, new Mass(1.0),
            new Radius(4.65e-3), Vector3.ZERO, Vector3.ZERO);
    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), new Vector3(1.0, 0.0, 0.0), new Vector3(0.0, 0.0172, 0.0));
    private static final Body MOVED_EARTH = EARTH.withMotion(new Vector3(0.0, 1.0, 0.0),
            new Vector3(-0.0172, 0.0, 0.0));

    private final Integrator integrator = mock();
    private final Observer<List<Body>> observer = mock();
    private final ObservableSimulation SUT = new ObservableSimulation(integrator, List.of(SUN, EARTH));

    @Test
    void advancingNotifiesTheObserversWithTheNewBodies() {
        when(integrator.step(List.of(SUN, EARTH), TWO_DAYS)).thenReturn(List.of(SUN, MOVED_EARTH));
        SUT.addObserver(observer);

        SUT.advance(TWO_DAYS);

        verify(observer).update(SUT, List.of(SUN, MOVED_EARTH));
    }

    @Test
    void everyObserverIsNotified() {
        Observer<List<Body>> anotherObserver = mock();
        when(integrator.step(List.of(SUN, EARTH), TWO_DAYS)).thenReturn(List.of(SUN, MOVED_EARTH));
        SUT.addObserver(observer);
        SUT.addObserver(anotherObserver);

        SUT.advance(TWO_DAYS);

        verify(observer).update(SUT, List.of(SUN, MOVED_EARTH));
        verify(anotherObserver).update(SUT, List.of(SUN, MOVED_EARTH));
    }

    @Test
    void aFailedAdvanceNotifiesNobody() {
        when(integrator.step(List.of(SUN, EARTH), TWO_DAYS)).thenThrow(new IllegalStateException("Integration failed"));
        SUT.addObserver(observer);

        assertThatThrownBy(() -> SUT.advance(TWO_DAYS)).isInstanceOf(IllegalStateException.class);

        verifyNoInteractions(observer);
    }

    @Test
    void theStateIsTheCurrentBodies() {
        List<Body> state = SUT.getState();

        assertThat(state).containsExactly(SUN, EARTH);
    }
}
