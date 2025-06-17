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

        public static Line of(Content content, String s) {
            return new Line(content, s);
        }

        public void deleteChar(Col<?> col) {
            replace(new StringBuilder(text).deleteCharAt(col.col()).toString());
        }

        public void insertChar(Col<?> col, char c) {
            replace(new StringBuilder(text).insert(col.col(), c).toString());
        }

        public void replace(String replacement) {
            text = replacement;
        }
    }

    private Line lineAt(int row) {
        return containsRow(row) ? c.get(row) : null;
    }

    final private List<Line> c;

    public Content(Stream<String> lines) {
        this.c = lines
                .map(s -> new Content.Line(this, s))
                .collect((Collectors.toCollection(ArrayList::new)));
    }

    public Content(String text) {
        this(Stream.of(text.split("\n")));
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

    Content.Line createLine(String s) {
        return new Content.Line(this, s);
    }

    Content.Line createEmptyLine() {
        return createLine("");
    }

    void insertRowAt(int row, Content.Line rowContent) {
        if (containsRow(row)) {
            this.c.set(row, rowContent);
        }
    }
    void insertEmptyLine(Row<?> row) {
        insertRowAt(row.row(), createEmptyLine());
    }
}

