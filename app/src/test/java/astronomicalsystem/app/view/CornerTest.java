package astronomicalsystem.app.view;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CornerTest {

    private static final float IMAGE_WIDTH = 320.0f;
    private static final float IMAGE_HEIGHT = 240.0f;

    // Ten pixels away from the edges of the image
    @ParameterizedTest
    @CsvSource(textBlock = """
            TOP_LEFT,      100.0,  44.0,  10.0,   10.0
            TOP_LEFT,      50.0,   20.0,  10.0,   10.0
            BOTTOM_LEFT,   100.0,  44.0,  10.0,   186.0
            BOTTOM_LEFT,   50.0,   20.0,  10.0,   210.0
            BOTTOM_RIGHT,  100.0,  44.0,  210.0,  186.0
            BOTTOM_RIGHT,  50.0,   20.0,  260.0,  210.0
            """)
    void aBlockOfTextStartsAMarginAwayFromItsCorner(Corner SUT, float textWidth, float textHeight, float left,
                                                    float top) {
        assertThat(SUT.left(textWidth, IMAGE_WIDTH)).isEqualTo(left);
        assertThat(SUT.top(textHeight, IMAGE_HEIGHT)).isEqualTo(top);
    }
}
