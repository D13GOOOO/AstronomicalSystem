package astronomicalsystem.app.time;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SystemClockTest {

    private static final long A_TENTH_OF_A_SECOND_IN_MILLISECONDS = 100;
    // Lower bound looser than the pause, since the system timer may wake the thread a little early;
    // upper bound loose enough for a busy machine
    private static final double AT_LEAST = 0.09;
    private static final double AT_MOST = 1.0;

    private final SystemClock SUT = new SystemClock();

    @Test
    void measuresTheRealTimeElapsedInSeconds() throws InterruptedException {
        double before = SUT.seconds();
        Thread.sleep(A_TENTH_OF_A_SECOND_IN_MILLISECONDS);
        double after = SUT.seconds();

        assertThat(after - before).isBetween(AT_LEAST, AT_MOST);
    }
}
