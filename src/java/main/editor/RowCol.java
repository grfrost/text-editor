package editor;

public interface RowCol<T extends RowCol<T>> extends Row<T>, Col<T> {
}
