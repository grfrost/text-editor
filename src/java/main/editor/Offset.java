package editor;

record Offset(int x, int y) implements XY<Offset> {
    public Offset moveTo(int x, int y) {
        return new Offset(x, y);
    }
}
