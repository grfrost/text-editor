package editor;

public class  Cursor <T extends RowColBounds<T>> implements RowCol<Cursor<T>> {
    private int col;
    private int row;
    private Cursor(T parent,  int row, int col){
        this.row = row;
        this.col = col;
    }
    Cursor<T> self(){
        return this;
    }

    public static <T extends RowColBounds<T>>Cursor<T> of(T parent, int row, int col ){
        return new Cursor<>(parent, row,col);
    }
    public static <T extends RowColBounds<T>>Cursor<T> of( T parent ){
        return of(parent, 0,0);
    }

    @Override public int row() {
        return row;
    }

    @Override public int col() {
        return col;
    }

    public Cursor<T> col(int c) {
       this.col = c;
       return this;
    }

    public  Cursor<T> row(int r) {
        this.row = r;
        return self();
    }

    public  Cursor<T> right() {
        col++;
        return self();
    }

    public  Cursor<T> left() {
        col--;
        return self();
    }

    public  Cursor<T> up(int rows) {
        row -=rows;
        return self();
    }

    public  Cursor<T> down(int rows) {
        row +=rows;
        return self();
    }
}
