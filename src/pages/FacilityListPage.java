package src.pages;

import src.components.InputField;
import src.pages.core.Page;
import src.utils.Database;
import src.utils.Renderer;
import src.utils.Router;

public class FacilityListPage extends Page {
    public enum Subpage
    {
        VIEW_FACILITY,
        REPORT_ISSUE
    }
    private static final int VISIBLE_NUMBER = 30;
    private InputField inputField = new InputField("", 20, "Type something...");
    private int selectionOffset = 0;
    private Subpage subpage;

    public FacilityListPage(Subpage subpage)
    {
        this.subpage = subpage;
    }

    public void render()
    {
        System.out.print("Search: ");

        inputField.render();

        System.out.println();

        for (int i = selectionOffset; i < selectionOffset + VISIBLE_NUMBER; i++)
        {
            if (selection + selectionOffset == i)
            {
                System.out.print("> ");
            }
            else
            {
                System.out.print("  ");
            }
            System.out.println(Integer.toString(i + 1) + ". " + Database.Facility.getAll().get(i).getName());
        }
    }

    public void select()
    {
        switch (subpage)
        {
            case Subpage.VIEW_FACILITY:
                Router.redirect(new ViewFacilityPage(selection + selectionOffset));
                break;
            case Subpage.REPORT_ISSUE:
                Router.redirect(new ReportPage(selection + selectionOffset));
                break;
        }
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP":
                if (selection - 1 >= 0)
                {
                    selection--;
                }
                else if (selection + selectionOffset - 1 >= 0)
                {
                    selectionOffset--;
                }
                break;
            case "DOWN":
                if (selection + 1 < VISIBLE_NUMBER)
                {
                    selection++;
                }
                else if (selection + selectionOffset + 1 < Database.Facility.getAll().size())
                {
                    selectionOffset++;
                }
                break;
            case "ESC":
                Router.back();
                break;
            case "ENTER":
                select();
        }

        Renderer.refresh();
    }
}
