package src.utils;

import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    private static boolean isInputCaretVisible = true;

    private static final StringBuilder pageBuffer = new StringBuilder(8192);
    private static final StringBuilder finalBuffer = new StringBuilder(8192);

    private static final String CURSOR_HOME = "\033[H";
    private static final String CLEAR_LINE = "\033[K";
    private static final String CLEAR_TO_END_OF_SCREEN = "\033[0J";

    public static void refresh() {
        pageBuffer.setLength(0);
        finalBuffer.setLength(0);

        Router.getPage().render(pageBuffer);

        finalBuffer.append(CURSOR_HOME);

        for (int i = 0; i < pageBuffer.length(); i++) {
            char c = pageBuffer.charAt(i);
            if (c == '\n') {
                finalBuffer.append(CLEAR_LINE);
            }
            finalBuffer.append(c);
        }

        if (pageBuffer.length() > 0 && pageBuffer.charAt(pageBuffer.length() - 1) != '\n') {
            finalBuffer.append(CLEAR_LINE);
        }

        finalBuffer.append(CLEAR_TO_END_OF_SCREEN); 

        Global.getTerminal().writer().print(finalBuffer.toString());
        Router.getPage().updateCaret();
        Global.getTerminal().flush();
    }

    public static void hideInputCaret() {
        if (isInputCaretVisible) {
            Global.getTerminal().puts(Capability.cursor_invisible);
            isInputCaretVisible = false;
        }
    }

    public static void showInputCaret() {
        if (!isInputCaretVisible) {
            Global.getTerminal().puts(Capability.cursor_visible);
            isInputCaretVisible = true;
        }
    }
}
/*
package src.utils;
import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    private static boolean isInputCaretVisible = true;

    public static void refresh()
    {
        StringBuilder mainFrame = new StringBuilder();

        Router.getPage().render(mainFrame);
        Global.getTerminal().puts(Capability.clear_screen);
        Global.getTerminal().writer().print(mainFrame.toString());
        Router.getPage().updateCaret();
        Global.getTerminal().flush();
    }

    public static void hideInputCaret()
    {
        if (isInputCaretVisible)
        {
            Global.getTerminal().puts(Capability.cursor_invisible);
            isInputCaretVisible = false;
        }
    }

    public static void showInputCaret()
    {
        if (!isInputCaretVisible)
        {
            Global.getTerminal().puts(Capability.cursor_visible);
            isInputCaretVisible = true;
        }
    }
}
*/