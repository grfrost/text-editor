package editor;

class Viewport {
    private int x;
    private int y;
    public final int width;
    public final int height;

    public Viewport(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    int x(){
        return x;
    }
    int y(){
        return y;
    }

    public void track(Cursor cursor) {
        if (cursor.y() >= (y+height)) {
            y = cursor.y() - height-1;
            if (y<0){
                y=0;
            }
        } else if (cursor.y() < y) {
            y= cursor.y();
        }

        if (cursor.x() >= (x+width)) {
            x=cursor.x() - width-1;
            if (x<0){
                x=0;
            }
        } else if (cursor.x() < x) {
            x=cursor.x();
        }
    }

    public String clip(String text) {
        int len = text.length();
        if (len <x) {
            return "<";
        }else if ((len-x)>=width) {
            return text.substring(x, x+width);
        }else  {
            return text.substring(x);
        }
    }
}
