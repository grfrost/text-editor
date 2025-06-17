package editor;

public interface Row<T extends Row<T>> {
    int row();

    default boolean isBelow(RowBounds<?> bounds) {
        return row()>=bounds.maxRow();
    }
    default boolean isAbove(RowBounds<?> bounds) {
        return row()<bounds.minRow();
    }

    default boolean onFirstRow(RowBounds<?> bounds){
        return row()==bounds.minRow();
    }
    default boolean onLastRow(RowBounds<?> bounds){
        return row()==bounds.maxRow()-1;
    }
}
