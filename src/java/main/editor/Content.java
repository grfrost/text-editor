package editor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record Content(Path path, List<String> content, boolean ok) {
    public static Content of(Path path) {
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

    public int size() {
        return content.size();
    }

    public String line(int line) {
        return (line < size()) ? content.get(line) : null;
    }

    public void deleteLine(int line) {
        if (line >= 0 && line < content.size()) {
            content.remove(line);
        }
    }

    public boolean save() {
        try {
            Files.write(path, content());
            return true;
        } catch (IOException e) {
            return false;
        }
    }

}
