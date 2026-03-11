package src.pages;

import java.util.Arrays;
import java.util.function.Function;

import src.components.Ansi;
import src.components.InputField;
import src.pages.cores.Subpage;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.SearchEngine;

public class ListPage extends Subpage {
    private static final int VISIBLE_NUMBER = 30;
    private InputField inputField = new InputField(20, "Type something...");
    private int selectionOffset = 0;
    private Subpage subpage = null;
    private String[] list;
    private String[] searchResult;
    private Function<Subpage, String[]> callback;

    /*
    public ListPage(Subpage subpage, List<?> m, Function<List<?>, String[]> callback)
    {
        this.subpage = subpage;
        this.list = callback.apply(m);
    }

    public ListPage(Subpage subpage, Map<String, ?> l, Function<Map<String, ?>, String[]> callback)
    {
        this.subpage = subpage;
        this.list = callback.apply(l);
    }
    */

    public ListPage(Subpage subpage, Function<Subpage, String[]> callback)
    {
        this.subpage = subpage;
        this.callback = callback;
    }

    public ListPage(Subpage subpage, String[] list)
    {
        this.subpage = subpage;
        this.list = list;
        this.searchResult = Arrays.copyOf(list, list.length);
    }

    public void render()
    {
        System.out.print("Search: ");

        inputField.setBackgroundColor(selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE);
        inputField.render();

        System.out.println();

        for (int i = selectionOffset; i < selectionOffset + (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER); i++)
        {
            if (selection + selectionOffset - 1 == i)
            {
                System.out.print("> ");
            }
            else
            {
                System.out.print("  ");
            }
            System.out.println(Integer.toString(i + 1) + ". " + searchResult[i]);
        }
        inputField.updateCaret("Search: ".length(), 0);
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP" -> {
                if (selection - 1 >= 1)
                {
                    selection--;
                }
                else if (selectionOffset - 1 >= 0)
                {
                    selectionOffset--;
                }
                else if (selection - 1 >= 0)
                {
                    selection--;
                }
            }
            case "DOWN" -> {
                if (selection + 1 <= (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER))
                {
                    selection++;
                }
                else if (selection + selectionOffset + 1 <= searchResult.length)
                {
                    selectionOffset++;
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "ENTER" -> {
                switch (selection)
                {
                    case 0 -> {
                        return;
                    }
                }
                subpage.setId(selection + selectionOffset - 1);
                subpage.setLabel(this.searchResult[selection + selectionOffset - 1]);

                if (subpage instanceof ListPage listPage)
                {
                    if (listPage.getCallback() != null)
                    {
                        listPage.setList(listPage.getCallback().apply(listPage));
                        listPage.searchResult = Arrays.copyOf(listPage.list, listPage.list.length);
                    }
                }

                subpage.init();

                Router.redirect(subpage);
            }
        }

        switch (selection)
        {
            case 0 -> {
                inputField.handleInput(action);
                if (inputField.getValue().equals(""))
                {
                    searchResult = list;
                }
                else
                {
                    searchResult = Arrays.stream(SearchEngine.searchSimilar(inputField.getValue(), list, 0.6)).map(result -> result.text).toArray(String[]::new);
                }
            }
        }

        Renderer.refresh();
    }

    public void setList(String[] list)
    {
        this.list = list;
    }

    public Function<Subpage, String[]> getCallback()
    {
        return this.callback;
    }
}
