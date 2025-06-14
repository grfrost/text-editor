package editor.terminal.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import editor.terminal.Terminal;
import editor.terminal.WindowSize;

import java.util.Arrays;

public class MacOsTerminal implements Terminal {

    private static MacOsTerminal.LibC.Termios originalAttributes;

    @Override
    public void enableRawMode() {
        MacOsTerminal.LibC.Termios termios = new MacOsTerminal.LibC.Termios();
        System.out.println("tcgetattr{");
        int rc = MacOsTerminal.LibC.INSTANCE.tcgetattr(MacOsTerminal.LibC.SYSTEM_OUT_FD, termios);
        System.out.println("}tcgetattr");
        if (rc != 0) {
            System.err.println("There was a problem calling tcgetattr");
            System.exit(rc);
        }

        originalAttributes = MacOsTerminal.LibC.Termios.of(termios);

        termios.c_lflag &= ~(MacOsTerminal.LibC.ECHO | MacOsTerminal.LibC.ICANON | MacOsTerminal.LibC.IEXTEN | MacOsTerminal.LibC.ISIG);
        termios.c_iflag &= ~(MacOsTerminal.LibC.IXON | MacOsTerminal.LibC.ICRNL);
        termios.c_oflag &= ~(MacOsTerminal.LibC.OPOST);

       /* termios.c_cc[LibC.VMIN] = 0;
        termios.c_cc[LibC.VTIME] = 1;*/

        MacOsTerminal.LibC.INSTANCE.tcsetattr(MacOsTerminal.LibC.SYSTEM_OUT_FD, MacOsTerminal.LibC.TCSAFLUSH, termios);
    }

    @Override
    public void disableRawMode() {
        System.out.println("tcsetattr{");
        MacOsTerminal.LibC.INSTANCE.tcsetattr(MacOsTerminal.LibC.SYSTEM_OUT_FD, MacOsTerminal.LibC.TCSAFLUSH, originalAttributes);
        System.out.println("}tcsetattr");
    }

    @Override
    public WindowSize getWindowSize() {
        final MacOsTerminal.LibC.Winsize winsize = new MacOsTerminal.LibC.Winsize();

        System.out.println("ioctl{"+winsize.ws_col);
        final int rc = MacOsTerminal.LibC.INSTANCE.ioctl(MacOsTerminal.LibC.SYSTEM_OUT_FD,MacOsTerminal.LibC.TIOCGWINSZ, winsize);
        System.out.println("}ioctl");
        if (rc != 0) {
            System.err.println("ioctl failed with return code[={}]" + rc);
            System.exit(1);
        }
        return new WindowSize(winsize.ws_row, winsize.ws_col);
    }


    interface LibC extends Library {

        int SYSTEM_OUT_FD = 0;
        int ISIG = 1, ICANON = 2, ECHO = 10, TCSAFLUSH = 2,
                IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TIOCGWINSZ = 0x40087468;

        // we're loading the C standard library for POSIX systems
        MacOsTerminal.LibC INSTANCE = Native.load("c", MacOsTerminal.LibC.class);

        @Structure.FieldOrder(value = {"ws_row", "ws_col", "ws_xpixel", "ws_ypixel"})
        class Winsize extends Structure {
            public short ws_row, ws_col, ws_xpixel, ws_ypixel;
        }


        @Structure.FieldOrder(value = {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"})
        class Termios extends Structure {
            public long c_iflag, c_oflag, c_cflag, c_lflag;

            public byte[] c_cc = new byte[19];

            public Termios() {
            }

            public static MacOsTerminal.LibC.Termios of(MacOsTerminal.LibC.Termios t) {
                MacOsTerminal.LibC.Termios copy = new MacOsTerminal.LibC.Termios();
                copy.c_iflag = t.c_iflag;
                copy.c_oflag = t.c_oflag;
                copy.c_cflag = t.c_cflag;
                copy.c_lflag = t.c_lflag;
                copy.c_cc = t.c_cc.clone();
                return copy;
            }

            @Override
            public String toString() {
                return "Termios{" +
                        "c_iflag=" + c_iflag +
                        ", c_oflag=" + c_oflag +
                        ", c_cflag=" + c_cflag +
                        ", c_lflag=" + c_lflag +
                        ", c_cc=" + Arrays.toString(c_cc) +
                        '}';
            }
        }


        int tcgetattr(int fd, MacOsTerminal.LibC.Termios termios);

        int tcsetattr(int fd, int optional_actions,
                      MacOsTerminal.LibC.Termios termios);

        int ioctl(int fd, int opt, MacOsTerminal.LibC.Winsize winsize) throws LastErrorException;

    }

}
