package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Integrator;
import org.junit.jupiter.api.Test;

import java.util.List;

import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INITIAL_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.TWO_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ObservableSimulationTest {

    private final Integrator integrator = mock();
    private final Observer<List<Body>> observer = mock();
    private final ObservableSimulation SUT = new ObservableSimulation(integrator, INITIAL_BODIES);

    @Test
    void advancingNotifiesTheObserversWithTheNewBodies() {
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        SUT.addObserver(observer);

        SUT.advance(TWO_DAYS);

        verify(observer).update(SUT, INTEGRATED_BODIES);
    }

    @Test
    void everyObserverIsNotified() {
        Observer<List<Body>> anotherObserver = mock();
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        SUT.addObserver(observer);
        SUT.addObserver(anotherObserver);

        SUT.advance(TWO_DAYS);

        verify(observer).update(SUT, INTEGRATED_BODIES);
        verify(anotherObserver).update(SUT, INTEGRATED_BODIES);
    }

    @Test
    void aFailedAdvanceNotifiesNobody() {
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenThrow(new IllegalStateException("Integration failed"));
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
