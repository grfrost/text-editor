package editor;

import java.util.function.Consumer;
import java.util.function.Function;

public interface ANSI<T extends ANSI<T>>  extends Function<String,T> {
    T row(int r);
    T col(int c);
    int col();
    int row();
    default T self(){
        return (T) this;
    }
    default T esc(){
        return apply("\033");
    }
    default T csi(){
        return esc().apply("[");
    }
    default T csiQuery(){
        return csi().apply("?");
    }
    default T hideCursor(){
        return csiQuery().ints(25).apply("l");
    }
    default T showCursor(){
        return csiQuery().ints(25).apply("h");
    }
    default T ints(int ...n){
        apply(String.valueOf(n[0]));
        for(int i=1; i<n.length; i++) {
            apply(";"+ n[i]);
        }
        return self();
    }

    default T rowCol(int r, int c ){
        row(r);
        col(c);
        return csi().ints(r,c).apply("H");
    }



  //  https://community.unix.com/t/mouse-tracking-in-terminal/383854/4
    // https://github.com/tinmarino/mouse_xterm/blob/master/mouse.sh

//https://github.com/tinmarino/mouse_xterm/blob/master/mouse.sh
    default T trackMouse(String s){
        return csiQuery().ints(1000,1006,1015).apply(s);
    }

    default T trackMouseStart(){
        return trackMouse("h");
    }
    default T trackMouseEnd(){
        return trackMouse("l");
    }

    default T clearScreen(){
        csi().ints(2).apply("J");
        return self();
    }
    default T cursorHome(){
        csi().apply("H");
        row(0);
        col(0);
        return self();
    }
    default T inv(){
        return csi().ints(7).apply("m");
    }
    default T reset(){
        return csi().ints(0).apply("m");
    }

    default T inv(Consumer<T> c){
        inv();
        c.accept(self());
        return reset();
    }
    default T nextLine(){
        row(row()+1);
        col(0);
        csi().apply("K\r\n");
        return self();
    }

   default T line(String prefix,String s){
        return inv(_-> apply(prefix)).apply(" ").apply(s).nextLine();
   }

    static ANSIBuffer of(StringBuilder stringBuilder){
        return new ANSIBuffer(stringBuilder);
     }
    default T repeat(String s, int count) {
        return apply(s.repeat(count));
    }

     default T fill(int cols, String s) {
         return apply(s).repeat(" ",Math.max(0, cols - s.length()));
     }

    // T flush();
    // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797


}
