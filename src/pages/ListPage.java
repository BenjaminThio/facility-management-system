package src.pages;

import java.util.Arrays;
import java.util.function.Function;

import src.components.Ansi;
import src.components.fields.InputField;
import src.pages.cores.Subpage;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.SearchEngine;

public class ListPage extends Subpage {
    private static final int VISIBLE_NUMBER = 30;
    private InputField inputField = new InputField(20, "Type something...");
    private int selectionOffset = 0;
    private Subpage referenceSubPage = null;
    private Subpage subpage = null;
    private String[] list;
    private String[] searchResult;
    private Function<Subpage, String[]> callback;

    @Override
    public Subpage copy()
    {
        ListPage clone = new ListPage();

        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;

        clone.inputField = this.inputField.copy();
        clone.selectionOffset = this.selectionOffset;
        clone.referenceSubPage = this.referenceSubPage;
        if (this.referenceSubPage != null)
            clone.subpage = this.referenceSubPage.copy();

        if (this.list != null)
            clone.list = Arrays.copyOf(this.list, this.list.length);

        if (this.searchResult != null)
            clone.searchResult = Arrays.copyOf(this.searchResult, this.searchResult.length);

        clone.callback = this.callback;

        return clone;
    }

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

    private ListPage() {}

    public ListPage(Subpage subpage, Function<Subpage, String[]> callback)
    {
        this.referenceSubPage = subpage;
        this.callback = callback;
    }

    public ListPage(Subpage subpage, String[] list)
    {
        this.referenceSubPage = subpage;
        this.setList(list);
    }

    @Override
    public void render(StringBuilder frame)
    {
        frame.append("Search: ");

        inputField.setBackgroundColor(selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE);
        inputField.render(frame);

        frame.append('\n');

        for (int i = selectionOffset; i < selectionOffset + (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER); i++)
        {
            if (selection + selectionOffset - 1 == i)
            {
                frame.append("> ");
            }
            else
            {
                frame.append("  ");
            }
            frame.append(Integer.toString(i + 1) + ". " + searchResult[i]).append('\n');
        }
    }

    @Override
    public void updateCaret()
    {
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
                if (referenceSubPage == null)
                    return;

                subpage = referenceSubPage.copy();
                subpage.setId(selection + selectionOffset - 1);
                subpage.setLabel(this.searchResult[selection + selectionOffset - 1]);

                if (subpage instanceof ListPage listPage)
                {
                    listPage.triggerCallback();
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
        this.searchResult = Arrays.copyOf(list, list.length);
    }

    public String[] getList()
    {
        return this.list;
    }

    public Function<Subpage, String[]> getCallback()
    {
        return this.callback;
    }

    public void triggerCallback()
    {
        if (callback != null)
        {
            this.setList(getCallback().apply(this));
        }
    }

    public void updateSelection()
    {
        if (selection > (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER))
        {
            selection = (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER);
        }
    }
}
