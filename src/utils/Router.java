package src.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import src.pages.MenuPage;
import src.pages.cores.Page;

public class Router {
    private static Page page = new MenuPage();
    private static List<Page> routesHistory = new ArrayList<>(Arrays.asList(page));

    public static void redirect(Page p)
    {
        page = p;
        routesHistory.add(p);
        Renderer.refresh();
    }

    public static void back()
    {
        routesHistory.removeLast();
        page = routesHistory.getLast();
        Renderer.refresh();
    }

    public static Page getPage()
    {
        return page;
    }

    public static List<Page> getRoutesHistory()
    {
        return routesHistory;
    }

    public static void clear()
    {
        Page MenuPage = new MenuPage();

        routesHistory.clear();
        page = MenuPage;
        routesHistory.add(page);
        Renderer.refresh();
    }
}
