package src.pages;

import src.components.Ansi;
import src.components.Field;
import src.components.InputField;
import src.components.MemoField;
import src.models.Report;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.FileExplorer;
import src.utils.Image;
import src.utils.Renderer;
import src.utils.Router;

public class ReportPage extends Subpage {
    private static final int MIN_TITLE_LENGTH = 5;
    private static final int MAX_TITLE_LENGTH = 50;
    private static final int MIN_DESCRIPTION_LENGTH = 15;
    private static final int MAX_DESCRIPTION_LENGTH = 250;
    private int severity = 0;
    private boolean isLocked = false;
    private FileExplorer fileExplorer;

    private Field[] fields = new Field[]{
        new InputField(50, "Broken Air-con..."),
        new MemoField(23, 50, "Type something...", false)
    };

    public void render()
    {
        System.err.println("Facility Name: " + Database.Facility.getAll().get(id).getName() + "\n");
        System.out.println("Title:");
        this.fields[0].render();
        System.out.println();
        System.out.println("Description:");
        this.fields[1].render();
        System.out.println();
        System.out.println("Severity:");
        if (isLocked)
        {
            System.out.println(" " +
            new Ansi("   LOW    ", severity == 0 ? Ansi.BG_GREEN : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " " +
            new Ansi("  MEDIUM  ", severity == 1 ? Ansi.BG_YELLOW : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " "  +
            new Ansi("   HIGH   ", severity == 2 ? Ansi.BG_RED : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString());
        }
        else
        {
            switch (selection)
            {
                case 2 -> {
                    System.out.println(" " +
                    new Ansi("   LOW    ", severity == 0 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  MEDIUM  ", severity == 1 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " "  +
                    new Ansi("   HIGH   ", severity == 2 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString());
                }
                default -> {
                    System.out.println(" " +
                    new Ansi("   LOW    ", severity == 0 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  MEDIUM  ", severity == 1 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " "  +
                    new Ansi("   HIGH   ", severity == 2 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString());
                }
            }
        }
        System.out.println();
        System.out.println(new Ansi("Upload File",
            this.selection == 3 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
            Ansi.FG_BLACK));
        if (fileExplorer != null && fileExplorer.getPath() != null)
            System.out.println(new Ansi(fileExplorer.getPath().getFullPath(), Ansi.FG_DARK_GRAY));
        System.out.println();
        System.out.println(new Ansi("Submit",
            this.selection == 4 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
            Ansi.FG_BLACK));

        switch (this.selection)
        {
            case 0:
            case 1:
                this.fields[this.selection].updateCaret(0, (3 * this.selection) + 3);
                break;
        }
    }

    private void submit()
    {
        this.fields[0].setError(this.fields[0].getValue().length() < MIN_TITLE_LENGTH || this.fields[0].getValue().length() > MAX_TITLE_LENGTH);
        this.fields[1].setError(this.fields[1].getValue().length() < MIN_DESCRIPTION_LENGTH || this.fields[1].getValue().length() > MAX_DESCRIPTION_LENGTH);

        for (Field field : fields)
        {
            if (field.error())
                return;
        }

        Image image = new Image(fileExplorer.getPath().getFullPath());

        Database.Report.add(Database.Facility.get(id).getName(), new Report(this.fields[0].getValue(), this.fields[1].getValue(), Report.Severity.cast(severity), image.getBase64()));
        Database.Report.save();
        Router.redirect(new MenuPage());
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "TAB":
                if (isLocked)
                    return;
                if (this.selection + 1 <= this.fields.length + 2)
                {
                    this.selection++;
                }
                else
                {
                    this.selection = 0;
                }
                break;
            case "SHIFT_TAB":
                if (isLocked)
                    return;
                if (this.selection - 1 >= 0)
                {
                    this.selection--;
                }
                else
                {
                    this.selection = fields.length + 2;
                }
                break;
            case "LEFT":
                if (!isLocked)
                    return;
                if (severity - 1 >= 0)
                {
                    severity--;
                }
                else
                {
                    severity = Report.Severity.values().length - 1;
                }
                break;
            case "RIGHT":
                if (!isLocked)
                    return;
                if (severity + 1 < Report.Severity.values().length)
                {
                    severity++;
                }
                else
                {
                    severity = 0;
                }
                break;
            case "ESC":
                Router.back();
                break;
            case "ENTER":
                switch (this.selection)
                {
                    case 2:
                        isLocked = !isLocked;
                        break;
                    case 3:
                        fileExplorer = new FileExplorer();
                        break;
                    case 4:
                        submit();
                        break;
                }
                break;
        }
        switch (this.selection)
        {
            case 0:
            case 1:
                this.fields[this.selection].handleInput(action);
                break;
        }
        Renderer.refresh();
    }
}
