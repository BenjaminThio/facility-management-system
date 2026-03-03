package src.pages;

import java.util.regex.Pattern;

import src.components.Ansi;
import src.components.InputField;
import src.pages.core.Page;
import src.utils.Renderer;
import src.utils.Router;

public class SignUpPage extends Page {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.{3,32}$)[a-zA-Z]+(?: [a-zA-Z]+)*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]{5,32}@utar.edu.my?");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,32}$");
    InputField[] fields = {
        new InputField("", 30, "Enter your full name"),
        new InputField("", 30, "name@domain.com"),
        new InputField("", 30, "Create new password"),
    };

    public void render()
    {
        System.out.println("Full Name:");
        fields[0].render();
        System.out.println();
        System.out.println("Email:");
        fields[1].render();
        System.out.println();
        System.out.println("Password:");
        fields[2].render();
        System.out.println();
        System.out.println(new Ansi("Sign Up", selection == 3 ? Ansi.BG_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK).toString());

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].updateCaret(0, selection * 3 + 1);
        }
    }

    private void SignUp()
    {
        this.fields[0].setError(!USERNAME_PATTERN.matcher(this.fields[0].getValue()).matches());
        this.fields[1].setError(!EMAIL_PATTERN.matcher(this.fields[1].getValue()).matches());
        this.fields[2].setError(!PASSWORD_PATTERN.matcher(this.fields[2].getValue()).matches());

        for (int i = 0; i < fields.length; i++)
        {
            if (fields[i].error())
            {
                return;
            }
        }

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
                    case 3:
                        SignUp();
                        break;
                }
        }

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].handleInput(action);
        }
        Renderer.refresh();
    }
}