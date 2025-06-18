package editor;

import java.io.IOException;
import java.nio.file.Path;

public class Editor {

    public static void main(String[] args) throws IOException {

        ANSITerminal terminal = new ANSITerminal().raw();
        Path path = Path.of(args[0]);
        if (Content.of(path) instanceof Content content) {

            Cursor<Content> cursor = Cursor.of(content);
            int leftmargin = 12;// Line # + line len
            int statusHeight = 1;

            class State {
                private boolean finished = false;
                private Viewport viewport;
                private String status;

                State() {
                    viewport = Viewport.of(terminal.maxRow() - (statusHeight-2), terminal.maxCol() - leftmargin);
                    status = "OK";
                    finished = false;
                }

                void status(String message) {
                    status = message;
                }

                void stopEditing() {
                    finished = true;
                }

                boolean finished() {
                    return finished;
                }

                String message() {
                    return status;
                }

                void track(Cursor cursor) {
                    viewport= viewport.track(cursor);
                }

            }
            State state = new State();

            while (!state.finished()) {
                state.track(cursor);
                terminal.cursorHome().hideCursor();
                for (int row = state.viewport.minRow(); row < state.viewport.maxRow(); row++) {
                    if (content.lineAt(row) instanceof Content.Line line) {
                        terminal.line(
                                String.format("%-" + (leftmargin - 2) + "s",
                                        String.format("%4d %4d", row, line.maxCol())), state.viewport.clip(line.text())
                        );
                    } else {
                        terminal.line("...........", "~");
                    }
                }
                terminal.inv(_ ->
                        terminal.fill(terminal.maxCol(), terminal.maxDims()
                              //  + " Lines:" + content.maxRow()
                                + " View " + state.viewport.dims()
                                + " Curs " + cursor.rowColPos()
                              //  + " Term " + terminal.rowColPos()
                                + " Stat " + state.message()
                                + " T.C " + terminal.getCursorLocation().rowColPos()
                              //  + " T.M " + terminal.getMouseLocation().rowColPos()
                        )
               );

                terminal.rowCol(state.viewport.rowOffset(cursor)+1, state.viewport.colOffset(cursor) + leftmargin);
                terminal.flush().showCursor();

                Key key = Key.readAndMap(terminal);

                if (key == Key.CtrlQ) {
                    state.stopEditing();

                } else if (key == Key.CtrlF) {
                    state.status("Find not implemented");
                    //editorFind();
                } else if (key == Key.CtrlS) {
                    state.status("Save not implemented");
                    //  if (content.save(path)) {
                    //setStatusMessage("Successfully saved file");
                    //  } else {
                    // setStatusMessage("There was an error saving your file ");
                    //   }
                } else if (content.line(cursor) instanceof Content.Line line) {
                    if (key == Key.NL) {
                        if (cursor.onFirstCol(line)) {
                            content.insertEmptyLine(cursor);
                            cursor.down(1);
                            state.status("INS NL COL1");
                            // Add an empty line above
                        } else if (cursor.onLastCol(line)) {
                            cursor.down(1);
                            content.insertEmptyLine(cursor);
                            state.status("INS NL LINE END");
                            // Add an empty line below
                        } else {
                            String text = line.text();
                            line.replace(text.substring(0, cursor.col()));
                            Content.Line right = content.createLine(text.substring(cursor.col()));
                            content.insertRowAt(cursor.row() + 1, right);
                            state.status("INS NL mid line");
                            cursor.down(1);
                        }
                        cursor.col(0);
                    } else if (key == Key.BACKSPACE || key == Key.CtrlH || key == Key.DEL) {
                        if (cursor.onFirstCol(line)) {
                            if (!cursor.onFirstRow(content)) {
                                var lineAbove = content.lineAbove(cursor);
                                var text = lineAbove.text();
                                text = text.substring(0, text.length() - 1);
                                text = text + line.text();
                                lineAbove.replace(text);
                                content.deleteLine(cursor);
                                cursor.up(1).col(text.length());
                                state.status("DEL CH and joined lines");
                            } else {
                                state.status("DEL CH ignored");
                            }
                        } else {
                            cursor.left();
                            if (line.containsCol(cursor)) {
                                line.deleteChar(cursor);
                            }
                            state.status("DEL CH in line");
                        }

                    } else if (key == Key.ARROW_UP && !cursor.onFirstRow(content)) {
                        cursor.up(1);
                        if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                            cursor.col(l.maxCol());
                        }
                        state.status("ARROW_UP");
                    } else if (key == Key.ARROW_DOWN && !cursor.onLastRow(content)) {
                        cursor.down(1);
                        if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                            cursor.col(l.maxCol());
                        }
                        state.status("ARROW_DOWN");
                    } else if (key == Key.ARROW_LEFT && !cursor.onFirstCol(line)) {
                        cursor.left();
                        state.status("<--");
                    } else if (key == Key.ARROW_RIGHT && !cursor.onLastCol(line)) {
                        cursor.right();
                        state.status("-->");
                    } else if (key == Key.PAGE_UP) {
                        for (int i = 0; !cursor.onFirstRow(content) && i < state.viewport.height(); i++) {
                            cursor.up(1);
                        }
                        if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                            cursor.col(l.maxCol());
                        }
                    } else if (key == Key.PAGE_DOWN) {
                        for (int i = 0; !cursor.onLastRow(content) && i < state.viewport.height(); i++) {
                            cursor.down(1);
                        }
                        if (content.line(cursor) instanceof Content.Line l && cursor.isRightOf(l)) {
                            cursor.col(l.maxCol());
                        }
                    } else if (key == Key.HOME) {
                        cursor.col(0);
                    } else if (key == Key.END) {
                        cursor.col(line.maxCol());
                    } else if (line.containsCol(cursor)) {
                        line.insertChar(cursor, (char) key.v());
                        cursor.right();
                        state.status("inserted " + key.v());
                    } else {
                        state.status("UNHANDLED 1 " + key.v());
                    }
                } else {
                    state.status("UNHANDLED 2" + key.v());
                }
            }

            terminal.clearScreen().cursorHome().cooked().flush();
        }
    }
}