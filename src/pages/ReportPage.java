package src.pages;

import java.util.ArrayList;
import java.util.Arrays;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Table;
import src.components.fields.InputField;
import src.components.fields.MemoField;
import src.components.fields.core.Field;
import src.models.RGB;
import src.models.Report;
import src.pages.cores.Subpage;
import src.utils.Database;
import src.utils.FileExplorer;
import src.utils.Global;
import src.utils.Image;
import src.utils.Renderer;
import src.utils.Router;
import src.utils.Util;

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
        new MemoField("", 23, 50, "Type something...", false)
    };

    @Override
    public Subpage copy()
    {
        ReportPage clone = new ReportPage();

        clone.selection = this.selection;
        clone.id = this.id;
        clone.label = this.label;

        clone.severity = this.severity;
        clone.isLocked = this.isLocked;

        if (clone.fields != null)
            for (int i = 0; i < this.fields.length; i++)
                clone.fields[i] = this.fields[i].copy();

        return clone;
    }

    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();
        Ansi inputFieldLength = new Ansi(
            "(" + this.fields[0].getValue().length() + "/" + Integer.toString(MAX_TITLE_LENGTH) + ")",
            this.fields[0].getValue().length() > MAX_TITLE_LENGTH ? Ansi.FG_RED : Ansi.FG_WHITE
        );
        Ansi memoFieldLength = new Ansi(
            "(" + this.fields[1].getValue().length() + "/" + Integer.toString(MAX_DESCRIPTION_LENGTH) + ")",
            this.fields[1].getValue().length() > MAX_DESCRIPTION_LENGTH ? Ansi.FG_RED : Ansi.FG_WHITE
        );

        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            "Facility Name: " + Database.Facility.getAll().get(id).getName()
        ))));

        AnsiBuilder severitySelect = new AnsiBuilder();

        if (isLocked)
        {
            severitySelect
                 .append(new Ansi("   LOW    ", severity == 0 ? Ansi.BG_GREEN : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                 .append(' ')
                 .append(new Ansi("  MEDIUM  ", severity == 1 ? Ansi.BG_YELLOW : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                 .append(' ')
                 .append(new Ansi("   HIGH   ", severity == 2 ? Ansi.BG_RED : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK));
        }
        else
        {
            switch (selection)
            {
                case 2 -> {
                    severitySelect
                         .append(new Ansi("   LOW    ", Ansi.FG_BLACK).background(new RGB(0, severity == 0 ? 255 : 128, 0)))
                         .append(' ')
                         .append(new Ansi("  MEDIUM  ", Ansi.FG_BLACK).background(new RGB(0, severity == 1 ? 255 : 128, 0)))
                         .append(' ')
                         .append(new Ansi("   HIGH   ", Ansi.FG_BLACK).background(new RGB(0, severity == 2 ? 255 : 128, 0)));
                }
                default -> {
                    severitySelect
                         .append(new Ansi("   LOW    ", severity == 0 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                         .append(' ')
                         .append(new Ansi("  MEDIUM  ", severity == 1 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                         .append(' ')
                         .append(new Ansi("   HIGH   ", severity == 2 ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK));
                }
            }
        }

        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            "Title:\n",
            this.fields[0].toAnsiBuilder(),
            " ".repeat(this.fields[0].length() - inputFieldLength.length()),
            inputFieldLength,
            "\nDescription:\n",
            this.fields[1].toAnsiBuilder(),
            " ".repeat(this.fields[1].length() - memoFieldLength.length()),
            memoFieldLength
        ))));
        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            "Severity:\n" + " ".repeat(9),
            severitySelect
        ))));
        if (fileExplorer != null && fileExplorer.getPath() != null)
        {
            table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
                "Proof Image File:\n",
                new Ansi(" Upload File ",
                    this.selection == 3 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                    Ansi.FG_BLACK),
                " ",
                new Ansi(
                    Util.textOverflowEllipsis(
                        fileExplorer.getPath().getFullPath(),
                        this.fields[1].length() - 14
                    ), Ansi.FG_DARK_GRAY
                )
            ))));
        }
        else
        {
            table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
                "Proof Image File:\n",
                " ".repeat(18),
                new Ansi(" Upload File ",
                    this.selection == 3 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                    Ansi.FG_BLACK)
            ))));
        }
        table.add(new ArrayList<>(Arrays.asList(new AnsiBuilder(
            " ".repeat(18),
            new Ansi("   Submit    ",
                this.selection == 4 ? Ansi.BG_GREEN : Ansi.BG_WHITE,
                Ansi.FG_BLACK)
        ))));

        Table.render(frame, table);
    }

    @Override
    public void updateCaret()
    {
        switch (this.selection)
        {
            case 0:
            case 1:
                this.fields[this.selection].updateCaret(1, (3 * this.selection) + 4);
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

        Database.Report.add(Database.Facility.get(id).getName(), new Report(this.fields[0].getValue(), this.fields[1].getValue(), Report.Severity.cast(severity), image.getBase64(), Global.getUser().getEmail()));
        Database.Report.save();
        Router.clear();
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
            case 0, 1:
                this.fields[this.selection].handleInput(action);
                break;
        }
        Renderer.refresh();
    }
}
