package astronomicalsystem.app.view;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.util.List;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

class LayersTest {

    private final Drawable scene = mock();
    private final Drawable text = mock();
    private final Layers SUT = new Layers(List.of(scene, text));

    @Test
    void theLayersAreDrawnFromTheBottomOne() {
        SUT.draw();

        InOrder bottomToTop = inOrder(scene, text);
        bottomToTop.verify(scene).draw();
        bottomToTop.verify(text).draw();
    }
}
