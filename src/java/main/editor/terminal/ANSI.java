package editor.terminal;

import java.util.function.Consumer;

public interface ANSI<T extends ANSI<T>> {

    T write(String s);

    default T self(){
        return (T) this;
    }
    default T esc(){
        return write("\033");
    }
    default T esc(String s){
        return esc().write(s);
    }
    default T escBrace(String s){
        return esc().write("[").write(s);
    }

    default T rowCol(int y, int x ){
        return escBrace(String.format("%d;%dH", y, x));
    }
  //  https://community.unix.com/t/mouse-tracking-in-terminal/383854/4
    // https://github.com/tinmarino/mouse_xterm/blob/master/mouse.sh

//https://github.com/tinmarino/mouse_xterm/blob/master/mouse.sh
    default T trackMouseStart(){
        return escBrace("?1000;1006;1015h");
    }
    default T trackMouseEnd(){
        return escBrace("?1000;1006;1015l");
    }

    default T  clearScreen(){
        esc("[2J");
        return self();
    }
    default T cursorHome(){
        esc("[H");
        return self();
    }
    default T inv(){
        return escBrace("7m");
    }
    default T reset(){
        return escBrace("0m");
    }

    default T inv(Consumer<T> c){
        inv();
        c.accept(self());
        return reset();
    }
    default T nextLine(){
        escBrace("K\r\n");
        return self();
    }

   default T line(String prefix,String s){
        return inv(_->write(prefix)).write(" ").write(s).nextLine();
   }

    class ANSIBuilder implements ANSI<ANSIBuilder>{
         final StringBuilder stringBuilder;
         public ANSIBuilder(StringBuilder stringBuilder) {
             this.stringBuilder = stringBuilder;
         }

         @Override
         public ANSIBuilder write(String s) {
              stringBuilder.append(s);
              return this;
         }
         @Override public String toString(){
             return stringBuilder.toString();
         }

     }

     static ANSIBuilder of(StringBuilder stringBuilder){
        return new ANSIBuilder(stringBuilder);
     }
    default T repeat(String s, int count) {
        return write(s.repeat(count));
    }

     default T fill(int cols, String s) {
         return write(s).repeat(" ",Math.max(0, cols - s.length()));
     }
    // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797

}
