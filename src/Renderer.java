package src;
import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    public static void refresh()
    {
        Global.terminal.puts(Capability.clear_screen);
        Global.terminal.flush();
        Route.getPage().render();
    }
}
