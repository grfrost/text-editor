package editor.terminal.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

record winsize_s(int fd, MemorySegment seg) {
    static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
        VarHandle vh = LAYOUT.varHandle(elements);
        vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
        return vh;
    }

    interface MacLibC {
        int SYSTEM_OUT_FD = 0;
        int ISIG = 1, ICANON = 2, ECHO = 10, TCSANOW = 0, TCSADRAIN = 1, TCSAFLUSH = 2,
                IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TIOCGWINSZ = 0x40087468;

    }

    public static int TIOCGWINSZ;

    interface LinuxLib {
        int SYSTEM_OUT_FD = 0;
        int ISIG = 1, ICANON = 2, ECHO = 10, TCSAFLUSH = 2,
                IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5, TIOCGWINSZ = 0x5413;
    }

    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            String arch = System.getProperty("os.arch");
            boolean isMipsPpcOrSparc = arch.equals("mips")
                    || arch.equals("mips64")
                    || arch.equals("mipsel")
                    || arch.equals("mips64el")
                    || arch.startsWith("ppc")
                    || arch.startsWith("sparc");
            TIOCGWINSZ = isMipsPpcOrSparc ? 0x40087468 : 0x00005413;
        } else if (osName.startsWith("Solaris") || osName.startsWith("SunOS")) {
            int _TIOC = ('T' << 8);
            TIOCGWINSZ = (_TIOC | 104);
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            TIOCGWINSZ = 0x40087468;
        } else if (osName.startsWith("FreeBSD")) {
            TIOCGWINSZ = 0x40087468;
        } else {
            throw new UnsupportedOperationException();
        }
        System.out.println("TIOCGWINSZ=" + Integer.toHexString(TIOCGWINSZ));
    }


    static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_SHORT.withName("ws_row"),
            ValueLayout.JAVA_SHORT.withName("ws_col"),
            ValueLayout.JAVA_SHORT.withName("ws_xpixels"),
            ValueLayout.JAVA_SHORT.withName("ws_ypixels"));
    private static final VarHandle ws_row = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_row"));
    private static final VarHandle ws_col = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_col"));
    private static final VarHandle ws_xpixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_xpixels"));
    private static final VarHandle ws_ypixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_ypixels"));
    private static final MethodHandle ioctl = MacOrUnixTerminal.linker.downcallHandle(
            MacOrUnixTerminal.lookup.find("ioctl").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS),
            Linker.Option.firstVariadicArg(2)
    );

    static int ioctl(int fd, MemorySegment seg) {
        try {
            return (int) ioctl.invoke(fd, (long) winsize_s.TIOCGWINSZ, seg);
        } catch (Throwable e) {
            return 0;
        }
    }





    short ws_col() {
        return (short) ws_col.get(seg);
    }

    short ws_row() {
        return (short) ws_row.get(seg);
    }

    short ws_xpixels() {
        return (short) ws_xpixels.get(seg);
    }

    short ws_ypixels() {
        return (short) ws_ypixels.get(seg);
    }
}
