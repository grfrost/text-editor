package editor;

import jdk.swing.interop.SwingInterOpUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Content(Path path, List<Line> c, boolean ok) {



    public record Line(String text) {
        public int width() {
            return text.length();
        }
    }

    public static Content of(Path path) {
        if (Files.exists(path)) {
            try (Stream<String> stream = Files.lines(path)) {
                return new Content(path, stream.map(Line::new).collect((Collectors.toCollection(ArrayList::new))), true);
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


    public int height() {
        return c.size();
    }

    public Line line(int line) {
        return (line>=0 && line < height()) ? c.get(line) : null;
    }

    public void deleteLine(int line) {
        if (line >= 0 && line < c.size()) {
            c.remove(line);
        }
    }

    public boolean save() {
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



    int lineWidth(int y){
        return (line(y) instanceof Line line)?line.width():0;
    }




    void insertChar(int y, int x, int c) {
        if (line(y) instanceof Line line && x>=0&& x<line.width() ) {
            this.c.set(y, new Content.Line(new StringBuilder(line.text()).insert(x, (char) c).toString()));
        }
    }

    void deleteChar(int y, int x) {
        if (line(y) instanceof Line line ) {
            this.c().set(y, new Content.Line(new StringBuilder(line.text()).deleteCharAt(x).toString()));
        }
    }

    void appendAtLine(int y, Content.Line append) {
        this.c().set(y, new Content.Line(this.c().get(y).text() + append));
    }

      void insertRowAt(int y, Content.Line rowContent) {
        if (y >= 0 || y < height()) {
            this.c().add(y, rowContent);
        }
    }

      void insertChar(Cursor cursor, Key k) {
        if (cursor.x() == height()) {
            insertRowAt(height(), new Content.Line(""));
        }
        insertChar(cursor.y(),cursor.x(), k.v());
    }

      void insertNewLine(Cursor cursor) {
        if (cursor.x() == 0) {
            insertRowAt(cursor.y(), new  Content.Line(""));
        } else {
            insertRowAt(cursor.y() + 1, new Content.Line(this.c().get(cursor.y()).text().substring(cursor.x())));
            this.c().set(cursor.y(), new Content.Line(this.c().get(cursor.y()).text().substring(0, cursor.x())));
        }

    }
}
