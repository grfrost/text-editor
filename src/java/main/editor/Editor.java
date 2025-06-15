package editor;

import editor.terminal.Terminal;
import editor.terminal.ffm.MacOrUnixTerminal;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;

public class Editor {


    record Screen(int rows, int cols) {
    }



    private  void insertChar(Key k) {
        if (cursor.x() == content.size()) {
            // append row
            insertRowAt(content.size(), "");
        }
        insertCharIntoRow(cursor.row(), cursor.col(), k.v());
        cursor = cursor.right();
    }

    private  void deleteChar() {
        if (cursor.y() == content.size()) {
            return;
        }

        if (cursor.x() == 0 && cursor.y() == 0) {
            return;
        }
        if (cursor.x() > 0) {
            deleteCharFromRow(cursor.row(), cursor.col() - 1);
            cursor = cursor.left();
        } else {
            cursor = cursor.x(content.line(cursor.y() - 1).length());
            appendStringToRow(cursor.col() - 1, content.line(cursor.row()));
            content.deleteLine(cursor.row());
            cursor = cursor.up();

        }
    }


    private  void appendStringToRow(int at
            , String append) {
        content.content().set(at, content.content().get(at) + append);
    }

    private  void insertRowAt(int at, String rowContent) {
        if (at < 0 || at > content.size()) return;

        content.content().add(at, rowContent);
    }


    private  void insertNewLine() {
        if (cursor.x() == 0) {
            insertRowAt(cursor.y(), "");
        } else {
            insertRowAt(cursor.y() + 1, content.content().get(cursor.y()).substring(cursor.x()));
            content.content().set(cursor.y(), content.content().get(cursor.y()).substring(0, cursor.x()));
        }
        cursor = cursor.down().column(0);
    }

    private  void insertCharIntoRow(int row, int at, int c) {
        if (at < 0 || at > content.content().get(row).length()) at = content.content().get(row).length();
        String editedLine = new StringBuilder(content.content().get(row)).insert(at, (char) c).toString();
        content.content().set(row, editedLine);
    }

    private  void deleteCharFromRow(int row, int at) {
        if (at < 0 || at > content.content().get(row).length()) return;
        String editedLine = new StringBuilder(content.content().get(row)).deleteCharAt(at).toString();
        content.content().set(row, editedLine);
    }

    private  void scroll() {
        if (cursor.y() >= screen.rows() + offset.y()) {
            offset = offset.x(cursor.y() - screen.rows() + 1);
        } else if (cursor.y() < offset.y()) {
            offset = offset.y(cursor.y());
        }

        if (cursor.x() >= screen.cols() + offset.x()) {
            offset = offset.x(cursor.x() - screen.cols() + 1);
        } else if (cursor.x() < offset.x()) {
            offset = offset.x(cursor.x());
        }
    }


    private  void refreshScreen() {
        scroll();
        StringBuilder builder = new StringBuilder();
        drawCursorInTopLeft(builder);
        drawContent(builder);
        drawStatusBar(builder);
        drawCursor(builder);
        System.out.print(builder);
    }

    private  void drawCursorInTopLeft(StringBuilder builder) {
        builder.append("\033[H");
    }

    private  void drawCursor(StringBuilder builder) {
        builder.append(String.format("\033[%d;%dH", cursor.y() - offset.y() + 1, cursor.x() - offset.y() + 1));
    }

    public  void setStatusMessage(String message) {
        statusMessage = message;
    }

