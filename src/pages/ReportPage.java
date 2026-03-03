package src.pages;

import src.components.InputField;
import src.components.MemoField;
import src.pages.core.Page;
import src.utils.Global;
import src.utils.Renderer;

public class ReportPage extends Page {
    private int facilityIdx;
    private InputField title = new InputField("", 50, "Broken Air-con...");
    private MemoField description = new MemoField(23, 50, "Type something...", false);

    public ReportPage(int facilityIdx)
    {
        this.facilityIdx = facilityIdx;
    }

    public void render()
    {
        System.err.println("Facility Name: " + Global.facilities.get(facilityIdx).getName() + "\n");
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

    public void select()
    {

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
            default:
                switch (this.selection)
                {
                    case 0:
                        title.handleInput(action);
                        break;
                    case 1:
                        description.handleInput(action);
                        break;
                }
                break;
        }
        Renderer.refresh();
    }
}
