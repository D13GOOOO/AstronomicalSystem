package astronomicalsystem.app.view;

/**
 * Receives what the user types in a window, as raw events.
 */
public interface KeyboardHandler {

    /**
     * @param codePoint of the Unicode character typed, as the keyboard layout of the user writes it
     */
    void characterTyped(int codePoint);
}
