package editor;

interface XY<T extends XY<T>> {
    int x();

    int y();
    default T self(){
        return (T) this;
    }
    T moveTo(int x, int y);

    default T right() {
        return moveTo(x() + 1, y());
    }

    default T left() {
        if (x()>0){
            return moveTo(x() - 1, y());
        }
        return self();
    }

    default T up() {
         if (y()>0) {
             return moveTo(x(), y() - 1);
         }
         return self();
    }

    default T down() {
        return moveTo(x(), y() + 1);
    }

    default T x(int x) {
        return moveTo(x, y());
    }

    default T y(int y) {
        return moveTo(x(), y);
    }
}
