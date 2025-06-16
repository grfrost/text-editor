package editor;

class Cursor  {


    private int x;
    private int y;
    Cursor( int x, int y){

        this.x = x;
        this.y = y;
    }

    public void xy(int x, int y) {
       this.x = x;
       this.y = y;
    }


    public int y() {
        return y;
    }

    public int x() {
        return x;
    }
    public void x(int x) {
       this.x = x;
    }

    public void y(int y) {
        this.y = y;
    }

    public void right() {
        x++;
    }

    public void left() {
        if (x>0){
           x--;
        }
    }

    public void up(int n) {
        if ((y-n)>0) {
             y-=n;
        }
    }

    public void down(int n) {
        y+=n;
    }

    public void startOfNextLine() {
        down(1);
        x(0);
    }
}
