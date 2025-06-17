package editor.terminal;

import jdk.internal.org.jline.terminal.Attributes;
import jdk.internal.org.jline.terminal.Terminal;
import jdk.internal.org.jline.terminal.TerminalBuilder;
import jdk.internal.org.jline.utils.NonBlockingReader;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;

public class JlineTerminal implements ANISTerminal<JlineTerminal> {
    @Override
    public boolean isatty() {
        return true;
    }

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

    @Override
    public JlineTerminal enableRawMode() {
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

    public JlineTerminal clearScreen() {
        esc("[2J");

        return self();
    }

    public JlineTerminal cursorHome() {
        esc("[H");
        return self();
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

    @Override
    public JlineTerminal disableRawMode() {
        terminal.setAttributes(cooked);
        return this;
    }

    @Override
    public WindowSize getWindowSize() {
        return WindowSize.of( terminal.getHeight(),  terminal.getWidth());
    }

    @Override
    public int read() {
        try {
            NonBlockingReader reader = terminal.reader();
            int i = reader.read();
            return i;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JlineTerminal write(String s) {
        terminal.writer().write(s);
        terminal.writer().flush();
        return self();
    }

    Terminal terminal;

    public JlineTerminal() throws IOException {
        TerminalBuilder builder = TerminalBuilder.builder();
        this.terminal = builder.encoding(Charset.defaultCharset())
                .exec(false)
                .ffm(true)
                .nativeSignals(false)
                .systemOutput(TerminalBuilder.builder().computeSystemOutput())
                .build();


        System.out.println(terminal.getClass().getSimpleName());


    }
}
