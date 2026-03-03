package src.components;

import java.util.ArrayList;
import java.util.List;

import org.jline.utils.InfoCmp.Capability;

import src.models.Position;
import src.utils.Global;
import src.utils.Renderer;

public class MemoField {
    // Helper class to strictly track where text belongs on screen
    private class VisualLine {
        String text;
        int startIdx; // Absolute index where this visual line starts
        int endIdx;   // Absolute index where this visual line ends
        // int skip;     // Characters skipped after this line (e.g., 1 for space or \n, 0 for hard wrap)

        public VisualLine(String text, int startIdx, int endIdx, int skip) {
            this.text = text;
            this.startIdx = startIdx;
            this.endIdx = endIdx;
            // this.skip = skip;
        }
    }

    private StringBuilder text = new StringBuilder();
    private int absoluteCaret = 0;

    private List<VisualLine> visualLines = new ArrayList<>();
    private int caretVisualX = 0;
    private int caretVisualY = 0;
    private int scrollOffsetY = 0;

    // private int minHeight = 10;
    private int length = 20;
    private int height = 10;
    private String placeholder = "";
    private int backgroundColor = Ansi.BG_WHITE;
    private int textColor = Ansi.FG_BLACK;
    private int placeholderColor = Ansi.FG_DARK_GRAY;

    public MemoField(int minHeight, int length, String placeholder, boolean overflowX) {
        // this.minHeight = minHeight;
        this.length = length;
        this.height = minHeight;
        this.placeholder = placeholder;
        reflow(); 
    }

    private void reflow() {
        visualLines.clear();
        String raw = text.toString();
        int start = 0;

        // Break text into Visual Lines, accounting for word wraps and explicit \n
        while (start < raw.length()) {
            int end = Math.min(start + length, raw.length());
            int newlineIdx = raw.substring(start, end).indexOf('\n');
            
            int lineEndStr;
            int skip;

            if (newlineIdx != -1) {
                // Wrap exactly at the newline
                lineEndStr = start + newlineIdx;
                skip = 1; 
            } else if (end == raw.length()) {
                // End of the string
                lineEndStr = end;
                skip = 0;
            } else {
                // Word wrapping logic
                char nextChar = raw.charAt(end);
                if (nextChar == ' ' || nextChar == '\n') {
                    lineEndStr = end;
                    skip = 1;
                } else {
                    int lastSpace = raw.substring(start, end).lastIndexOf(' ');
                    if (lastSpace != -1) {
                        lineEndStr = start + lastSpace;
                        skip = 1;
                    } else {
                        // Hard wrap if the word is longer than terminal width
                        lineEndStr = end;
                        skip = 0;
                    }
                }
            }

            visualLines.add(new VisualLine(raw.substring(start, lineEndStr), start, lineEndStr, skip));
            start = lineEndStr + skip;
        }

        // If text is empty or ends with a newline, guarantee an empty visual line at the end
        if (raw.isEmpty() || (raw.length() > 0 && raw.charAt(raw.length() - 1) == '\n')) {
            visualLines.add(new VisualLine("", raw.length(), raw.length(), 0));
        }

        updateCaretVisualPosition();
    }

    private void updateCaretVisualPosition() {
        if (visualLines.isEmpty()) {
            caretVisualX = 0;
            caretVisualY = 0;
            return;
        }

        // Find exactly which line the absolute caret falls into
        for (int i = 0; i < visualLines.size(); i++) {
            VisualLine vl = visualLines.get(i);
            
            // Because our start/end logic has no gaps, the caret WILL match one of these lines
            if (absoluteCaret >= vl.startIdx && absoluteCaret <= vl.endIdx) {
                caretVisualY = i;
                caretVisualX = absoluteCaret - vl.startIdx;
                return;
            }
        }
    }

    public void handleInput(String input) {
        switch (input) {
            case "LEFT":
                if (absoluteCaret > 0) absoluteCaret--;
                break;
            case "RIGHT":
                if (absoluteCaret < text.length()) absoluteCaret++;
                break;
            case "UP":
                if (caretVisualY > 0) {
                    VisualLine prevLine = visualLines.get(caretVisualY - 1);
                    int targetX = Math.min(caretVisualX, prevLine.text.length());
                    absoluteCaret = prevLine.startIdx + targetX;
                }
                break;
            case "DOWN":
                if (caretVisualY < visualLines.size() - 1) {
                    VisualLine nextLine = visualLines.get(caretVisualY + 1);
                    int targetX = Math.min(caretVisualX, nextLine.text.length());
                    absoluteCaret = nextLine.startIdx + targetX;
                }
                break;
            case "BACKSPACE":
                if (absoluteCaret > 0) {
                    text.deleteCharAt(absoluteCaret - 1);
                    absoluteCaret--;
                }
                break;
            case "ENTER": // Enter key
                text.insert(absoluteCaret, '\n');
                absoluteCaret++;
                break;
            default:
                switch (input.length())
                {
                    case 1:
                        text.insert(absoluteCaret, input);
                        absoluteCaret += input.length();
                        break;
                }
                break;
        }

        reflow(); 

        // Keep the caret visually in frame
        if (caretVisualY < scrollOffsetY) {
            scrollOffsetY = caretVisualY;
        } else if (caretVisualY >= scrollOffsetY + height) {
            scrollOffsetY = caretVisualY - height + 1;
        }

        Renderer.refresh();
    }

    public void render() {
        for (int i = 0; i < height; i++) {
            int visualIndex = scrollOffsetY + i;

            if (visualIndex < visualLines.size()) {
                String line = visualLines.get(visualIndex).text;
                
                if (text.length() == 0 && !placeholder.isEmpty() && i == 0) {
                    String displayPlaceholder = placeholder.length() <= length ? placeholder : placeholder.substring(0, length - 1) + "…";
                    System.out.print(new Ansi(displayPlaceholder + " ".repeat(length - displayPlaceholder.length()), backgroundColor, placeholderColor).toString());
                } else {
                    System.out.print(new Ansi(line + " ".repeat(length - line.length()), backgroundColor, textColor).toString());
                }
            } else {
                System.out.print(new Ansi(" ".repeat(length), backgroundColor).toString());
            }
            System.out.println();
        }
    }

    public void updateCaret(int offsetX, int offsetY)
    {
        Global.terminal.puts(Capability.cursor_address, offsetY + caretVisualY - scrollOffsetY, offsetX + caretVisualX);
    }

    public void updateCaret(Position offset)
    {
        updateCaret(offset.x, offset.y);
    }
}