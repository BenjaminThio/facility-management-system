package src.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import src.pages.MenuPage;
import src.pages.SignUpPage;
import src.pages.core.Page;

public class Router {
    private static Page page = new SignUpPage();
    private static List<Page> previousPages = new ArrayList<>(Arrays.asList(page));

    public static void redirect(Page p)
    {
        page = p;
        previousPages.add(p);
        Renderer.refresh();
    }

    public static void back()
    {
        previousPages.removeLast();
        page = previousPages.getLast();
        Renderer.refresh();
    }

    public static Page getPage()
    {
        return page;
    }
}
