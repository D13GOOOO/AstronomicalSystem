package astronomicalsystem.app.impact;

import astronomicalsystem.app.time.TimeFlow;
import astronomicalsystem.app.time.TimeWarp;
import astronomicalsystem.core.body.Body;
import astronomicalsystem.core.body.BodyKind;
import astronomicalsystem.core.body.BodyName;
import astronomicalsystem.core.body.Mass;
import astronomicalsystem.core.body.Radius;
import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observable;
import astronomicalsystem.core.observer.Observer;
import astronomicalsystem.core.physics.Collision;
import astronomicalsystem.core.time.JulianDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;

class BlastsTest {

    private static final Body EARTH = new Body(new BodyName("Earth"), BodyKind.PLANET, new Mass(3.003e-6),
            new Radius(4.259e-5), Vector3.ZERO, Vector3.ZERO);
    private static final Body EARTH_LATER = EARTH.withMotion(new Vector3(1e-3, 0.0, 0.0), Vector3.ZERO);
    private static final Vector3 UP = new Vector3(0.0, 0.0, 1.0);
    private static final Collision EARTH_HIT = new Collision(EARTH, UP, UP, 0.01, 1e-12);
    private static final JulianDate IMPACT = new JulianDate(2461041.5);
    private static final JulianDate FIVE_MINUTES_LATER = new JulianDate(2461041.5 + 5.0 / 1440.0);
    private static final TimeWarp SLOWEST = new TimeWarp(1.0 / 2048.0);
    // The fireball has gone out and the embers have fallen back
    private static final JulianDate A_DAY_LATER = new JulianDate(2461042.5);
    private static final JulianDate A_DAY_AND_A_MINUTE_LATER = new JulianDate(2461042.5 + 1.0 / 1440.0);

    private final Observable<List<Collision>> collisionNews = mock();
    private final Observable<TimeFlow> timeline = mock();
    private final Observable<List<Body>> simulation = mock();
    private final Observer<List<Flare>> observer = mock();
    private final Blasts SUT = new Blasts();

    @BeforeEach
    void theEarthIsHitNow() {
        SUT.timeObserver().update(timeline, new TimeFlow(IMPACT, SLOWEST, false));
        SUT.bodiesObserver().update(simulation, List.of(EARTH));
        SUT.update(collisionNews, List.of(EARTH_HIT));
        SUT.addObserver(observer);
    }

    // The blast moves with the Earth
    @Test
    void theFlaresOfABlastUnfoldWithTimeOnTheBodyThatSurvived() {
        SUT.bodiesObserver().update(simulation, List.of(EARTH_LATER));

        SUT.timeObserver().update(timeline, new TimeFlow(FIVE_MINUTES_LATER, SLOWEST, false));

        verify(observer).update(SUT, new Blast(EARTH_HIT, IMPACT).flaresAt(FIVE_MINUTES_LATER, EARTH_LATER.position()));
    }

    // The blast is shown, then once more to clear it, then never again
    @Test
    void whenNothingGlowsAnyMoreTheObserversAreToldOnce() {
        SUT.timeObserver().update(timeline, new TimeFlow(FIVE_MINUTES_LATER, SLOWEST, false));
        SUT.timeObserver().update(timeline, new TimeFlow(A_DAY_LATER, SLOWEST, false));

        SUT.timeObserver().update(timeline, new TimeFlow(A_DAY_AND_A_MINUTE_LATER, SLOWEST, false));

        verify(observer).update(SUT, List.of());
        verify(observer, times(2)).update(eq(SUT), anyList());
    }
}
