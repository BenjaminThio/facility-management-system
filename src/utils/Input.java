package src.utils;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;

public class Input {
    public static KeyMap<String> keyMap = new KeyMap<>();
    public static BindingReader reader;

    public static void setup()
    {
        reader = new BindingReader(Global.getTerminal().reader());

        keyMap.setAmbiguousTimeout(10L);

        keyMap.bind("UP", "\u001B[A", "\u001BOA");
        keyMap.bind("DOWN", "\u001B[B", "\u001BOB");
        keyMap.bind("RIGHT", "\u001B[C", "\u001BOC");
        keyMap.bind("LEFT", "\u001B[D", "\u001BOD");
        keyMap.bind("ENTER", "\r", "\n");
        keyMap.bind("ESC", "\u001B");
        keyMap.bind("BACKSPACE", "\b");
        keyMap.bind("TAB", "\t");
        keyMap.bind("SHIFT_TAB", "\u001B[Z");

        for (char c = 32; c <= 126; c++)
        {
            keyMap.bind(String.valueOf(c), String.valueOf(c));
        }
    }

    public static void listen()
    {
        while (true)
        {
            String action = reader.readBinding(keyMap);

            if (action != null)
            {
                Router.getPage().handleAction(action);
                // Audio.play("squeak");
            }
        }
    }
}
