package editor.terminal.ffm;

import editor.Key;
import editor.terminal.jna.MacOsTerminal;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
//https://invisible-island.net/xterm/ctlseqs/ctlseqs.html#The%20Alternate%20Screen%20Buffer
//https://github.com/alexarchambault/native-terminal/blob/main/native/jdk22/src/io/github/alexarchambault/nativeterm/internal/CLibrary.java
public class MacOrUnixTerminal {

    static Linker linker = Linker.nativeLinker();
    static SymbolLookup loader = SymbolLookup.loaderLookup();
    static SymbolLookup lookup = name -> loader.find(name).or(() -> linker.defaultLookup().find(name));


    // https://www.man7.org/linux/man-pages/man3/isatty.3.html
    static MethodHandle isatty = linker.downcallHandle(
            lookup.find("isatty").get(), FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));

    static boolean isatty() {
        try {
            return ((int) isatty.invoke(1) == 1);
        } catch (Throwable e) {
            return false;
        }
    }


    // https://man7.org/linux/man-pages/man3/ttyname.3.html
    static MethodHandle ttyname_r = linker.downcallHandle(
            lookup.find("ttyname_r").get(),
            FunctionDescriptor.of(
                    ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_LONG));




    record termios(int fd, MemorySegment seg){
        static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
            VarHandle vh = LAYOUT.varHandle(elements);
            vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
            return vh;
        }
        void show(String str){
            System.out.println(str+ ": c_cflag="+c_cflag()+" c_iflag="+c_iflag()+" c_oflag="+c_oflag()+" c_lflag="+c_lflag());

        }
        public static final int ISIG = 1, ICANON = 2, ECHO = 10,TCSANOW =0, TCSADRAIN=1, TCSAFLUSH = 2,
                IXON = 2000, ICRNL = 400, IEXTEN = 100000, OPOST = 1, VMIN = 6, VTIME = 5;



          //  public byte[] c_cc = new byte[19];


            static final GroupLayout LAYOUT = MemoryLayout.structLayout(
                ValueLayout.JAVA_LONG.withName("c_iflag"),
                ValueLayout.JAVA_LONG.withName("c_oflag"),
                ValueLayout.JAVA_LONG.withName("c_cflag"),
                ValueLayout.JAVA_LONG.withName("c_lflag"),
                MemoryLayout.sequenceLayout(20, ValueLayout.JAVA_BYTE).withName("c_cc"));
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
            c_iflag.set(seg,v);
        }
        void c_oflag(long v) {
            c_oflag.set(seg,v);
        }
        void c_cflag(long v) {
            c_cflag.set(seg,v);
        }
        void c_lflag(long v) {
            c_lflag.set(seg,v);
        }


        static termios of(int fd){
            MemorySegment seg=Arena.ofAuto().allocate(LAYOUT);
            return new termios(fd, seg);
        }
        // https://man7.org/linux/man-pages/man3/tcsetattr.3p.html
        static MethodHandle tcsetattr = linker.downcallHandle(
                lookup.find("tcsetattr").get(),
                FunctionDescriptor.of(
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        // https://man7.org/linux/man-pages/man3/tcgetattr.3p.html
        static MethodHandle tcgetattr = linker.downcallHandle(
                lookup.find("tcgetattr").get(),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        int get(){
            try {
                return  (int) tcgetattr.invoke(fd,  seg);
            } catch (Throwable e) {
                return 0;
            }
        }

        private int set(long tcsa ){
            try {
                return  (int) tcsetattr.invoke(fd, tcsa,  seg);
            } catch (Throwable e) {
                return 0;
            }
        }
        private int flush(){
            try {
                return  (int) tcsetattr.invoke(fd, TCSAFLUSH,  seg);
            } catch (Throwable e) {
                return 0;
            }
        }

    }


   record winsize(int fd, MemorySegment seg){
       static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
           VarHandle vh = LAYOUT.varHandle(elements);
           vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
           return vh;
       }

        private static final int TIOCGWINSZ;
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
        }

        private static final GroupLayout LAYOUT = MemoryLayout.structLayout(
                    ValueLayout.JAVA_SHORT.withName("ws_row"),
                    ValueLayout.JAVA_SHORT.withName("ws_col"),
                    ValueLayout.JAVA_SHORT.withName("ws_xpixels"),
                    ValueLayout.JAVA_SHORT.withName("ws_ypixels"));
        private static final VarHandle ws_row = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_row"));
        private static final VarHandle ws_col = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_col"));
        private static final VarHandle ws_xpixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_xpixels"));
        private static final VarHandle ws_ypixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_ypixels"));
      private  static final MethodHandle ioctl = linker.downcallHandle(
               lookup.find("ioctl").get(),
               FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS),
               Linker.Option.firstVariadicArg(2)
       );

        private static int ioctl(int fd, MemorySegment seg) {
            try {
                return  (int) ioctl.invoke(fd, (long) winsize.TIOCGWINSZ, seg);
            } catch (Throwable e) {
                return 0;
            }
        }

        static winsize of(int fd) {
            MemorySegment seg = Arena.ofAuto().allocate(LAYOUT);
            return ioctl(fd, seg) == 0?new winsize(fd,seg):null;
        }

        void update(){
            ioctl(fd,seg);
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

    public static void main(String[] args) throws Throwable {
        if (isatty()) {
            if (winsize.of(0) instanceof winsize ws){
                System.out.println("row " + ws.ws_row() + " col " + ws.ws_col() + " x_pixels " + ws.ws_xpixels() + " y_pixels " + ws.ws_ypixels());
                var prev = termios.of(0);
                prev.show("prev before get");
                prev.get();
                prev.show("prev after get ");

                var quiet = termios.of(0);
                quiet.show("quiet before get ");
                quiet.get();
                quiet.show("quiet after get ");
                quiet.c_lflag(quiet.c_lflag()& ~(termios.ECHO | termios.ICANON | termios.IEXTEN | termios.ISIG));

                quiet.c_iflag(quiet.c_iflag() & ~(termios.IXON |termios.ICRNL));
                quiet.c_oflag(quiet.c_oflag() & ~(termios.OPOST));
                quiet.flush();
                quiet.show("quiet after set ");
                long start = System.currentTimeMillis();
                while ((System.currentTimeMillis() - start) < 10000) {
                   Key key  =  Key.read();
                   System.out.print("."+key.name());
                }

                prev.flush();
            } else {
                System.out.println("ioctl failed ");
            }
        } else {
            System.out.println("not a tty ");
        }
    }
}