package editor.terminal;

public interface Terminal {
    void enableRawMode();

    void disableRawMode();

    WindowSize getWindowSize();
}
