package src;
import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;

public class Input {
    public static KeyMap<String> keyMap = new KeyMap<>();
    public static BindingReader reader;

    public static void setup ()
    {
        reader = new BindingReader(Global.terminal.reader());

        keyMap.setAmbiguousTimeout(10L);

        Input.keyMap.bind("UP", "\u001B[A", "\u001BOA");
        Input.keyMap.bind("DOWN", "\u001B[B", "\u001BOB");
        Input.keyMap.bind("RIGHT", "\u001B[C", "\u001BOC");
        Input.keyMap.bind("LEFT", "\u001B[D", "\u001BOD");
        Input.keyMap.bind("SELECT", "\r", "\n");
        Input.keyMap.bind("BACK", "\u001B");
    }

    public static void listen()
    {
        while (true)
        {
            String action = reader.readBinding(keyMap);

            if (action != null)
            {
                Route.getPage().handleAction(action);
                // Audio.play("squeak");
            }
        }
    }
}
