package editor;

import jdk.internal.org.jline.terminal.MouseEvent;
import jdk.internal.org.jline.terminal.Terminal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.function.Supplier;

public class ANSITerminal implements  ANSI<ANSITerminal>, RowColBounds<ANSITerminal>, Supplier<Integer>,RowCol<ANSITerminal> {
    final  jdk.internal.org.jline.terminal.Attributes cooked;
    final jdk.internal.org.jline.terminal.Attributes raw;

    public ANSITerminal raw() {
        terminal.setAttributes(raw);
        return self();
    }
    public Location<ANSITerminal> getCursorLocation() {

            int[] pos = new int[1];
            var terminalCursor = terminal.getCursorPosition(i -> {
                pos[0] = i;
            });

            //  System.out.println(pos[0] + " " + terminalCursor.getX() + " " + terminalCursor.getY());
            return Location.of(self(), terminalCursor.getY(),terminalCursor.getX());

    }

    Location<ANSITerminal> mouseLocation;

    public Location<ANSITerminal> getMouseLocation() {
      //  int[] pos = new int[1];
        MouseEvent me = terminal.readMouseEvent();
        if (me == null) {
            return null;
        }else {
            return Location.of(self(), me.getY(), me.getX());
        }
    }


    public ANSITerminal cooked() {
        terminal.setAttributes(cooked);
        return self();
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

    private jdk.internal.org.jline.terminal.Terminal terminal;
    int row;
    int col;
    @Override public int row(){return row;}
    @Override public int col(){return col;}
    @Override public ANSITerminal row(int r){row=r;return self();}
    @Override public ANSITerminal col(int c){col=c;return self();}

    public ANSITerminal() throws IOException {
        this.terminal = jdk.internal.org.jline.terminal.TerminalBuilder.builder().encoding(Charset.defaultCharset())
                .exec(false)
                .ffm(true)
                .nativeSignals(false)
              //  .systemOutput(TerminalBuilder.builder().computeSystemOutput())
                .build();
        cooked = terminal.getAttributes();
        raw = terminal.getAttributes();
        raw.setLocalFlag(jdk.internal.org.jline.terminal.Attributes.LocalFlag.ICANON, false);
        raw.setLocalFlag(jdk.internal.org.jline.terminal.Attributes.LocalFlag.ECHO, false);
        raw.setLocalFlag(jdk.internal.org.jline.terminal.Attributes.LocalFlag.IEXTEN, false);
        raw.setLocalFlag(jdk.internal.org.jline.terminal.Attributes.LocalFlag.ISIG, false);
        raw.setInputFlag(jdk.internal.org.jline.terminal.Attributes.InputFlag.IXON, false);
        raw.setInputFlag(jdk.internal.org.jline.terminal.Attributes.InputFlag.ICRNL, false);
        raw.setOutputFlag(jdk.internal.org.jline.terminal.Attributes.OutputFlag.OPOST, false);

        if (terminal.hasMouseSupport()) {
            Terminal.MouseTracking mouseTracking;
            terminal.trackMouse( Terminal.MouseTracking.Button);
        }
    }

    ANSITerminal saveCursorPos(){
        return csi().apply("s");
    }
    ANSITerminal restoreCursorPos(){
        return csi().apply("u");
    }
/*
 // https://github.com/cronvel/terminal-kit/blob/master/lib/termconfig/xterm.js#L212-L224
    /*
    ANSITerminal blink(){
        return csiQuery().ints(2).apply("c");
    }
    ANSITerminal block(){
        return csiQuery().ints(6).apply("c");
    }
    ANSITerminal redCursor(){
        //2 = underline
        //0 default
        //1 invisible
        return csiQuery().ints(2,0,0).apply("c");
    }

        To get normal blinking underline, use::

	echo -e '\033[?2c'

To get blinking block, use::

	echo -e '\033[?6c'

To get red non-blinking block, use::

	echo -e '\033[?17;0;64c'


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
    } */
}
