package src;

import java.util.List;

public class ViewFacilities extends Page {
    private final int VISIBLE_NUMBER = 30;
    private List<Facility> facilities;
    private int selectionOffset = 0;

    public void init()
    {
        facilities = Database.loadFacilities();
    }

    public void render()
    {
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
            System.out.println(Integer.toString(i + 1) + ". " + facilities.get(i).getName());
        }
    }

    public void select()
    {
        // TODO: View Facility Info
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
                else if (selection + selectionOffset + 1 < facilities.size())
                {
                    selectionOffset++;
                }
                break;
            case "BACK":
                Route.back();
                break;
        }

        Renderer.refresh();
    }

    public static void redirect()
    {
        ViewFacilities page = new ViewFacilities();

        page.init();
        Route.redirect(page);
    }
}
