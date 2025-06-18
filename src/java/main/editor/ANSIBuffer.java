package editor;

public class ANSIBuffer implements ANSI<ANSIBuffer> {
    final StringBuilder stringBuilder;
    public ANSIBuffer(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
    }

    @Override
    public ANSIBuffer apply(String s) {
        stringBuilder.append(s);
        return self();
    }
    @Override
    public String toString() {
        return stringBuilder.toString();
    }

    int row;
    int col;
    @Override
    public ANSIBuffer row(int r) {
        row = r;
        return self();
    }

    @Override
    public ANSIBuffer col(int c) {
        col = c;
        return self();
    }

    @Override
    public int col() {
        return col;
    }

    @Override
    public int row() {
        return row;
    }
}
