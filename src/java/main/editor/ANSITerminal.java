package editor;

import jdk.internal.org.jline.terminal.Attributes;
import jdk.internal.org.jline.terminal.Terminal;
import jdk.internal.org.jline.terminal.TerminalBuilder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;
import java.util.function.Supplier;

public class ANSITerminal implements  ANSI<ANSITerminal>, RowColBounds<ANSITerminal>, Supplier<Integer>,RowCol<ANSITerminal> {
    String get(Attributes a) {
        StringBuilder sb = new StringBuilder();
        EnumSet<?> o = a.getOutputFlags();
        sb.append("o{");
        for (var flag : o) {
            sb.append(flag).append(" ");
        }
        sb.append("},");
        EnumSet<?> i = a.getInputFlags();
        sb.append("i{");
        for (var flag : i) {
            sb.append(flag).append(" ");
        }
        sb.append("},");
        var l = a.getLocalFlags();
        sb.append("l{");
        for (var flag : l) {
            sb.append(flag).append(" ");
        }
        sb.append("},");
        var c = a.getControlFlags();
        sb.append("c{");
        for (var flag : c) {
            sb.append(flag).append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    Attributes cooked;
    Attributes raw;

    public ANSITerminal raw() {
        cooked = terminal.getAttributes();
        raw = terminal.getAttributes();
        //   raw.copy(cooked);
        raw.setLocalFlag(Attributes.LocalFlag.ICANON, false);
        raw.setLocalFlag(Attributes.LocalFlag.ECHO, false);
        raw.setLocalFlag(Attributes.LocalFlag.IEXTEN, false);
        raw.setLocalFlag(Attributes.LocalFlag.ISIG, false);

        // termios.c_lflag &= ~(UnixTerminal.LibC.ECHO | UnixTerminal.LibC.ICANON | UnixTerminal.LibC.IEXTEN | UnixTerminal.LibC.ISIG);
        raw.setInputFlag(Attributes.InputFlag.IXON, false);
        raw.setInputFlag(Attributes.InputFlag.ICRNL, false);
        //termios.c_iflag &= ~(UnixTerminal.LibC.IXON | UnixTerminal.LibC.ICRNL);
        raw.setOutputFlag(Attributes.OutputFlag.OPOST, false);
        //termios.c_oflag &= ~(UnixTerminal.LibC.OPOST);
        terminal.setAttributes(raw);
        // raw = terminal.getAttributes();
        String cookedString = get(cooked);
        String rawString = get(raw);
        System.out.println("raw: " + rawString);
        System.out.println("cooked: " + cookedString);
        return this;
    }


    public void getMouse() {
        if (terminal.hasMouseSupport()) {

            int[] pos = new int[1];
            // terminal.enterRawMode();
            var c = terminal.getCursorPosition(i -> {
                pos[0] = i;
            });


            System.out.println(pos[0] + " " + c.getX() + " " + c.getY());
        } else {
            System.out.println("No mouse support");
        }
    }


    public ANSITerminal cooked() {
        terminal.setAttributes(cooked);
        return this;
    }

    @Override
    public int minRow(){
        return 0;
    }

    @Override
    public int maxRow(){
        return  terminal.getHeight();
    }
    @Override
    public int minCol(){
        return 0;
    }

    @Override
    public int maxCol(){
        return  terminal.getWidth();
    }


    @Override
    public Integer get() {
        try {
            return terminal.reader().read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ANSITerminal apply(String s) {
        terminal.writer().write(s);
        return self();
    }

    public ANSITerminal flush() {
        terminal.writer().flush();
        return self();
    }

    private Terminal terminal;
    int row;
    int col;
    @Override public int row(){return row;}
    @Override public int col(){return col;}
    @Override public ANSITerminal row(int r){row=r;return self();}
    @Override public ANSITerminal col(int c){col=c;return self();}

    public ANSITerminal() throws IOException {
        this.terminal = TerminalBuilder.builder().encoding(Charset.defaultCharset())
                .exec(false)
                .ffm(true)
                .nativeSignals(false)
                .systemOutput(TerminalBuilder.builder().computeSystemOutput())
                .build();
    }
   // https://github.com/cronvel/terminal-kit/blob/master/lib/termconfig/xterm.js#L212-L224
    ANSITerminal blink(){
        return csiQuery().ints(2).apply("c");
    }
    ANSITerminal block(){
        return csiQuery().ints(6).apply("c");
    }
    ANSITerminal saveCursorPos(){
        return csi().apply("s");
    }
    ANSITerminal restoreCursorPos(){
        return csi().apply("u");
    }

    ANSITerminal redCursor(){
        //2 = underline
        //0 default
        //1 invisible
        return csiQuery().ints(2,0,0).apply("c");
    }

    /*
    To get normal blinking underline, use::

	echo -e '\033[?2c'

To get blinking block, use::

	echo -e '\033[?6c'

To get red non-blinking block, use::

	echo -e '\033[?17;0;64c'
     */

     String getCursor(){
        flush();
        csi().ints(6).apply("n");
        flush();
        char[] response=new  char[20];
        int len=0;
        while(len<response.length && (response[len]=(char)get().intValue())!='R'){
            len++;
        }
        return new String(response,0,len);
    }
}
