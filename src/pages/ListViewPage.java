package src.pages;

import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Consumer;

import src.components.Ansi;
import src.components.fields.InputField;
import src.pages.cores.Subpage;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.SearchEngine;

public class ListViewPage extends Subpage {
    private static final int VISIBLE_NUMBER = 30;

    public interface ColorMapper {
        int getColor(ListViewPage page, int index, String itemText);
    }
    private ColorMapper colorMapper = null;

    public void setColorMapper(ColorMapper colorMapper) {
        this.colorMapper = colorMapper;
    }

    private InputField inputField = new InputField(20, "Type something...");
    private int selectionOffset = 0;
    private Subpage referenceSubPage = null;

    private Subpage subpage = null;
    private String[] list;
    private String[] searchResult;
    private Function<Subpage, String[]> callback;
    private Consumer<Integer> onSelectCallback;

    @Override
    public Subpage copy()
    {
        ListViewPage clone = new ListViewPage();
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
        clone.onSelectCallback = this.onSelectCallback;
        clone.colorMapper = this.colorMapper;

        return clone;
    }

    private ListViewPage() {}

    public ListViewPage(String[] list, Consumer<Integer> onSelectCallback)
    {
        this.setList(list);
        this.onSelectCallback = onSelectCallback;
    }

    public ListViewPage(Subpage subpage, Function<Subpage, String[]> callback)
    {
        this.referenceSubPage = subpage;
        this.callback = callback;
    }

    public ListViewPage(Subpage subpage, String[] list)
    {
        this.referenceSubPage = subpage;
        this.setList(list);
    }

    // --- CRITICAL FIX: Safe Index Resolution Engine ---
    private int getOriginalIndex(int searchResultIndex) {
        if (this.list == null || this.searchResult == null) return searchResultIndex;

        String targetString = this.searchResult[searchResultIndex];
        int occurrenceInSearchResult = 0;
        
        // Count which # occurrence this string is in the filtered search results
        for (int j = 0; j <= searchResultIndex; j++) {
            if (this.searchResult[j].equals(targetString)) {
                occurrenceInSearchResult++;
            }
        }
        
        int occurrenceInList = 0;
        // Find the exact matching occurrence in the master list
        for (int j = 0; j < this.list.length; j++) {
            if (this.list[j].equals(targetString)) {
                occurrenceInList++;
                if (occurrenceInList == occurrenceInSearchResult) {
                    return j;
                }
            }
        }
        return searchResultIndex; 
    }
    // --------------------------------------------------

    @Override
    public void render(StringBuilder frame)
    {
        frame.append("Search: ");

        inputField.setBackgroundColor(selection == 0 ? Ansi.BG_GREEN : Ansi.BG_WHITE);
        inputField.setPlaceholderColor(selection == 0 ? Ansi.FG_BLACK : Ansi.FG_DARK_GRAY);
        inputField.render(frame);

        frame.append(selectionOffset > 0 ? "▲" : "").append('\n');

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
            
            int textColor = Ansi.FG_WHITE;

            if (this.colorMapper != null) {
                // FIX: Send the perfectly resolved original index to the color mapper!
                textColor = this.colorMapper.getColor(this, getOriginalIndex(i), searchResult[i]);
            }

            frame.append(new Ansi(Integer.toString(i + 1) + ". " + searchResult[i], textColor)).append('\n');
        }

        frame.append(selectionOffset + VISIBLE_NUMBER < searchResult.length ? "▼" : "");
    }

    @Override
    public void updateCaret()
    {
        switch (this.selection)
        {
            case 0 -> {
                Renderer.showInputCaret();
                inputField.updateCaret("Search: ".length(), 0);
            }
            default -> {
                Renderer.hideInputCaret();
            }
        }
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP" -> {
                if (selection - 1 >= 1) {
                    selection--;
                } else if (selectionOffset - 1 >= 0) {
                    selectionOffset--;
                } else if (selection - 1 >= 0) {
                    selection--;
                }
            }
            case "DOWN" -> {
                if (selection + 1 <= (searchResult.length < VISIBLE_NUMBER ? searchResult.length : VISIBLE_NUMBER)) {
                    selection++;
                } else if (selection + selectionOffset + 1 <= searchResult.length) {
                    selectionOffset++;
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "ENTER" -> {
                switch (selection) {
                    case 0 -> { return; }
                }
                
                int targetIndex = selection + selectionOffset - 1;
                int originalIndex = getOriginalIndex(targetIndex);

                if (onSelectCallback != null) {
                    Router.back();
                    // FIX: Pass the true database index to the callback!
                    onSelectCallback.accept(originalIndex);
                    Renderer.refresh();
                    return;
                }

                if (referenceSubPage == null) return;

                subpage = referenceSubPage.copy();
                // FIX: Set the subpage ID to the true database index!
                subpage.setId(originalIndex);
                subpage.setLabel(this.searchResult[targetIndex]);

                if (subpage instanceof ListViewPage listPage) {
                    listPage.triggerCallback();
                }

                Router.redirect(subpage);
                subpage.init();
            }
        }

        switch (selection)
        {
            case 0 -> {
                inputField.handleInput(action);
                if (inputField.getValue().equals("")) {
                    searchResult = list;
                } else {
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