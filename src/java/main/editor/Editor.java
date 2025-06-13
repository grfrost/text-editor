package editor;

import com.sun.jna.*;
import editor.terminal.Terminal;
import editor.terminal.WindowSize;
import editor.terminal.jna.MacOsTerminal;
import editor.terminal.jna.UnixTerminal;
import editor.terminal.jna.WindowsTerminal;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Editor {
    private static Cursor cursor = new Cursor(0, 0);

    private static Offset offset = new Offset(0, 0);

    record Screen(int rows, int cols) {
    }

    private static final Terminal terminal = Platform.isWindows()
            ? new WindowsTerminal() :
            Platform.isMac()
                    ? new MacOsTerminal()
                    : new UnixTerminal();

    record Content(Path path, List<String> content, boolean ok) {
        static Content of(Path path) {
            if (Files.exists(path)) {
                try (Stream<String> stream = Files.lines(path)) {
                    return new Content(path, stream.collect((Collectors.toCollection(ArrayList::new))), true);
                } catch (IOException e) {
                    return new Content(path, List.of(), false);
                }
            } else {
                try {
                    Files.createFile(path);
                    return new Content(path, List.of(), true);
                } catch (IOException e) {
                    return new Content(null, List.of(), false);
                }
            }
        }

        static Content empty() {
            return new Content(null, new ArrayList<>(), false);
        }

        int size() {
            return content.size();
        }

        String line(int line) {
            return (line < size()) ? content.get(line) : null;
        }

        void deleteLine(int line) {
            if (line >= 0 && line < content.size()) {
                content.remove(line);
            }
        }

        boolean save() {
            try {
                Files.write(path, content());
                return true;
            } catch (IOException e) {
                return false;
            }
        }

    }

    private static Content content = Content.empty();
    private static String statusMessage;


    private static void insertChar(Key k) {
        if (cursor.x() == content.size()) {
            // append row
            insertRowAt(content.size(), "");
        }
        insertCharIntoRow(cursor.row(), cursor.col(), k.v());
        cursor = cursor.right();
    }

    private static void deleteChar() {
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


    private static void appendStringToRow(int at
            , String append) {
        content.content.set(at, content.content.get(at) + append);
    }

    private static void insertRowAt(int at, String rowContent) {
        if (at < 0 || at > content.size()) return;

        content.content.add(at, rowContent);
    }


    private static void insertNewLine() {
        if (cursor.x() == 0) {
            insertRowAt(cursor.y(), "");
        } else {
            insertRowAt(cursor.y() + 1, content.content.get(cursor.y()).substring(cursor.x()));
            content.content.set(cursor.y(), content.content.get(cursor.y()).substring(0, cursor.x()));
        }
        cursor = cursor.down().column(0);
    }

    private static void insertCharIntoRow(int row, int at, int c) {
        if (at < 0 || at > content.content.get(row).length()) at = content.content.get(row).length();
        String editedLine = new StringBuilder(content.content.get(row)).insert(at, (char) c).toString();
        content.content.set(row, editedLine);
    }

    private static void deleteCharFromRow(int row, int at) {
        if (at < 0 || at > content.content.get(row).length()) return;
        String editedLine = new StringBuilder(content.content.get(row)).deleteCharAt(at).toString();
        content.content.set(row, editedLine);
    }

    private static void scroll() {
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


    private static void refreshScreen() {
        scroll();
        StringBuilder builder = new StringBuilder();
        drawCursorInTopLeft(builder);
        drawContent(builder);
        drawStatusBar(builder);
        drawCursor(builder);
        System.out.print(builder);
    }

    private static void drawCursorInTopLeft(StringBuilder builder) {
        builder.append("\033[H");
    }

    private static void drawCursor(StringBuilder builder) {
        builder.append(String.format("\033[%d;%dH", cursor.y() - offset.y() + 1, cursor.x() - offset.y() + 1));
    }

    public static void setStatusMessage(String message) {
        statusMessage = message;
    }

    public static void clearStatusMessage() {
        statusMessage = null;
    }


    public enum SearchDirection {
        FORWARDS, BACKWARDS
    }

    static int lastMatch = -1;

    static SearchDirection searchDirection = SearchDirection.FORWARDS;


    public static void editorFind() {
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

                String line = content.content.get(current);

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

    private static void prompt(String initialMessage, BiConsumer<String, Key> callback) {
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

    static Screen screen;

    private static void drawStatusBar(StringBuilder builder) {
        String toDraw = statusMessage != null ? statusMessage : ("Rows: " + screen.rows() + "X:" + cursor.x() + " Y: " + cursor.y());

        builder.append("\033[7m")
                .append(toDraw)
                .append(" ".repeat(Math.max(0, screen.cols() - toDraw.length())))
                .append("\033[0m");
    }

    private static void drawContent(StringBuilder builder) {
        for (int i = 0; i < screen.rows; i++) {
            int fileI = offset.y() + i;
            if (fileI >= content.size()) {
                builder.append("~");
            } else {
                String line = content.content.get(fileI);
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


    private static void handleKey(Key key) {
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

    private static void exit() {
        System.out.print("\033[2J");
        System.out.print("\033[H");
        terminal.disableRawMode();
        System.exit(0);
    }


    private static void moveCursor(Key key) {
        String line = cursor.y() < content.size() ? content.content.get(cursor.y()) : "";
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
        String newLine = cursor.y() < content.size() ? content.content.get(cursor.y()) : "";
        if (newLine != null && cursor.x() > newLine.length()) {
            cursor = cursor.x(newLine.length());
        }
    }

    public static void main(String[] args) throws IOException {
        Path path = Path.of(args[0]);
        content = Content.of(path);
        terminal.enableRawMode();
        WindowSize windowSize = terminal.getWindowSize();
        screen = new Screen(windowSize.rows() - 1, windowSize.cols());
        while (true) {
            refreshScreen();
            handleKey(Key.readAndMap());
        }
    }


}


/**/