package src.pages;

import java.util.ArrayList;
import java.util.Arrays;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.components.fields.InputField;
import src.models.User;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class SignInPage extends Page {
    InputField[] fields = {
        new InputField(31, "username@utar.edu.my"),
        new InputField(31, "Create new password", false)
    };

    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        fields[0].setBackgroundColor(selection == 0 ? Ansi.BG_LIGHT_GREEN : fields[0].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[1].setBackgroundColor(selection == 1 ? Ansi.BG_LIGHT_GREEN : fields[1].error() ? Ansi.BG_RED : Ansi.BG_WHITE);

        fields[0].setPlaceholderColor(selection == 0 || fields[0].error() ? Ansi.FG_BLACK : Ansi.FG_DARK_GRAY);
        fields[1].setPlaceholderColor(selection == 1 || fields[1].error() ? Ansi.FG_BLACK : Ansi.FG_DARK_GRAY);

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder()
                .append(new Container("Sign In", 31, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));
        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder()
                .append("Email:\n")
                .append(fields[0].toAnsiBuilder())
                .append('\n')
                .append("Password:\n")
                .append(fields[1].toAnsiBuilder())
                .append('\n')
                .append("           ")
                .append(new Ansi("Sign In", selection == fields.length ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK))
        )));

        Table.render(frame, table);
    }

    @Override
    public void updateCaret()
    {
        if (selection >= 0 && selection < fields.length)
        {
            Renderer.showInputCaret();
            fields[selection].updateCaret(1, selection * 3 + 4);
        }
        else
        {
            Renderer.hideInputCaret();
        }
    }

    private void signIn()
    {
        User user = Database.User.get(fields[0].getValue().toLowerCase());

        fields[0].setError(user == null);
        fields[1].setError(user == null || !user.getPassword().equals(fields[1].getValue()));

        for (int i = 0; i < fields.length; i++)
        {
            if (fields[i].error())
            {
                return;
            }
        }

        Global.setSession(user);
        Router.clear();
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "UP", "SHIFT_TAB" -> {
                if (selection - 1 >= 0)
                    selection--;
                else
                    selection = fields.length;
            }
            case "DOWN", "TAB" -> {
                if (selection + 1 <= fields.length)
                    selection++;
                else
                    selection = 0;
            }
            case "ENTER" -> {
                switch (selection)
                {
                    case 2 -> {
                        signIn();
                    }
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "F4" -> {
                switch (selection)
                {
                    case 1 -> {
                        fields[1].setVisibility(!fields[1].getVisibility());
                    }
                }
            }
        }

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].handleInput(action);
        }
        Renderer.refresh();
    }
}
