package editor;

import editor.terminal.ANISTerminal;
import editor.terminal.JlineTerminal;
import editor.terminal.WindowSize;

import java.io.IOException;
import java.nio.file.Path;

public class Editor {
    record Status(String message) {
        public static Status of(String messae) {
            return new Status(messae);
        }
    }

    static int leftmargin = 12; // Line # + line len
    Status status = Status.of("OK");

    private void refreshScreen() {
        viewport = viewport.track(cursor);
        terminal.cursorHome();
        for (Cursor c = Cursor.of(viewport.minRow(), 0); c.row() < viewport.maxRow(); c.down(1)) {
            if (content.line(c) instanceof Content.Line line) {
                terminal.line(String.format("%-" + (leftmargin - 2) + "s", String.format("%4d %4d", c.row(), line.maxCol())), viewport.clip(line.text()));
            } else {
                terminal.line("...........", "~");
            }
        }
        terminal.inv(_ ->
                terminal.fill(windowSize.maxCol(), windowSize.maxDims()
                        + " Lines:" + content.maxRow()
                        + " View " + viewport.dims()
                        + " Curs " + cursor.row() + ":" + cursor.col()
                        + " Stat " + status.message()
                )
        );
        terminal.rowCol(cursor.row() - viewport.minRow(), cursor.col() - viewport.minCol() + leftmargin);
    }

    private void handleKey(Key key) {
        if (key == Key.CtrlQ) {
            exit();
        } else if (key == Key.CtrlF) {
            //editorFind();
        } else if (key == Key.CtrlS) {
            if (content.save(path)) {
                //setStatusMessage("Successfully saved file");
            } else {
                // setStatusMessage("There was an error saving your file ");
            }
        } else if (content.line(cursor) instanceof Content.Line line) {
            // These all require us to be in the content
            if (key == Key.NL) {
                if (cursor.onFirstCol(line)) {
                    content.insertEmptyLine(cursor);
                    cursor.down(1);
                    status= Status.of("INS NL COL1");
                    // Add an empty line above
                } else if (cursor.onLastCol(line)) {
                    cursor.down(1);
                    content.insertEmptyLine(cursor);
                    status= Status.of("INS NL LINE END");
                    // Add an empty line below
                } else {
                    String text = line.text();
                    line.replace(text.substring(0, cursor.col()));
                    Content.Line right = content.createLine(text.substring(cursor.col()));
                    content.insertRowAt(cursor.row() + 1, right);
                    status = Status.of("INS NL mid line");
                    cursor.down(1);
                }
                cursor.col(0);
            } else if (key == Key.BACKSPACE || key == Key.CtrlH || key == Key.DEL) {
                if (cursor.onFirstCol(line)) {
                    if (!cursor.onFirstRow(content)) {
                        var lineAbove = content.lineAbove(cursor);
                        var text = lineAbove.text();
                        text = text.substring(0, text.length() - 1);
                        text = text + line.text();
                        lineAbove.replace(text);
                        content.deleteLine(cursor);
                        cursor.up(1).col(text.length());
                        status = Status.of("DEL CH and joined lines");
                    }else{
                        status = Status.of("DEL CH ignored");
                    }
                } else {
                    cursor.left();
                    if (line.containsCol(cursor)) {
                        line.deleteChar(cursor);
                    }
                    status = Status.of("DEL CH in line");
                }

            } else if (key == Key.ARROW_UP && !cursor.onFirstRow(content)) {
                cursor.up(1);
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
                status = Status.of("ARROW_UP");
            } else if (key == Key.ARROW_DOWN && !cursor.onLastRow(content)) {
                cursor.down(1);
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
                status = Status.of("ARROW_DOWN");
            } else if (key == Key.ARROW_LEFT && !cursor.onFirstCol(line)) {
                cursor.left();
                status = Status.of("<--");
            } else if (key == Key.ARROW_RIGHT && !cursor.onLastCol(line)) {
                cursor.right();
                status = Status.of("-->");
            } else if (key == Key.PAGE_UP) {
                for (int i = 0; !cursor.onFirstRow(content) && i < viewport.height(); i++) {
                    cursor.up(1);
                }
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
            } else if (key == Key.PAGE_DOWN) {
                for (int i = 0; !cursor.onLastRow(content) && i < viewport.height(); i++) {
                    cursor.down(1);
                }
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
            } else if (key == Key.HOME) {
                cursor.col(0);
            } else if (key == Key.END) {
                cursor.col(line.maxCol());
            } else if (line.containsCol(cursor)) {
                line.insertChar(cursor, (char) key.v());
                cursor.right();
                status = Status.of("inserted " + key.v());
            } else {
                status = Status.of("UNHANDLED 1 " + key.v());
            }
        } else {
            status = Status.of("UNHANDLED 2" + key.v());
        }
    }

    private ANISTerminal<?> terminal;
    private Cursor cursor;

    private Viewport viewport;
    private Content content;

    private void exit() {
        terminal.clearScreen().cursorHome().disableRawMode();
        System.exit(0);
    }

    Editor(ANISTerminal<?> terminal) {
        this.terminal = terminal;
    }

    Path path;
    WindowSize windowSize;

    void edit(Path path) {
        this.path = path;
        this.content = Content.of(path);
        this.cursor = Cursor.of();
        terminal.enableRawMode();
        this.windowSize = terminal.getWindowSize();
        this.viewport = Viewport.of(0, 0, windowSize.maxRow() - 1, windowSize.maxCol() - leftmargin);
        while (true) {
            refreshScreen();
            handleKey(Key.readAndMap(terminal));
        }
    }

    public static void main(String[] args) throws IOException {
        //  try (Arena arena = Arena.ofConfined()) {
        Path path = Path.of(args[0]);
        var terminal = new JlineTerminal();//new MacOrUnixTerminal(arena,0);
        // if (terminal.isatty()) {
        //   terminal.write("is a tty\n");
        Editor editor = new Editor(terminal);
        editor.edit(path);
        // } else {
        //   terminal.write("Not a tty\n");
        // }
        //  } catch (Exception e) {
        //     e.printStackTrace();
        // }
    }
}