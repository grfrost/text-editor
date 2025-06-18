package editor;

public record Viewport(int minRow, int minCol, int maxRow, int maxCol) implements RowColBounds<Viewport> {
    static private Viewport of(int minRow, int minCol, int maxRow, int maxCol) {
        return new Viewport(minRow, minCol, maxRow, maxCol);
    }
    static Viewport of(int maxRow, int maxCol) {
        return of (0,0,maxRow,maxCol);
    }
    Viewport track(RowCol<?>rowCol) {
       return this.track(rowCol, Viewport::new);
    }
}
