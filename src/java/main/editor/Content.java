package editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Content implements RowColBounds<Content> {


    public static class Line implements ColBounds<Line> {
        Content content;
        private String text;

        public Line(Content content, String text) {
            this.content = content;
            this.text = text;
        }

        public String text() {
            return text;
        }

        @Override
        public int maxCol() {
            return text.length();
        }

        @Override
        public int minCol() {
            return 0;
        }

        public static Line of(Content content,String s) {
            return new Line(content,s);
        }

        public void deleteChar(Col<?> col) {
            if (containsCol(col)) {
                 replace(new StringBuilder(text).deleteCharAt(col.col()).toString());
            }
        }

        public void insertChar(Col<?> col, char c) {
            replace(new StringBuilder(text).insert(col.col(), c).toString());
        }

        public void replace(String replacement) {
            text=replacement;
        }
    }

    final private List<Line> c;

    public Content(Stream<String> lines) {
        this.c = lines
                .map(s->new Content.Line(this, s))
                .collect((Collectors.toCollection(ArrayList::new)));
    }

    public Content(String text) {
        this(Stream.of(text.split("\n")));
    }

    public static Content of(Path path) {
        try {
            if (Files.exists(path)) {
                return new Content(Files.lines(path));
            } else {
                return of(Files.createFile(path));
            }
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int maxRow() {
        return c.size();
    }

    @Override
    public int maxCol() {
        throw new IllegalStateException("Does not make sense in this method");
       // return c.stream().map(Line::maxCol).max(Integer::compareTo).get();
    }

    @Override
    public int minRow() {
        return 0;
    }

    @Override
    public int minCol() {
        return 0;
    }

    Line lineAt(int row) {
        return containsRow(row) ? c.get(row) : null;
    }
    public Line line(Row<?> row) {
        return containsRow(row) ? c.get(row.row()) : null;
    }
    public Line lineAbove(Row<?> row) {
        return lineAt(row.row() - 1);
    }

    public Line lineBelow(Row<?> row) {
        return lineAt(row.row() + 1);
    }

    public void deleteLine(Row<?> cursor) {
        if (containsRow(cursor)) {
            c.remove(cursor.row());
        }
    }

    public boolean save(Path path) {
        try {
            StringBuilder sb = new StringBuilder();
            for (Line line : c) {
                sb.append(line.text).append("\n");
            }
            Files.write(path, sb.toString().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    Line set(int row, Line line) {
        this.c.set(row, line);
        return line;
    }

    void deleteChar(Cursor cursor) {
        if (line(cursor) instanceof Line line) {
            line.deleteChar(cursor);
        }
    }

    private Content.Line createLine(String s){
        return new Content.Line(this, s);
    }
    private Content.Line createEmptyLine(){
        return createLine("");
    }

    Line replace(Row<?> row, Line replacement) {
        if (line(row) instanceof Line line) {
            set(row.row(), replacement);
        }
        return replacement;
    }
    void appendAtLine(Row<?> row, Content.Line append) {
        replace(row, createLine(line(row).text + append));
    }

    void insertRowAt(int row, Content.Line rowContent) {
        if (containsRow(row)) {
            set(row, rowContent);
        }
    }

    void insertCharAndMoveCursorRight(Cursor cursor, Key k) {
        if (cursor.col() == maxRow()) {
            insertRowAt(maxRow(), createEmptyLine());
        }
        if (line(cursor) instanceof Line line && line.containsCol(cursor)) {
            line.insertChar(cursor, (char)k.v());
            cursor.right();
        }
    }

    void insertNewLineAndMoveCursorToNextLine(Cursor cursor) {
        if (line(cursor) instanceof Line line) {
            if (cursor.onFirstCol(line)){
                insertRowAt(cursor.row(), createEmptyLine());
                // Add an empty line above
            }else if (cursor.onLastCol(line)){
                insertRowAt(cursor.row()+1, createEmptyLine());
                // Add an empty line below
            }else{
                String text = line(cursor).text();
                line.replace(text.substring(0, cursor.col()));
                Line right = createLine(text.substring(cursor.col()));
                insertRowAt(cursor.row()+1, right);
            }
            cursor.down(1).col(0);
        }
    }
    public void deleteCharBeforeCursorAndAdjustCursor(Cursor cursor) {
        if (line(cursor) instanceof Content.Line line ) {
            if (cursor.onFirstCol(line)) {
                if (!cursor.onFirstRow(this)){
                    var lineAbove = lineAbove(cursor);
                    var text = lineAbove.text();
                    text = text.substring(0, text.length() - 1);
                    text = text + line.text();
                    lineAbove.replace(text);
                    deleteLine(cursor);
                    cursor.up(1).col(text.length());
                }
            }else {
                cursor.left();
                line.deleteChar(cursor);
            }
        }
    }
}