    public  void clearStatusMessage() {
        statusMessage = null;
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
            int current = lastMatch;

            for (int i = 0; i < content.size(); i++) {
                current += searchDirection == SearchDirection.FORWARDS ? 1 : -1;
                if (current == -1) {
                    current = content.size() - 1;
                } else if (current == content.size()) {
                    current = 0;
                }

                String line = content.content().get(current);

                int match = line.indexOf(query);

                if (match != -1) {
                    lastMatch = current;
                    cursor = cursor.moveTo(match, current);
                    offset = offset.y(content.size());
                    return;
                }
            }
        });
    }

    private  void prompt(String initialMessage, BiConsumer<String, Key> callback) {
        String message = initialMessage;
        StringBuilder userInputBuilder = new StringBuilder();
        while (true) {
            setStatusMessage(message);
            refreshScreen();
            Key key = Key.readAndMap();
            if (key == Key.DEL || key == Key.CtrlH || key == Key.BACKSPACE) {
                if (userInputBuilder.length() > 0) {
                    userInputBuilder.deleteCharAt(userInputBuilder.length() - 1);
                    message = userInputBuilder.toString();
                }
            } else if (key == Key.ESC) {  // escap
                clearStatusMessage();
                callback.accept(userInputBuilder.toString(), key);
                return;
            } else if (key == Key.NL) { // user pressed enter
                clearStatusMessage();
                callback.accept(userInputBuilder.toString(), key);
                return;
            } else if (!Character.isISOControl(key.v()) && key.v() < 128) {
                userInputBuilder.append((char) key.v());
                message = userInputBuilder.toString();
            }

            callback.accept(userInputBuilder.toString(), key);
        }
    }

     Screen screen;

    private  void drawStatusBar(StringBuilder builder) {
        String toDraw = statusMessage != null ? statusMessage : ("Rows: " + screen.rows() + "X:" + cursor.x() + " Y: " + cursor.y());

        builder.append("\033[7m")
                .append(toDraw)
                .append(" ".repeat(Math.max(0, screen.cols() - toDraw.length())))
                .append("\033[0m");
    }

    private  void drawContent(StringBuilder builder) {
        for (int i = 0; i < screen.rows; i++) {
            int fileI = offset.y() + i;
            if (fileI >= content.size()) {
                builder.append("~");
            } else {
                String line = content.content().get(fileI);
                int lengthToDraw = line.length() - offset.x();

                if (lengthToDraw < 0) {
                    lengthToDraw = 0;
                }
                if (lengthToDraw > screen.cols) {
                    lengthToDraw = screen.cols();
                }

                if (lengthToDraw > 0) {
                    builder.append(line, offset.x(), offset.x() + lengthToDraw);
                }


            }
            builder.append("\033[K\r\n");
        }
    }


    private  void handleKey(Key key) {
        if (key == Key.CtrlQ) {
            exit();
        } else if (key == Key.NL) {
            insertNewLine();
        } else if (key == Key.CtrlF) {
            editorFind();
        } else if (key == Key.CtrlS) {
            if (content.save()) {
                setStatusMessage("Successfully saved file");
            } else {
                setStatusMessage("There was an error saving your file ");
            }
        } else if (List.of(Key.BACKSPACE, Key.CtrlH, Key.DEL).contains(key)) {
            deleteChar();
        } else if (List.of(Key.ARROW_UP, Key.ARROW_DOWN, Key.ARROW_LEFT, Key.ARROW_RIGHT, Key.HOME, Key.END, Key.PAGE_UP, Key.PAGE_DOWN).contains(key)) {
            moveCursor(key);
        } else {
            insertChar(key);
        }
    }
    Terminal terminal;
    private  Cursor cursor = new Cursor(0, 0);

    private  Offset offset = new Offset(0, 0);
     private  Content content ;
    private  String statusMessage;

    private  void exit() {
        System.out.print("\033[2J");
        System.out.print("\033[H");
        terminal.disableRawMode();
        System.exit(0);
    }


    private  void moveCursor(Key key) {
        String line = cursor.y() < content.size() ? content.content().get(cursor.y()) : "";
        int lineLength = line.length();
        if (key == Key.ARROW_UP) {
            cursor = cursor.up();
        } else if (key == Key.ARROW_DOWN && cursor.y() < content.size()) {
            cursor = cursor.down();
        } else if (key == Key.ARROW_LEFT) {
            cursor = cursor.left();
        } else if (key == Key.ARROW_RIGHT && cursor.x() < lineLength) {
            cursor = cursor.right();
        } else if (key == Key.PAGE_UP) {
            cursor = cursor.y(offset.y());
            for (int i = 0; i < screen.rows(); i++) {
                cursor = cursor.up();
            }
        } else if (key == Key.PAGE_DOWN) {
            cursor = cursor.y(offset.y() + screen.rows() - 1);
            if (cursor.y() > content.size()) {
                cursor = cursor.y(content.size());
            }
            for (int i = 0; i < screen.rows(); i++) {
                cursor = cursor.down();
            }
        } else if (key == Key.HOME) {
            cursor = cursor.x(0);
        } else if (key == Key.END) {
            cursor = cursor.x(lineLength);
        }
        String newLine = cursor.y() < content.size() ? content.content().get(cursor.y()) : "";
        if (newLine != null && cursor.x() > newLine.length()) {
            cursor = cursor.x(newLine.length());
        }
    }
    Editor(Terminal terminal) {
        this.terminal = terminal;

    }
    void edit(Content content) {
        this.content = content;
        terminal.enableRawMode();
        var windowSize = terminal.getWindowSize();
        screen = new Screen(windowSize.rows() - 1, windowSize.cols());
        while (true) {
            refreshScreen();
            handleKey(Key.readAndMap());
        }
    }

    public static void main(String[] args) throws IOException {
        try (Arena arena = Arena.ofConfined()) {
            Path path = Path.of(args[0]);
            Terminal terminal = new MacOrUnixTerminal(arena,0);
            if (false && terminal.isatty()) {
                System.out.println("is a tty");
                Content content = Content.of(path);
                Editor editor = new Editor(terminal);
                editor.edit(content);
            } else {
                System.out.println("Not a tty");
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}