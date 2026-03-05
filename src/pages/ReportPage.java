package src.pages;

import src.components.InputField;
import src.components.MemoField;
import src.pages.core.Page;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;

public class ReportPage extends Page {
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MIN_DESCRIPTION_LENGTH = 15;
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private int facilityIdx;
    private InputField title = new InputField("", 50, "Broken Air-con...");
    private MemoField description = new MemoField(23, 50, "Type something...", false);

    public ReportPage(int facilityIdx)
    {
        this.facilityIdx = facilityIdx;
    }

    public void render()
    {
        System.err.println("Facility Name: " + Database.Facility.getAll().get(facilityIdx).getName() + "\n");
        System.out.println("Title:");
        title.render();
        System.out.println();
        System.out.println("Description:");
        description.render();

        switch (this.selection)
        {
            case 0:
                title.updateCaret(0, 3);
                break;
            case 1:
                description.updateCaret(0, 6);
                break;
        }
    }

    private void submit()
    {
        title.setError(title.getValue().length() < MIN_TITLE_LENGTH || title.getValue().length() > MAX_TITLE_LENGTH);
        description.setError(description.getValue().length() < MIN_DESCRIPTION_LENGTH || description.getValue().length() > MAX_DESCRIPTION_LENGTH);


    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "TAB":
                if (selection + 1 < 2)
                {
                    selection++;
                }
                else
                {
                    selection = 0;
                }
                break;
            case "SHIFT_TAB":
                if (selection - 1 >= 0)
                {
                    selection--;
                }
                else
                {
                    selection = 1;
                }
                break;
            case "ESC":
                Router.back();
                break;
            default:
                switch (this.selection)
                {
                    case 0:
                        title.handleInput(action);
                        break;
                    case 1:
                        description.handleInput(action);
                        break;
                    case 2:
                        switch (action)
                        {
                            case "Enter":

                                break;
                        }
                        break;
                }
                break;
        }
        Renderer.refresh();
    }
}
