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
    record bit(String name, long v){
        static bit of(String name, long v){
            return new bit(name, v);
        }
        boolean isSet(long b){
            return ((b&v)==b);
        }
        String flag(long b){
            return (isSet(b)?"+":"-")+name();
        }
    }
    public static final bit  ISIG = bit.of("ISIG", 1);
    public static final bit  ICANON = bit.of("ICANON",2);
    public static final bit  ECHO=bit.of("ECHO", 10);
    public static final bit  TCSANOW = bit.of("TCSANOW", 0);
    public static final bit TCSADRAIN = bit.of("TCSADRAIN", 1);
    public static final bit TCSAFLUSH = bit.of("TCSAFLUSH", 2);
    public static final bit    IXON = bit.of("IXON", 2000);
    public static final bit ICRNL = bit.of("ICRNL", 400);
    public static final bit       IEXTEN = bit.of("IEXTEN", 100000);
    public static final bit        OPOST = bit.of("OPOST", 1);
    public static final bit       VMIN = bit.of("VMIN", 6);
    public static final bit VTIME = bit.of("VTIME", 5);

    String hex(long v){
        String s = Long.toHexString(v);
        return "0".repeat(17-s.length())+s;
    }
    String bin(long v){
        String s = Long.toBinaryString(v);
        return "0".repeat(65-s.length())+s;
    }
    String info(String name, long v, long bit){
        return name+"=" +hex(v&bit)+" "+bin(v&bit);
    }
    String info(String name, long v){
        return info(name, v, 0xffffffffL);
    }

    void show(String str) {
        System.out.println(str + ": c_cflag=" + Long.toHexString(c_cflag()) + " c_iflag=" + Long.toHexString(c_iflag()) + " c_oflag=" + c_oflag() + " c_lflag=" + Long.toHexString(c_lflag()));
        {
            long cflag = c_cflag();
            //
         /*
                 // termios.c_lflag &= ~(UnixTerminal.LibC.ECHO | UnixTerminal.LibC.ICANON | UnixTerminal.LibC.IEXTEN | UnixTerminal.LibC.ISIG);
            //termios.c_iflag &= ~(UnixTerminal.LibC.IXON | UnixTerminal.LibC.ICRNL);
            //termios.c_oflag &= ~(UnixTerminal.LibC.OPOST);
          */

            System.out.println(str + info(":  c_cflag=", cflag));
        }
        {
            long iflag = c_iflag();
            System.out.print(str + info(":  c_iflag=", iflag));
            System.out.println(" "+IXON.flag(iflag)+" "+ICRNL.flag(iflag));
            System.out.println(str + info(":     IXON=", iflag, IXON.v));
            System.out.println(str + info(":     ICRNL=", iflag, ICRNL.v));

        }
        {
            long oflag = c_cflag();
            System.out.print(str + info(":  c_oflag=", oflag));
            System.out.println(" "+OPOST.flag(oflag));
            System.out.println(str + info(":     OPOST=", oflag, OPOST.v));
        }
        {
            long lflag = c_cflag();
            System.out.print(str + info(":  c_lflag=", lflag));
            System.out.println(" "+ECHO.flag(lflag)+" "+ICANON.flag(lflag)+" "+ISIG.flag(lflag)+" "+IEXTEN.flag(lflag));
            System.out.println();
            System.out.println(str + info(":     ECHO=", lflag, ECHO.v));
            System.out.println(str + info(":   ICANON=", lflag, ICANON.v));
            System.out.println(str + info(":     ISIG=", lflag, ISIG.v));
            System.out.println(str + info(":   IEXTEN=", lflag, IEXTEN.v));

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
