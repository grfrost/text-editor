package editor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public record Key(int v, String name, Key.Type type) {
    enum Type{
        NORMAL, CTRL, CURSOR_MOVE,ACTION
    }
    static Map<Integer, Key> cache = new HashMap<>();
    static Key of(int v, String name, Type type) {
        return cache.computeIfAbsent(v, k -> new Key(v, name, type));
    }
    static Key of(int v) {
        return cache.computeIfAbsent(v, k -> new Key(v, "\"" + ((char)v)+"\"", Type.NORMAL));
    }
    static private Key ctrl(int v) {
        return Key.of(v & 0x1f, "CTRL<"+((char)v)+">", Type.CTRL);
    }
    static final Key ARROW_UP = Key.of(1000, "ARROW_UP", Type.CURSOR_MOVE);
    static final Key ARROW_DOWN = Key.of(1001, "ARROW_DOWN",Type.CURSOR_MOVE);
    static final Key ARROW_LEFT = Key.of(1002, "ARROW_LEFT",Type.CURSOR_MOVE);
    static final Key ARROW_RIGHT = Key.of(1003,  "ARROW_RIGHT",Type.CURSOR_MOVE);
    static final Key HOME = Key.of(1004,  "HOME",Type.CURSOR_MOVE);
    static final Key END = Key.of(1005, "END",Type.CURSOR_MOVE);
    static final Key PAGE_UP = Key.of(1006,  "PAGE_UP",Type.CURSOR_MOVE);
    static final Key PAGE_DOWN = Key.of(1007,  "PAGE_DOWN",Type.CURSOR_MOVE);
    static final Key DEL = Key.of(1008,  "DEL",Type.ACTION);
    static final Key ESC = Key.of('\033', "ESC", Type.ACTION);
    static final Key NL = Key.of(13,"NL",Type.ACTION);
    static final Key CtrlQ = Key.ctrl('q');
    static final Key CtrlF = Key.ctrl('f');
    static final Key BACKSPACE = Key.of(127, "BACKSPACE", Type.ACTION);
    public static final Key CtrlS = ctrl('s') ;
    public static final Key CtrlH = ctrl('h');
    public static final Key OSBRACE = Key.of('[');
    public static final Key _O = Key.of('O');
    public static Key read() {
        int i = -1;
        try {
            i = System.in.read();
        }catch(IOException e) {

        }
        return Key.of(i);
    }
    static Key readAndMap(){
            var key = read();
            if (key !=  Key.ESC) {
                return key;
            }

            var nextKey = read();
            if (nextKey != OSBRACE && nextKey != _O) {
                return nextKey;
            }

            Key yetAnotherKey = read();

            if (nextKey == OSBRACE) {
                return switch (yetAnotherKey.v()) {
                    case 'A' -> Key.ARROW_UP;  // e.g. esc[A == arrow_up
                    case 'B' -> Key.ARROW_DOWN;
                    case 'C' -> Key.ARROW_RIGHT;
                    case 'D' -> Key.ARROW_LEFT;
                    case 'H' -> Key.HOME;
                    case 'F' -> Key.END;
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
                        Key yetYetAnotherChar = read();
                        if (yetYetAnotherChar != Key.of('~')) {
                            yield yetYetAnotherChar;
                        }
                        switch (yetAnotherKey.v()) {
                            case '1':
                            case '7':
                                yield Key.HOME;
                            case '3':
                                yield Key.DEL;
                            case '4':
                            case '8':
                                yield Key.END;
                            case '5':
                                yield Key.PAGE_UP;
                            case '6':
                                yield Key.PAGE_DOWN;
                            default:
                                yield yetAnotherKey;
                        }
                    }
                    default -> yetAnotherKey;
                };
            } else {
                return switch (yetAnotherKey.v()) {
                    case 'H' -> Key.HOME;
                    case 'F' -> Key.END;
                    default -> yetAnotherKey;
                };
            }
        }
    }



