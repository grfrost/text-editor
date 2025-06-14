package editor.terminal.ffm;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.GroupLayout;
import java.lang.foreign.Linker;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
//https://github.com/alexarchambault/native-terminal/blob/main/native/jdk22/src/io/github/alexarchambault/nativeterm/internal/CLibrary.java
public class MacOSTerminal {

    static Linker linker = Linker.nativeLinker();
    static SymbolLookup loader = SymbolLookup.loaderLookup();
    static SymbolLookup lookup = name -> loader.find(name).or(() -> linker.defaultLookup().find(name));
    static MethodHandle ioctl = linker.downcallHandle(
            lookup.find("ioctl").get(),
            FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.ADDRESS),
            Linker.Option.firstVariadicArg(2)
    );

    static VarHandle lookupVarHandle(MemoryLayout.PathElement... elements) {
        VarHandle vh = winsize.LAYOUT.varHandle(elements);
        vh = MethodHandles.insertCoordinates(vh, vh.coordinateTypes().size() - 1, 0L);
        return vh;
    }

    static class winsize {
        static int  TIOCGWINSZ;
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



        static final GroupLayout LAYOUT;
        private static final VarHandle ws_col;
        private static final VarHandle ws_row;
        private static final VarHandle ws_xpixels;
        private static final VarHandle ws_ypixels;
        static {
            LAYOUT = MemoryLayout.structLayout(
                    ValueLayout.JAVA_SHORT.withName("ws_row"),
                    ValueLayout.JAVA_SHORT.withName("ws_col"),
                    ValueLayout.JAVA_SHORT.withName("ws_xpixels"),
                    ValueLayout.JAVA_SHORT.withName("ws_ypixels"));
            ws_row = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_row"));
            ws_col = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_col"));
            ws_xpixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_xpixels"));
            ws_ypixels = lookupVarHandle(MemoryLayout.PathElement.groupElement("ws_ypixels"));
        }

        private final java.lang.foreign.MemorySegment seg;

        winsize() {
            seg = java.lang.foreign.Arena.ofAuto().allocate(LAYOUT);
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

        public void get() {
            int res = 0;
            try {
                res = (int) ioctl.invoke(1, (long) winsize.TIOCGWINSZ, seg);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
            if (res == 0) {
                System.out.println("row " + ws_row()+" col " + ws_col());
                System.out.println("x_pixels " + ws_xpixels()+" y_pixels " + ws_ypixels());
            }else{
                System.out.println("ioctl failed ");
            }
        }
    }
        public static void main(String[] args) throws Throwable {
                winsize ws = new winsize();
                ws.get();

        }
    }