package editor.terminal.jna;

import com.sun.jna.LastErrorException;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Structure;
import editor.terminal.ANISTerminal;
import editor.terminal.WindowSize;

import java.io.IOException;
import java.util.Arrays;

public class UnixTerminal implements ANISTerminal<UnixTerminal> {

    private static UnixTerminal.LibC.Termios originalAttributes;

    @Override
    public UnixTerminal enableRawMode() {
        UnixTerminal.LibC.Termios termios = new UnixTerminal.LibC.Termios();
        int rc = UnixTerminal.LibC.INSTANCE.tcgetattr(UnixTerminal.LibC.SYSTEM_OUT_FD, termios);

        if (rc != 0) {
            System.err.println("There was a problem calling tcgetattr");
            System.exit(rc);
        }

        originalAttributes = UnixTerminal.LibC.Termios.of(termios);
        termios.c_lflag &= ~(UnixTerminal.LibC.ECHO | UnixTerminal.LibC.ICANON | UnixTerminal.LibC.IEXTEN | UnixTerminal.LibC.ISIG);
        termios.c_iflag &= ~(UnixTerminal.LibC.IXON | UnixTerminal.LibC.ICRNL);
        termios.c_oflag &= ~(UnixTerminal.LibC.OPOST);
        UnixTerminal.LibC.INSTANCE.tcsetattr(UnixTerminal.LibC.SYSTEM_OUT_FD, UnixTerminal.LibC.TCSAFLUSH, termios);
        return self();
    }

    @Override
    public UnixTerminal disableRawMode() {
        UnixTerminal.LibC.INSTANCE.tcsetattr(UnixTerminal.LibC.SYSTEM_OUT_FD, UnixTerminal.LibC.TCSAFLUSH, originalAttributes);
        return self();
    }

    @Override
    public WindowSize getWindowSize() {
        final UnixTerminal.LibC.Winsize winsize = new UnixTerminal.LibC.Winsize();

        final int rc = UnixTerminal.LibC.INSTANCE.ioctl(UnixTerminal.LibC.SYSTEM_OUT_FD, UnixTerminal.LibC.TIOCGWINSZ, winsize);

        if (rc != 0) {
            System.err.println("ioctl failed with return code[={}]" + rc);
            System.exit(1);
        }
        return WindowSize.of(winsize.ws_row, winsize.ws_col);
    }


    interface LibC extends Library {

        int SYSTEM_OUT_FD = 0;
        int ISIG = 1, ICANON = 2, ECHO = 10, TCSAFLUSH = 2,
                IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TIOCGWINSZ = 0x5413;

        // we're loading the C standard library for POSIX systems
        UnixTerminal.LibC INSTANCE = Native.load("c", UnixTerminal.LibC.class);

        @Structure.FieldOrder(value = {"ws_row", "ws_col", "ws_xpixel", "ws_ypixel"})
        class Winsize extends Structure {
            public short ws_row, ws_col, ws_xpixel, ws_ypixel;
        }


        @Structure.FieldOrder(value = {"c_iflag", "c_oflag", "c_cflag", "c_lflag", "c_cc"})
        class Termios extends Structure {
            public int c_iflag, c_oflag, c_cflag, c_lflag;

            public byte[] c_cc = new byte[32]; // 32

            public Termios() {
            }

            public static UnixTerminal.LibC.Termios of(UnixTerminal.LibC.Termios t) {
                UnixTerminal.LibC.Termios copy = new UnixTerminal.LibC.Termios();
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


        int tcgetattr(int fd, UnixTerminal.LibC.Termios termios);

        int tcsetattr(int fd, int optional_actions,
                      UnixTerminal.LibC.Termios termios);

        int ioctl(int fd, int opt, UnixTerminal.LibC.Winsize winsize) throws LastErrorException;

    }
    public int read() {
        try {
            return System.in.read();
        } catch (IOException e) {
        }
        return -1;
    }

    public UnixTerminal write(String s) {
        System.out.print(s);
        return self();
    }
}
