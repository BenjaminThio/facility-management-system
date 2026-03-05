package src.pages;

import src.components.Ansi;
import src.components.InputField;
import src.models.User;
import src.pages.core.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class SignInPage extends Page {
    InputField[] fields = {
        new InputField("", 30, "name@domain.com"),
        new InputField("", 30, "Create new password")
    };

    public void render()
    {
        User user = Database.User.get(fields[0].getValue());
        System.out.println("Email:");
        fields[0].render();
        System.out.println();
        System.out.println("Password:");
        fields[1].render();
        System.out.println(user == null ? null : user.getPassword() + "," + fields[1].getValue());
        System.out.println();
        System.out.println(new Ansi("Sign In", selection == fields.length ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK).toString());

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
            case "SHIFT_TAB":
            case "UP":
                if (selection - 1 >= 0)
                {
                    selection--;
                }
                else
                {
                    selection = fields.length;
                }  
                break;
            case "TAB":
            case "DOWN":
                if (selection + 1 <= fields.length)
                {
                    selection++;
                }
                else
                {
                    selection = 0;
                }
                break;
            case "ENTER":
                switch (selection)
                {
                    case 2:
                        SignIn();
                        break;
                }
                break;
            case "ESC":
                Router.back();
                break;
        }

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].handleInput(action);
        }
        Renderer.refresh();
    }
}
