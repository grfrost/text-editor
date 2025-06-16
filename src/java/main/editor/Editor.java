package editor;

import editor.terminal.ANISTerminal;
import editor.terminal.jline.JlineTerminal;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

public class Editor {


    record Screen(int height, int width) {
    }

    private  void refreshScreen() {
        // Scroll cursor to correct position given offset x and y
        viewport.track(cursor);
        terminal.cursorHome();
        // Draw content
        for (int i = 0; i < viewport.height; i++) {
            if (content.line(i + viewport.y()) instanceof Content.Line line) {
                terminal.write(viewport.clip(line.text()));
            }else{
                terminal.write("~");
            }
            terminal.escBrace("K\r\n");
        }

        terminal.inv(_-> terminal.fill(
                        "Rows: " + screen.height() + "X:" + cursor.x() + " Y: " + cursor.y()
                        + " Lines:"+content.height()
                        + " Viewport "+viewport.x()+","+viewport.y()+"-"+(viewport.x()+ viewport.width)+","+viewport.y()+ viewport.height
                , screen.width)
        );
        terminal.rowCol( cursor.y() - viewport.y(), cursor.x() - viewport.x() + 1);
    }


    public enum SearchDirection {
        FORWARDS, BACKWARDS
    }

    static int lastMatch = -1;

    static SearchDirection searchDirection = SearchDirection.FORWARDS;


    public  void editorFind() {
        prompt("Search: %s (Use ESC/Arrows/Enter)", (query, key) -> {
            if (query == null || query.isBlank()) {
                lastMatch = -1;
                searchDirection = SearchDirection.FORWARDS;
                return;
            }
            if (key == Key.ARROW_RIGHT || key == Key.ARROW_DOWN) {
                searchDirection = SearchDirection.FORWARDS;
            } else if (key == Key.ARROW_LEFT || key == Key.ARROW_UP) {
                searchDirection = SearchDirection.BACKWARDS;
            } else {
                lastMatch = -1;
                searchDirection = SearchDirection.FORWARDS;
            }


            if (lastMatch == -1) searchDirection = SearchDirection.FORWARDS;
            int y = lastMatch;

            for (int i = 0; i < content.height(); i++) {
                y += searchDirection == SearchDirection.FORWARDS ? 1 : -1;
                if (y == -1) {
                    y = content.height() - 1;
                } else if (y == content.height()) {
                    y = 0;
                }

                if (content.line(y) instanceof Content.Line line) {
                    int x = line.text().indexOf(query);

                    if (x != -1) {
                        lastMatch = y;
                        cursor.xy(x, y);
                        viewport.track(cursor);
                        return;
                    }
                }else{
                    return;
                }
            }
        });
    }

    private  void prompt(String initialMessage, BiConsumer<String, Key> callback) {
        String message = initialMessage;
        StringBuilder userInputBuilder = new StringBuilder();
        while (true) {
            //setStatusMessage(message);
            refreshScreen();
            Key key = Key.readAndMap(terminal);
            if (key.deletesCharBefore() && userInputBuilder.length() > 0) {
                userInputBuilder.deleteCharAt(userInputBuilder.length() - 1);
            } else if (key.clearsStatus()) {  // escap
                //clearStatusMessage();
            } else if (key.canBeInserted()) {
                userInputBuilder.append((char) key.v());
                message = userInputBuilder.toString();
            }

            callback.accept(userInputBuilder.toString(), key);
        }
    }

     Screen screen;



    private  void handleKey(Key key) {
        if (key == Key.CtrlQ) {
            exit();
        } else if (key == Key.NL) {
            content.insertNewLine(cursor);
            cursor.startOfNextLine();
        } else if (key == Key.CtrlF) {
            editorFind();
        } else if (key == Key.CtrlS) {
            if (content.save()) {
               //setStatusMessage("Successfully saved file");
            } else {
               // setStatusMessage("There was an error saving your file ");
            }
        } else if (List.of(Key.BACKSPACE, Key.CtrlH, Key.DEL).contains(key)) {
            if (!(cursor.y() == content.height() || (cursor.x() == 0 && cursor.y() == 0))) {
                if (cursor.x() > 0) {
                    content.deleteChar(cursor.y(), cursor.x() - 1);
                    cursor.left();
                } else {
                    cursor.x(content.line(cursor.y() - 1).width());
                    content.appendAtLine(cursor.y() - 1, content.line(cursor.y()));
                    content.deleteLine(cursor.y());
                    cursor.up(1);
                }
            }
        } else if (List.of(Key.ARROW_UP, Key.ARROW_DOWN, Key.ARROW_LEFT, Key.ARROW_RIGHT, Key.HOME, Key.END, Key.PAGE_UP, Key.PAGE_DOWN).contains(key)) {
            moveCursor(key);
        } else {
            content.insertChar(cursor, key);
            cursor.right();
        }
    }
    ANISTerminal<?> terminal;
    private  Cursor cursor;

    private Viewport viewport ;
     private  Content content ;
    private  String statusMessage;

    private  void exit() {
        terminal.clearScreen().cursorHome().disableRawMode();
        System.exit(0);
    }


    private  void moveCursor(Key key) {
        if (content.line(cursor.y()) instanceof Content.Line line) {
            if (key == Key.ARROW_UP) {
                cursor.up(1);
            } else if (key == Key.ARROW_DOWN && cursor.y() < content.height()) {
                cursor.down(1);
            } else if (key == Key.ARROW_LEFT) {
                cursor.left();
            } else if (key == Key.ARROW_RIGHT && cursor.x() < line.width()) {
                cursor.right();
            } else if (key == Key.PAGE_UP) {
                cursor.y(viewport.y());
                cursor.up( screen.height());
            } else if (key == Key.PAGE_DOWN) {
                cursor.y(viewport.y() + screen.height() - 1);
                if (cursor.y() > content.height()) {
                    cursor.y(content.height());
                }
                cursor.down(screen.height());
            } else if (key == Key.HOME) {
                cursor.x(0);
            } else if (key == Key.END) {
                cursor.x(line.width());
            }
        }
    }
    Editor(ANISTerminal<?> terminal) {
        this.terminal = terminal;

    }
    void edit(Content content) {
        this.content = content;
        this.cursor = new Cursor( 0, 0);
       
        terminal.enableRawMode();
        var windowSize = terminal.getWindowSize();
        screen = new Screen(windowSize.height() - 1, windowSize.width());
        this.viewport = new Viewport( 0, 0,  windowSize.width(),windowSize.height());
        while (true) {
            refreshScreen();
            handleKey(Key.readAndMap(terminal));
        }
    }

    public static void main(String[] args) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            Path path = Path.of(args[0]);
            var terminal = new JlineTerminal();//new MacOrUnixTerminal(arena,0);
            if (terminal.isatty()) {
                terminal.write("is a tty\n");
                Content content = Content.of(path);
                Editor editor = new Editor(terminal);
                editor.edit(content);
            } else {
                terminal.write("Not a tty\n");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}