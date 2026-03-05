package src.utils;
import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    public static void refresh()
    {
        Global.getTerminal().puts(Capability.clear_screen);
        Global.getTerminal().flush();
        Router.getPage().render();
    }
}
