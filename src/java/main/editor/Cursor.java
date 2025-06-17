package editor;

public class Cursor implements RowCol<Cursor> {
    private int col;
    private int row;
    private Cursor( int row, int col){
        this.row = row;
        this.col = col;
    }
    public static Cursor of( ){
        return new Cursor(0,0);
    }
    public static Cursor of(int row, int col ){
        return new Cursor(row,0);
    }

    @Override public int row() {
        return row;
    }

    @Override public int col() {
        return col;
    }

    public Cursor col(int c) {
       this.col = c;
       return this;
    }

    public Cursor row(int r) {
        this.row = r;
        return this;
    }

    public Cursor right() {
        col++;
        return this;
    }

    public Cursor left() {
        col--;
        return this;
    }

    public Cursor up(int rows) {
        row -=rows;
        return this;
    }

    public Cursor down(int rows) {
        row +=rows;
        return this;
    }

  /*  public Cursor moveToStartOfNextLine() {
        down(1);
        col(0);
        return this;
    } */
}
