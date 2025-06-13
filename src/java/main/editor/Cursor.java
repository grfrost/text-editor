package editor;

record Cursor(int x, int y) implements XY<Cursor> {
    public Cursor moveTo(int x, int y) {
        return new Cursor(x, y);
    }

    public Cursor column(int col) {
        return moveTo(0, y());
    }

    public int row() {
        return y();
    }

    public int col() {
        return x();
    }


}
