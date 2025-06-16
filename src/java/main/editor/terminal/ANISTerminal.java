package editor.terminal;


public interface ANISTerminal<T extends ANISTerminal<T>> extends ANSI<T> {


    T enableRawMode();

    T disableRawMode();

    WindowSize getWindowSize();

    default boolean isatty(){return true;}

    int read();

    @Override
    T write(String s);



}
