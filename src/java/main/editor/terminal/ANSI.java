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

     default T fill(String s, int cols) {
         return write(s).repeat(" ",Math.max(0, cols - s.length()));
     }
    // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797

}
