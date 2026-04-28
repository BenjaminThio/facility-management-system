package src.components.fields;

import java.util.ArrayList;
import java.util.List;

import org.jline.utils.InfoCmp.Capability;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.fields.core.Field;
import src.models.Position;
import src.utils.Global;
import src.utils.Renderer;

public class MemoField extends Field {
    // Helper class to strictly track where text belongs on screen
    private class VisualLine {
        String text;
        int startIdx; // Absolute index where this visual line starts
        int endIdx;   // Absolute index where this visual line ends

        public VisualLine(String text, int startIdx, int endIdx, int skip) {
            this.text = text;
            this.startIdx = startIdx;
            this.endIdx = endIdx;
        }
    }

    private StringBuilder text = new StringBuilder();
    private int absoluteCaret = 0;

    private List<VisualLine> visualLines = new ArrayList<>();
    private int caretVisualX = 0;
    private int caretVisualY = 0;
    private int scrollOffsetY = 0;

    private int height = 10;
    private boolean scrollable = false;
    
    // ADDED: Boundary tracking variable
    private boolean outOfRange = false;

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
        reflow();
    }

    public boolean isScrollable() {
        return scrollable;
    }

    public boolean isOutOfRange() {
        return outOfRange;
    }

    public MemoField copy()
    {
        MemoField clone = new MemoField();

        clone.length = this.length;
        clone.placeholder = this.placeholder;
        clone.backgroundColor = this.backgroundColor;
        clone.textColor = this.textColor;
        clone.placeholderColor = this.placeholderColor;
        clone.error = this.error;

        clone.text = new StringBuilder(this.text);
        clone.absoluteCaret = this.absoluteCaret;
        clone.visualLines = new ArrayList<>(this.visualLines);
        clone.caretVisualX = this.caretVisualX;
        clone.caretVisualY = this.caretVisualY;
        clone.scrollOffsetY = this.scrollOffsetY;
        clone.height = this.height;
        clone.scrollable = this.scrollable; 
        clone.outOfRange = this.outOfRange; // ADDED: copy outOfRange state

        return clone;
    }

    private MemoField() {}

    public MemoField(String text, int minHeight, int length, String placeholder, boolean overflowX) {
        this.text = new StringBuilder(text);
        this.length = length;
        this.height = minHeight;
        this.placeholder = placeholder;
        reflow(); 
    }

    public MemoField(int minHeight, int length, String placeholder, boolean overflowX) {
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

    @Override
    public void handleInput(String input) {
        // ADDED: Reset the flag on every new keystroke
        outOfRange = false;

        // Snapshot the state before applying changes
        String previousText = text.toString();
        int previousCaret = absoluteCaret;

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
                } else {
                    // ADDED: Hit the top boundary
                    outOfRange = true;
                }
                break;
            case "DOWN":
                if (caretVisualY < visualLines.size() - 1) {
                    VisualLine nextLine = visualLines.get(caretVisualY + 1);
                    int targetX = Math.min(caretVisualX, nextLine.text.length());
                    absoluteCaret = nextLine.startIdx + targetX;
                } else {
                    // ADDED: Hit the bottom boundary
                    outOfRange = true;
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

        // Revert changes if scrolling is disabled and the new lines exceed max height
        if (!scrollable && visualLines.size() > height) {
            text = new StringBuilder(previousText);
            absoluteCaret = previousCaret;
            reflow();
            return; // Exit early, skipping the render refresh
        }

        // Keep the caret visually in frame based on scroll setting
        if (scrollable) {
            if (caretVisualY < scrollOffsetY) {
                scrollOffsetY = caretVisualY;
            } else if (caretVisualY >= scrollOffsetY + height) {
                scrollOffsetY = caretVisualY - height + 1;
            }
        } else {
            scrollOffsetY = 0;
        }

        Renderer.refresh();
    }

    @Override
    public void render(StringBuilder frame) {
        for (int i = 0; i < height; i++) {
            int visualIndex = scrollOffsetY + i;

            if (visualIndex < visualLines.size()) {
                String line = visualLines.get(visualIndex).text;
                
                if (text.length() == 0 && !placeholder.isEmpty() && i == 0) {
                    String displayPlaceholder = placeholder.length() <= length ? placeholder : placeholder.substring(0, length - 1) + "…";
                    frame.append(new Ansi(displayPlaceholder + " ".repeat(length - displayPlaceholder.length()), backgroundColor, placeholderColor).toString());
                } else {
                    frame.append(new Ansi(line + " ".repeat(length - line.length()), backgroundColor, textColor).toString());
                }
            } else {
                frame.append(new Ansi(" ".repeat(length), backgroundColor).toString());
            }
            frame.append('\n');
        }
    }

    @Override
    public AnsiBuilder toAnsiBuilder()
    {
        AnsiBuilder ansiBuilder = new AnsiBuilder();

        for (int i = 0; i < height; i++) {
            int visualIndex = scrollOffsetY + i;

            if (visualIndex < visualLines.size()) {
                String line = visualLines.get(visualIndex).text;
                
                if (text.length() == 0 && !placeholder.isEmpty() && i == 0) {
                    String displayPlaceholder = placeholder.length() <= length ? placeholder : placeholder.substring(0, length - 1) + "…";
                    ansiBuilder.append(new Ansi(displayPlaceholder + " ".repeat(length - displayPlaceholder.length()), backgroundColor, placeholderColor));
                } else {
                    ansiBuilder.append(new Ansi(line + " ".repeat(length - line.length()), backgroundColor, textColor));
                }
            } else {
                ansiBuilder.append(new Ansi(" ".repeat(length), backgroundColor));
            }
            ansiBuilder.append('\n');
        }

        return ansiBuilder;
    }

    @Override
    public void updateCaret(int offsetX, int offsetY)
    {
        Global.getTerminal().puts(Capability.cursor_address, offsetY + caretVisualY - scrollOffsetY, offsetX + caretVisualX);
    }

    @Override
    public void updateCaret(Position offset)
    {
        updateCaret(offset.x, offset.y);
    }

    @Override
    public String getValue()
    {
        return text.toString().trim();
    }
}