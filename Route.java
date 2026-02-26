import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Route {
    private static Page page = new Menu();
    private static List<Page> previousPages = new ArrayList<>(Arrays.asList(page));

    public static void redirect(Page p)
    {
        page = p;
        previousPages.add(p);
    }

    public static void previous()
    {
        previousPages.removeLast();
        page = previousPages.getLast();
    }

    public static Page getPage()
    {
        return page;
    }
}
