package astronomicalsystem.core.simulation;

import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.physics.CollisionResolver;
import astronomicalsystem.core.physics.Integrator;
import astronomicalsystem.core.physics.Resolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static astronomicalsystem.core.simulation.SolarSystemFixtures.ASTEROID;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.EARTH_INTO_THE_SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INITIAL_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_BODIES;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.INTEGRATED_TWICE;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.SUN;
import static astronomicalsystem.core.simulation.SolarSystemFixtures.TWO_DAYS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class ObservableSimulationTest {

    private static final int TWO_STEPS = 2;
    private static final int ONE_STEP = 1;
    private static final List<Collision> NO_COLLISIONS = List.of();

    private final Integrator integrator = mock();
    private final CollisionResolver collisions = mock();
    private final Observer<List<Body>> observer = mock();
    private ObservableSimulation SUT;

    @BeforeEach
    void createSimulationWithoutCollisions() {
        when(collisions.resolve(anyList(), anyList()))
                .thenAnswer(call -> new Resolution(call.getArgument(1), NO_COLLISIONS));
        SUT = new ObservableSimulation(integrator, collisions, INITIAL_BODIES);
    }

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
    void severalStepsAtOnceNotifyTheObserversOnceWithTheLastBodies() {
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(integrator.step(INTEGRATED_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_TWICE);
        SUT.addObserver(observer);

        SUT.advance(TWO_DAYS, TWO_STEPS);

        verify(observer).update(SUT, INTEGRATED_TWICE);
        verifyNoMoreInteractions(observer);
    }

    @Test
    void aBodyJoiningNotifiesTheObservers() {
        SUT.addObserver(observer);

        SUT.add(ASTEROID);

        verify(observer).update(SUT, List.of(SUN, EARTH, ASTEROID));
    }

    @Test
    void aBodyRefusedNotifiesNobody() {
        SUT.addObserver(observer);

        assertThatThrownBy(() -> SUT.add(EARTH)).isInstanceOf(IllegalArgumentException.class);

        verifyNoInteractions(observer);
    }

    @Test
    void theCollisionsOfAnAdvanceAreToldToTheirObservers() {
        Observer<List<Collision>> collisionObserver = mock();
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        when(collisions.resolve(INITIAL_BODIES, INTEGRATED_BODIES))
                .thenReturn(new Resolution(INTEGRATED_BODIES, List.of(EARTH_INTO_THE_SUN)));
        SUT.collisionNews().addObserver(collisionObserver);

        SUT.advance(TWO_DAYS, ONE_STEP);

        verify(collisionObserver).update(SUT.collisionNews(), List.of(EARTH_INTO_THE_SUN));
    }

    @Test
    void anAdvanceWithoutCollisionsTellsTheirObserversNothing() {
        Observer<List<Collision>> collisionObserver = mock();
        when(integrator.step(INITIAL_BODIES, TWO_DAYS)).thenReturn(INTEGRATED_BODIES);
        SUT.collisionNews().addObserver(collisionObserver);

        SUT.advance(TWO_DAYS, ONE_STEP);

        verifyNoInteractions(collisionObserver);
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
