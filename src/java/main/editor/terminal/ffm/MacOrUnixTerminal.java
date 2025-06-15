package editor.terminal.ffm;

import editor.terminal.Terminal;
import editor.terminal.WindowSize;
import editor.terminal.jna.UnixTerminal;

import java.io.IOException;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

//https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#The%20Alternate%20Screen%20Buffer
//https://github.com/alexarchambault/native-terminal/blob/main/native/jdk22/src/io/github/alexarchambault/nativeterm/internal/CLibrary.java
public class MacOrUnixTerminal implements Terminal<MacOrUnixTerminal> {

    static Linker linker = Linker.nativeLinker();
    static SymbolLookup loader = SymbolLookup.loaderLookup();
    static SymbolLookup lookup = name -> loader.find(name).or(() -> linker.defaultLookup().find(name));


    // https://www.man7.org/linux/man-pages/man3/isatty.3.html
    static MethodHandle isatty = linker.downcallHandle(
            lookup.find("isatty").get(), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

    public boolean isatty() {
        try {
            return ((int) isatty.invoke(fd) == 1);
        } catch (Throwable e) {
            return false;
        }
    }


    // https://man7.org/linux/man-pages/man3/ttyname.3.html
    static MethodHandle ttyname_r = linker.downcallHandle(
            lookup.find("ttyname_r").get(),
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));


    @Override
    public MacOrUnixTerminal enableRawMode() {
        raw.enable();
        return self();
    }

    @Override
    public MacOrUnixTerminal disableRawMode() {
        cooked.enable();
        return self();
    }

    @Override
    public WindowSize getWindowSize() {
        return new WindowSize(size.ws_row(), size.ws_col());
    }

    final Arena arena;

    final int fd;

    final private termios_s cooked;
    final private termios_s raw;
    final private winsize_s size;

    public MacOrUnixTerminal(Arena arena, int fd) {
        this.arena = arena;
        this.fd = fd;
        if (isatty()) {
            this.cooked = new termios_s(fd, arena.allocate(termios_s.LAYOUT));

            this.cooked.get();
            this.cooked.show("cooked  -> ");
            this.raw =  new termios_s(fd, arena.allocate(termios_s.LAYOUT));
            this.raw.get();
            this.raw.show("raw     -> ");
            this.raw.c_lflag(raw.c_lflag() & ~(termios_s.ECHO.v() | termios_s.ICANON.v() | termios_s.IEXTEN.v() | termios_s.ISIG.v()));
            this.raw.c_iflag(raw.c_iflag() & ~(termios_s.IXON.v() | termios_s.ICRNL.v()));
            this.raw.c_oflag(raw.c_oflag() & ~(termios_s.OPOST.v()));
            this.raw.show("raw     -> ");
            UnixTerminal unixTerminal = new UnixTerminal();
            unixTerminal.enableRawMode();
            this.raw.get();
         //   this.cooked.enable();
            this.raw.show("raw     -> ");

          //  System.exit(1);

           // termios.c_lflag &= ~(UnixTerminal.LibC.ECHO | UnixTerminal.LibC.ICANON | UnixTerminal.LibC.IEXTEN | UnixTerminal.LibC.ISIG);
            //termios.c_iflag &= ~(UnixTerminal.LibC.IXON | UnixTerminal.LibC.ICRNL);
            //termios.c_oflag &= ~(UnixTerminal.LibC.OPOST);
           //    System.exit(1);


            MemorySegment seg = arena.allocate(winsize_s.LAYOUT);
            this.size = (winsize_s.ioctl(fd, seg) == 0 )? new winsize_s(fd,seg ) : null;

            // System.out.println("row " + size.ws_row() + " col " + size.ws_col() + " x_pixels " + size.ws_xpixels() + " y_pixels " + size.ws_ypixels());
        } else {
            throw new IllegalStateException("not a tty");
        }
    }

    public int read() {
        try {
            return System.in.read();
        } catch (IOException e) {
        }
        return -1;
    }

    public MacOrUnixTerminal write(String s) {
        System.out.print(s);
        return self();
    }
}