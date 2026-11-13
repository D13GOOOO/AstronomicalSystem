package astronomicalsystem.app.camera;

import astronomicalsystem.core.math.Vector3;
import astronomicalsystem.core.observer.Observer;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.function.Consumer;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ObservableCameraRigTest {

    private static final Vector3 TARGET = Vector3.ZERO;
    private static final Orbit ON_THE_ECLIPTIC = new Orbit(2.0, 0.0, 0.0);
    private static final Lens RIGHT_ANGLE_LENS = new Lens(Math.PI / 2.0, 1.0, 3.0);
    private static final Viewport TWO_BY_ONE = new Viewport(200, 100);
    private static final double QUARTER_TURN = Math.PI / 2.0;
    private static final double HALF_THE_DISTANCE = 0.5;
    private static final Viewport SQUARE = new Viewport(100, 100);
    private static final double SAME_DISTANCE = 1.0;
    private static final Vector3 ELSEWHERE = new Vector3(1.0, 0.0, 0.0);

    private final Observer<Projection> observer = mock();
    private final ObservableCameraRig SUT = new ObservableCameraRig(TARGET, ON_THE_ECLIPTIC, RIGHT_ANGLE_LENS,
            TWO_BY_ONE);

    static Stream<Arguments> changes() {
        return Stream.of(
                Arguments.of(Named.of("rotation", (Consumer<CameraRig>) rig -> rig.orbitBy(QUARTER_TURN, 0.0))),
                Arguments.of(Named.of("zoom", (Consumer<CameraRig>) rig -> rig.zoomBy(HALF_THE_DISTANCE))),
                Arguments.of(Named.of("new viewport", (Consumer<CameraRig>) rig -> rig.resize(SQUARE))),
                Arguments.of(Named.of("new target", (Consumer<CameraRig>) rig -> rig.lookAt(ELSEWHERE))));
    }

    @ParameterizedTest
    @MethodSource("changes")
    void everyChangeNotifiesTheObserversWithTheNewCamera(Consumer<CameraRig> change) {
        SUT.addObserver(observer);

        change.accept(SUT);

        verify(observer).update(SUT, SUT.getState());
    }

    static Stream<Arguments> operationsThatChangeNothing() {
        return Stream.of(
                Arguments.of(Named.of("no rotation", (Consumer<CameraRig>) rig -> rig.orbitBy(0.0, 0.0))),
                Arguments.of(Named.of("no zoom", (Consumer<CameraRig>) rig -> rig.zoomBy(SAME_DISTANCE))),
                Arguments.of(Named.of("same viewport", (Consumer<CameraRig>) rig -> rig.resize(TWO_BY_ONE))),
                Arguments.of(Named.of("same target", (Consumer<CameraRig>) rig -> rig.lookAt(TARGET))));
    }

    @ParameterizedTest
    @MethodSource("operationsThatChangeNothing")
    void anOperationThatChangesNothingNotifiesNobody(Consumer<CameraRig> operation) {
        SUT.addObserver(observer);

        operation.accept(SUT);

        verifyNoInteractions(observer);
    }
}
