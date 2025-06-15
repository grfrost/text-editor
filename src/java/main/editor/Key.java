package editor;

import editor.terminal.Terminal;

import java.util.HashMap;
import java.util.Map;

public record Key(int v) {
    static Map<Integer, Key> cache = new HashMap<>();
    static Key of(int v) {

        return cache.computeIfAbsent(v, k -> new Key(v));
    }
    static private Key ctrl(int v) {
        return Key.of(v & 0x1f);
    }
    static final Key ARROW_UP = Key.of(1000);
    static final Key ARROW_DOWN = Key.of(1001);
    static final Key ARROW_LEFT = Key.of(1002);
    static final Key ARROW_RIGHT = Key.of(1003);
    static final Key HOME = Key.of(1004);
    static final Key END = Key.of(1005);
    static final Key PAGE_UP = Key.of(1006);
    static final Key PAGE_DOWN = Key.of(1007);
    static final Key DEL = Key.of(1008);
    static final Key ESC = Key.of('\033');
    static final Key NL = Key.of(13);
    static final Key CtrlQ = Key.ctrl('q');
    static final Key CtrlF = Key.ctrl('f');
    static final Key BACKSPACE = Key.of(0x7f);
    public static final Key CtrlS = ctrl('s') ;
    public static final Key CtrlH = ctrl('h');
    public static final Key OSBRACE = Key.of('[');
    public static final Key _O = Key.of('O');

    static Key readAndMap(Terminal terminal){
            var keyCh = terminal.read();
            var key = Key.of(keyCh);
            if (key !=  Key.ESC) {
                return key;
            }
        var nextKeyCh= terminal.read();
            var nextKey = Key.of(nextKeyCh);
            if (nextKey != OSBRACE && nextKey != _O) {
                return nextKey;
            }
         var yetAnotherKeyCh = terminal.read();


            if (nextKey == OSBRACE) {
                return switch (yetAnotherKeyCh) {
                    case 'A' -> Key.ARROW_UP;  // e.g. esc[A == arrow_up
                    case 'B' -> Key.ARROW_DOWN;
                    case 'C' -> Key.ARROW_RIGHT;
                    case 'D' -> Key.ARROW_LEFT;
                    case 'H' -> Key.HOME;
                    case 'F' -> Key.END;
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
                        int yetYetAnotherCh = terminal.read();
                        if (yetYetAnotherCh != '~') {
                            yield Key.of(yetYetAnotherCh);
                        }
                        switch (yetAnotherKeyCh) {
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
                                yield Key.of(yetAnotherKeyCh);
                        }
                    }
                    default -> Key.of(yetAnotherKeyCh);
                };
            } else {
                return switch (yetAnotherKeyCh) {
                    case 'H' -> Key.HOME;
                    case 'F' -> Key.END;
                    default -> Key.of(yetAnotherKeyCh);
                };
            }
        }

    public boolean deletesCharBefore() {
         return this == Key.DEL || this == Key.CtrlH || this == Key.BACKSPACE;
    }

    public boolean clearsStatus() {
        return this  == Key.ESC || this == Key.NL;
    }

    public boolean canBeInserted() {
        return !Character.isISOControl(v()) && v() < 128;
    }
}



