package editor;

public interface RowColBounds<T extends RowColBounds<T>> extends RowBounds<T>,ColBounds<T>{
    default String dims(){
        return minDims()+"-"+maxDims();
    }
    @FunctionalInterface
    interface Factory<T>{
        T create(int minRow, int maxRow, int minCol, int maxCol);
    }

    default String maxDims(){
        return maxRow()+","+maxCol();
    }
    default String minDims(){
        return minRow()+","+maxRow();
    }

    default String clip(String text) {
        if (text.length() < minCol()) {
            return "<";
        } else if ((text.length() - minCol()) >= maxCol()-minCol()) {
            return text.substring(minCol(), maxCol());
        } else {
            return text.substring(minCol());
        }
    }

    default  T track(RowCol<?> rowcol, Factory<T> factory) {
        int newMinCol=minCol();
        if (rowcol.isRightOf(this)) {
            newMinCol =Math.max(0, rowcol.col() - width() - 1);
        } else if (rowcol.isLeftOf(this)) {
            newMinCol = rowcol.col();
        }
        int newMinRow=minRow();
        if (rowcol.isBelow(this)) {
            newMinRow = Math.max(0, rowcol.row() - height() - 1);
        } else if (rowcol.isAbove(this)) {
            newMinRow = rowcol.row();
        }
        return (minRow()!=newMinRow || minCol()!=newMinCol)
                ?factory.create(newMinRow,newMinCol, newMinRow+height(), newMinCol+width())
                : (T)this;
    }

}
