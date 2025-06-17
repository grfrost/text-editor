package editor;

public interface Col<T extends Col<T>> {
    int col();
    default boolean isRightOf(ColBounds<?> bounds) {
        return col()>=bounds.maxCol();
    }
    default boolean isLeftOf(ColBounds<?> bounds) {
        return col()<bounds.minCol();
    }

    default boolean onFirstCol(ColBounds<?> bounds){
        return col()==bounds.minCol();
    }
    default boolean onLastCol(ColBounds<?> bounds){
        return col()==bounds.maxCol()-1;
    }
}
