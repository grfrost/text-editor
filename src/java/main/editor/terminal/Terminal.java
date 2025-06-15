package editor.terminal;

public interface Terminal {
    void enableRawMode();

    void disableRawMode();

    WindowSize getWindowSize();

    default boolean isatty(){return true;}

    int read();
    void write(String s);
}
