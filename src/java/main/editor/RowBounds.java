package editor;

public interface RowBounds<T extends RowBounds<T>> {
    int maxRow();

    int minRow();

    default boolean containsRow(int r) {
        return r >= 0 && r < maxRow();
    }

    default boolean containsRow(Row<?> c) {
        return containsRow(c.row());
    }
    default int height() {
        return maxRow() - minRow();
    }

    default int rowOffset(Row<?> r){
        return r.row() - minRow();
    }
}
