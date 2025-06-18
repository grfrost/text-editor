package editor;

public record NewKey(int v) {
    static NewKey[] cache = new NewKey[512];

    static NewKey of(int v) {
        if (cache[v] == null) {
            cache[v] = new NewKey(v);
        }
        return cache[v];
    }

    static final int CTRL_MASK = 0x1f;

    static private NewKey ctrl(int v) {
        return NewKey.of(v & CTRL_MASK);
    }

    static final int MOTION = 0x100;
    static final int UP = MOTION | 0x1;
    static final int DOWN = MOTION | 0x2;
    static final int LEFT = MOTION | 0x4;
    static final int RIGHT = MOTION | 0x8;
    static final int PAGE = MOTION | 0x10;
    static final int CH = MOTION | 0x20;
    static final int EDIT = MOTION | CH | 0x40;
    static final NewKey ARROW_UP = NewKey.of(UP | CH);
    static final NewKey ARROW_DOWN = NewKey.of(DOWN | CH);
    static final NewKey ARROW_LEFT = NewKey.of(LEFT | CH);
    static final NewKey ARROW_RIGHT = NewKey.of(RIGHT | CH);
    static final NewKey HOME = NewKey.of(LEFT | PAGE);
    static final NewKey END = NewKey.of(RIGHT | PAGE);
    static final NewKey PAGE_UP = NewKey.of(UP | PAGE);
    static final NewKey PAGE_DOWN = NewKey.of(DOWN | PAGE);
    static final NewKey DEL = NewKey.of(EDIT | LEFT);
    static final NewKey ESC = NewKey.of('\033');
    static final NewKey NL = NewKey.of('\n');
    static final NewKey CtrlQ = NewKey.ctrl('q');
    static final NewKey CtrlF = NewKey.ctrl('f');
    static final NewKey BACKSPACE = NewKey.of(0x7f);
    public static final NewKey CtrlS = ctrl('s');
    public static final NewKey CtrlH = ctrl('h');
    public static final NewKey OSBRACE = NewKey.of('[');
    public static final NewKey _O = NewKey.of('O');

    static NewKey readAndMap(ANSITerminal terminal) {
        var keyCh = terminal.get();
        var key = NewKey.of(keyCh);
        if (key != NewKey.ESC) {
            return key;
        }
        var nextKeyCh = terminal.get();
        var nextKey = NewKey.of(nextKeyCh);
        if (nextKey != OSBRACE && nextKey != _O) {
            return nextKey;
        }
        var yetAnotherKeyCh = terminal.get();


        if (nextKey == OSBRACE) {
            return switch (yetAnotherKeyCh.intValue()) {
                case 'A' -> NewKey.ARROW_UP;  // e.g. esc[A == arrow_up
                case 'B' -> NewKey.ARROW_DOWN;
                case 'C' -> NewKey.ARROW_RIGHT;
                case 'D' -> NewKey.ARROW_LEFT;
                case 'H' -> NewKey.HOME;
                case 'F' -> NewKey.END;
                case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {  // e.g: esc[5~ == page_up
                    int yetYetAnotherCh = terminal.get();
                    if (yetYetAnotherCh != '~') {
                        yield NewKey.of(yetYetAnotherCh);
                    }
                    switch (yetAnotherKeyCh.intValue()) {
                        case '1':
                        case '7':
                            yield NewKey.HOME;
                        case '3':
                            yield NewKey.DEL;
                        case '4':
                        case '8':
                            yield NewKey.END;
                        case '5':
                            yield NewKey.PAGE_UP;
                        case '6':
                            yield NewKey.PAGE_DOWN;
                        default:
                            yield NewKey.of(yetAnotherKeyCh);
                    }
                }
                default -> NewKey.of(yetAnotherKeyCh);
            };
        } else {
            return switch (yetAnotherKeyCh.intValue()) {
                case 'H' -> NewKey.HOME;
                case 'F' -> NewKey.END;
                default -> NewKey.of(yetAnotherKeyCh);
            };
        }
    }
}



