import org.jline.terminal.Terminal;
import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    public static void clearScreen(Terminal terminal)
    {
        terminal.puts(Capability.clear_screen);
        terminal.flush();
        Route.getPage().render();
    }
}
