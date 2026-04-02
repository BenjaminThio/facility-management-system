package src.utils;
import org.jline.utils.InfoCmp.Capability;

public class Renderer {
    public static void refresh()
    {
        StringBuilder mainFrame = new StringBuilder();

        Router.getPage().render(mainFrame);
        Global.getTerminal().puts(Capability.clear_screen);
        Global.getTerminal().writer().print(mainFrame.toString());
        Router.getPage().updateCaret();
        Global.getTerminal().flush();
    }
}
