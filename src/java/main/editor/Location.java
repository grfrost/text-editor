package editor;

public record Location<T extends RowColBounds<T>>(T bounds, int row, int  col) implements RowCol<Location<T>>{
    public static <T extends RowColBounds<T>> Location<T> of(T bounds, int y, int x) {
        return new Location<>(bounds, y, x);
    }
}
