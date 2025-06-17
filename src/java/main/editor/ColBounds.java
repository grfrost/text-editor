package editor;

public interface ColBounds<T extends ColBounds<T>>  {
    int maxCol();
    int minCol();
    default boolean isColToRight(int c) {
        return c >= maxCol();
    }

    default boolean isColToLeft(int c) {
        return c < maxCol();
    }

    default boolean containsCol(int r) {
        return r>=0&& r< maxCol();
    }

    default boolean containsCol(Col<?> c) {
        return containsCol(c.col());
    }

    default int width() {
        return maxCol() - minCol();
    }
}
