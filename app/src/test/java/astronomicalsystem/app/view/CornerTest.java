package astronomicalsystem.app.view;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class CornerTest {

    private static final float IMAGE_WIDTH = 320.0f;
    private static final float IMAGE_HEIGHT = 240.0f;
    private static final float TEXT_WIDTH = 100.0f;

    // Ten pixels away from the edges of the image
    @ParameterizedTest
    @CsvSource(textBlock = """
            TOP_LEFT,     44.0,  10.0
            TOP_LEFT,     20.0,  10.0
            BOTTOM_LEFT,  44.0,  186.0
            BOTTOM_LEFT,  20.0,  210.0
            """)
    void aBlockOfTextStartsAMarginAwayFromItsCorner(Corner SUT, float textHeight, float top) {
        assertThat(SUT.top(textHeight, IMAGE_HEIGHT)).isEqualTo(top);
        assertThat(SUT.left(TEXT_WIDTH, IMAGE_WIDTH)).isEqualTo(10.0f);
    }
}
