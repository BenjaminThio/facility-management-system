package src.pages;

import src.components.Ansi;
import src.components.InputField;
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

    public void render()
    {
        User user = Database.User.get(fields[0].getValue());
        System.out.println("Email:");
        fields[0].setBackgroundColor(selection == 0 ? Ansi.BG_LIGHT_GREEN : fields[0].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[0].render();
        System.out.println();
        System.out.println("Password:");
        fields[1].setBackgroundColor(selection == 1 ? Ansi.BG_LIGHT_GREEN : fields[1].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[1].render();
        System.out.println(user == null ? null : user.getPassword() + "," + fields[1].getValue());
        System.out.println();
        System.out.println("           " + new Ansi("Sign In", selection == fields.length ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK).toString());

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].updateCaret(0, selection * 3 + 1);
        }
    }

    private void SignIn()
    {
        User user = Database.User.get(fields[0].getValue());

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
        Router.redirect(new MenuPage());
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
                        SignIn();
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
