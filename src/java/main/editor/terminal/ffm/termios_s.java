package editor.terminal.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

record termios_s(int fd, MemorySegment seg) {
    static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
        VarHandle vh = LAYOUT.varHandle(elements);
        vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
        return vh;
    }
    public static final long ISIG = 1, ICANON = 2, ECHO = 10, TCSANOW = 0, TCSADRAIN = 1, TCSAFLUSH = 2,
            IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5;
    void show(String str) {
        System.out.println(str + ": c_cflag=" + Long.toHexString(c_cflag()) + " c_iflag=" + Long.toHexString(c_iflag()) + " c_oflag=" + c_oflag() + " c_lflag=" + Long.toHexString(c_lflag()));
        long clflag = c_cflag();
        //
         /*
                 // termios.c_lflag &= ~(UnixTerminal.LibC.ECHO | UnixTerminal.LibC.ICANON | UnixTerminal.LibC.IEXTEN | UnixTerminal.LibC.ISIG);
            //termios.c_iflag &= ~(UnixTerminal.LibC.IXON | UnixTerminal.LibC.ICRNL);
            //termios.c_oflag &= ~(UnixTerminal.LibC.OPOST);
          */
        System.out.print("c_cflag: ");
        if ((clflag & ECHO) == ECHO) {
            System.out.print("ECHO");
        }

        if ((clflag & ICANON) == ICANON) {
            System.out.print("ICANON");
        }
        if ((clflag & ISIG) == ISIG) {
            System.out.print("ISIG");
        }

        if ((clflag & IEXTEN) == IEXTEN) {
            System.out.print("IEXTEN");
        }

        System.out.println();
    }

    private static final int NCCS;

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            NCCS = 32;
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            NCCS = 20;
        } else {
            throw new UnsupportedOperationException();
        }
        System.out.println("NCSS=" + NCCS);
    }


    static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_LONG.withName("c_iflag"),
            ValueLayout.JAVA_LONG.withName("c_oflag"),
            ValueLayout.JAVA_LONG.withName("c_cflag"),
            ValueLayout.JAVA_LONG.withName("c_lflag"),
            MemoryLayout.sequenceLayout(NCCS, ValueLayout.JAVA_BYTE).withName("c_cc"));
    private static final VarHandle c_iflag = lookupVarHandle(MemoryLayout.PathElement.groupElement("c_iflag"));
    private static final VarHandle c_oflag = lookupVarHandle(MemoryLayout.PathElement.groupElement("c_oflag"));
    private static final VarHandle c_cflag = lookupVarHandle(MemoryLayout.PathElement.groupElement("c_cflag"));
    private static final VarHandle c_lflag = lookupVarHandle(MemoryLayout.PathElement.groupElement("c_lflag"));
    //   private static final VarHandle c_cc = lookupVarHandle(MemoryLayout.PathElement.groupElement("c_cc"));

    long c_iflag() {
        return (long) c_iflag.get(seg);
    }

    long c_oflag() {
        return (long) c_oflag.get(seg);
    }

    long c_cflag() {
        return (long) c_cflag.get(seg);
    }

    long c_lflag() {
        return (long) c_lflag.get(seg);
    }

    void c_iflag(long v) {
        c_iflag.set(seg, v);
    }

    void c_oflag(long v) {
        c_oflag.set(seg, v);
    }

    void c_cflag(long v) {
        c_cflag.set(seg, v);
    }

    void c_lflag(long v) {
        c_lflag.set(seg, v);
    }






    // https://man7.org/linux/man-pages/man3/tcsetattr.3p.html
    static MethodHandle tcsetattr = MacOrUnixTerminal.linker.downcallHandle(
            MacOrUnixTerminal.lookup.find("tcsetattr").get(),
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
    // https://man7.org/linux/man-pages/man3/tcgetattr.3p.html
    static MethodHandle tcgetattr = MacOrUnixTerminal.linker.downcallHandle(
            MacOrUnixTerminal.lookup.find("tcgetattr").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));

    int get() {
        try {
            return (int) tcgetattr.invoke(fd, seg);
        } catch (Throwable e) {
            return 0;
        }
    }

    private int set(long tcsa) {
        try {
            return (int) tcsetattr.invoke(fd, tcsa, seg);
        } catch (Throwable e) {
            return 0;
        }
    }

    int enable() {
        try {
            return (int) tcsetattr.invoke(fd, TCSAFLUSH, seg);
        } catch (Throwable e) {
            return 0;
        }
    }

}
