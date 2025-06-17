package editor.terminal;



import editor.RowColBounds;

public record WindowSize(int minRow, int minCol, int maxRow, int maxCol) implements RowColBounds<WindowSize> {
   public static WindowSize of(int maxRow, int maxCol) {
       return new WindowSize(0, 0, maxRow, maxCol);
   }
}
