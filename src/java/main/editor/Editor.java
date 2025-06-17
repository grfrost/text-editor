package editor;

import editor.terminal.ANISTerminal;
import editor.terminal.WindowSize;
import editor.terminal.jline.JlineTerminal;

import java.io.IOException;
import java.nio.file.Path;

public class Editor {
    static int leftmargin = 12; // Line # + line len

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
                terminal.fill(windowSize.maxCol(), "Screen: " + windowSize.maxDims()
                        + " Lines:" + content.maxRow()
                        + " Viewport " + viewport.dims()
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
                content.insertNewLineAndMoveCursorToNextLine(cursor);
            } else if (key == Key.BACKSPACE || key == Key.CtrlH || key == Key.DEL) {
                content.deleteCharBeforeCursorAndAdjustCursor(cursor);
            } else if (key == Key.ARROW_UP && !cursor.onFirstRow(content)) {
                cursor.up(1);
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
            } else if (key == Key.ARROW_DOWN && !cursor.onLastRow(content)) {
                cursor.down(1);
                if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                    cursor.col(l.maxCol());
                }
            } else if (key == Key.ARROW_LEFT && !cursor.onFirstCol(line)) {
                cursor.left();
            } else if (key == Key.ARROW_RIGHT && !cursor.onLastCol(line)) {
                cursor.right();
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
            }
        } else {
            content.insertCharAndMoveCursorRight(cursor, key);
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