package editor.terminal.ffm;

import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import static editor.terminal.ffm.termios_s.IFLAG.ICRNL;
import static editor.terminal.ffm.termios_s.IFLAG.IXON;
import static editor.terminal.ffm.termios_s.LFLAG.*;
import static editor.terminal.ffm.termios_s.OFLAG.OPOST;

record termios_s(int fd, MemorySegment seg) {
    static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
        VarHandle vh = LAYOUT.varHandle(elements);
        vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
        return vh;
    }
    public record bit(String name, long v){
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

    public record LFLAG(){
        public static final bit ECHO       = bit.of("ECHO",0x00000008);
        public static final bit ECHOE      = bit.of("ECHOE",0x00000010);
        public static final bit ECHOK      = bit.of("ECHOK",0x00000020);
        public static final bit ECHONL     = bit.of("ECHONL",0x00000040);
        public static final bit ICANON     = bit.of("ICANON",0x00000002);
        public static final bit IEXTEN     = bit.of("IEXTEN",0x00008000);
        public static final bit ISIG       = bit.of("ISIG",0x00000001);
        public static final bit NOFLSH     = bit.of("NOFLSH",0x00000080);
        public static final bit TOSTOP     = bit.of("TOSTOP",0x00000100);
    }

    public record IFLAG(){
        public static final bit BRKINT     = bit.of("BRKINT",0x00000002);
        public static final bit ICRNL      = bit.of("ICRNL",0x00000100);
        public static final bit INLCR      = bit.of("INLCR",0x00000040);
        public static final bit IGNBRK     = bit.of("IGNBRK",0x00000001);
        public static final bit IXON       = bit.of("IXON",0x00000400);
        public static final bit IXOFF      = bit.of("IXOFF",0x00001000);

        public static final bit ISTRIP     = bit.of("ISTRIP",0x00000020);
    }

    public record OFLAG(){
        public static final bit OCRNL      = bit.of("OCRNL",0x00000008);
        public static final bit ONLCR      = bit.of("ONLCR",0x00000004);
        public static final bit OPOST      = bit.of("OPOST",0x00000001);
        public static final bit OLCUC      = bit.of("OLCUC",0x00000002);
    }

    public static final bit  TCSANOW = bit.of("TCSANOW", 0);
    public static final bit TCSADRAIN = bit.of("TCSADRAIN", 1);
    public static final bit TCSAFLUSH = bit.of("TCSAFLUSH", 2);

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
    private static final int TERMIO_LEN;
    static {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            NCCS = 32;
            TERMIO_LEN = 60;
        } else if (osName.startsWith("Mac") || osName.startsWith("Darwin")) {
            NCCS = 20;
            TERMIO_LEN = 60;
        } else {
            throw new UnsupportedOperationException();
        }
        System.out.println("NCSS=" + NCCS);
        System.out.println("TERMIO_LEN=" + TERMIO_LEN);
    }


    static final GroupLayout LAYOUT = MemoryLayout.structLayout(
            ValueLayout.JAVA_INT.withName("c_iflag"),
            ValueLayout.JAVA_INT.withName("c_oflag"),
            ValueLayout.JAVA_INT.withName("c_cflag"),
            ValueLayout.JAVA_INT.withName("c_lflag"),
            MemoryLayout.sequenceLayout(TERMIO_LEN-(ValueLayout.JAVA_INT.byteSize()*4), ValueLayout.JAVA_BYTE).withName("c_cc"));
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
        c_iflag.set(seg, (int)v);
    }

    void c_oflag(long v) {
        c_oflag.set(seg, (int)v);
    }

    void c_cflag(long v) {
        c_cflag.set(seg, (int)v);
    }

    void c_lflag(long v) {
        c_lflag.set(seg, (int)v);
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
