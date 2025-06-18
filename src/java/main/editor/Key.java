package editor;


public record Key(int v) {
    static Key[] cache = new Key[0x200];

    static Key of(int v) {
        if (cache[v] == null) {
            cache[v] = new Key(v);
        }
        return cache[v];
    }
    static private Key ctrl(int v) {
        return Key.of(v & 0x1f);
    }
    static final Key ARROW_UP = Key.of(0x100+0);
    static final Key ARROW_DOWN = Key.of(0x100+1);
    static final Key ARROW_LEFT = Key.of(0x100+2);
    static final Key ARROW_RIGHT = Key.of(0x100+3);
    static final Key HOME = Key.of(0x100+4);
    static final Key END = Key.of(0x100+5);
    static final Key PAGE_UP = Key.of(0x100+6);
    static final Key PAGE_DOWN = Key.of(0x100+7);
    static final Key DEL = Key.of(0x100+8);
    static final Key ESC = Key.of('\033');
    static final Key NL = Key.of(13);
    static final Key CtrlQ = Key.ctrl('q');
    static final Key CtrlF = Key.ctrl('f');
    static final Key BACKSPACE = Key.of(0x7f);
    public static final Key CtrlS = ctrl('s') ;
    public static final Key CtrlH = ctrl('h');
    public static final Key OSBRACE = Key.of('[');
    public static final Key _O = Key.of('O');

    static Key readAndMap(ANSITerminal terminal){
            var keyCh = terminal.get();
            var key = Key.of(keyCh);
            if (key !=  Key.ESC) {
                return key;
            }
        var nextKeyCh= terminal.get();
            var nextKey = Key.of(nextKeyCh);
            if (nextKey != OSBRACE && nextKey != _O) {
                return nextKey;
            }
         var yetAnotherKeyCh = terminal.get();


            if (nextKey == OSBRACE) {
                return switch (yetAnotherKeyCh.intValue()) {
                    case 'A' -> Key.ARROW_UP;  // e.g. esc[A == arrow_up
                    case 'B' -> Key.ARROW_DOWN;
                    case 'C' -> Key.ARROW_RIGHT;
                    case 'D' -> Key.ARROW_LEFT;
                    case 'H' -> Key.HOME;
                    case 'F' -> Key.END;
                    case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
                        int yetYetAnotherCh = terminal.get();
                        if (yetYetAnotherCh != '~') {
                            yield Key.of(yetYetAnotherCh);
                        }
                        switch (yetAnotherKeyCh.intValue()) {
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
                return switch (yetAnotherKeyCh.intValue()) {
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



