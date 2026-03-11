package src.pages;

import java.util.regex.Pattern;

import src.components.Ansi;
import src.components.InputField;
import src.models.User;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class SignUpPage extends Page {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.{3,32}$)[a-zA-Z]+(?: [a-zA-Z]+)*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]{5,32}@utar.edu.my?");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,32}$");
    InputField[] fields = {
        new InputField(31, "Enter your full name"),
        new InputField(31, "username@utar.edu.my"),
        new InputField(31, "Create new password", false),
        new InputField(31, "Confirm your password", false)
    };
    int role = 0;
    boolean isLocked = false;

    public void render()
    {
        System.out.println("Full Name:");
        fields[0].setBackgroundColor(selection == 0 ? Ansi.BG_LIGHT_GREEN : fields[0].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[0].render();
        System.out.println();
        System.out.println("Email:");
        fields[1].setBackgroundColor(selection == 1 ? Ansi.BG_LIGHT_GREEN : fields[1].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[1].render();
        System.out.println();
        System.out.println("Password:");
        fields[2].setBackgroundColor(selection == 2 ? Ansi.BG_LIGHT_GREEN : fields[2].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[2].render();
        System.out.println();
        System.out.println("Confirm Password:");
        fields[3].setBackgroundColor(selection == 3 ? Ansi.BG_LIGHT_GREEN : fields[3].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[3].render();
        System.out.println();
        System.out.println("Role:");
        if (isLocked)
        {
            System.out.println(" " +
            new Ansi(" STUDENT ", User.Role.cast(role) == User.Role.STUDENT ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK).toString() + " " +
            new Ansi("  STAFF  ", User.Role.cast(role) == User.Role.STAFF ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK).toString() + " " +
            new Ansi("  ADMIN  ", User.Role.cast(role) == User.Role.ADMIN ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK).toString());
        }
        else
        {
            switch (selection)
            {
                case 4 -> {
                    System.out.println(" " +
                    new Ansi(" STUDENT ", User.Role.cast(role) == User.Role.STUDENT ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  STAFF  ", User.Role.cast(role) == User.Role.STAFF ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  ADMIN  ", User.Role.cast(role) == User.Role.ADMIN ? Ansi.BG_LIGHT_GREEN : Ansi.BG_GREEN, Ansi.FG_BLACK).toString());
                }
                default -> {
                    System.out.println(" " +
                    new Ansi(" STUDENT ", User.Role.cast(role) == User.Role.STUDENT ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  STAFF  ", User.Role.cast(role) == User.Role.STAFF ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString() + " " +
                    new Ansi("  ADMIN  ", User.Role.cast(role) == User.Role.ADMIN ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK).toString());
                }
            }
        }
        System.out.println();
        System.out.println("           " + new Ansi(" Sign Up ", selection == 5 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK).toString());

        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].updateCaret(0, selection * 3 + 1);
        }
    }

    private void SignUp()
    {
        this.fields[0].setError(Database.User.isExists(
            this.fields[0].getValue()) || !USERNAME_PATTERN.matcher(this.fields[0].getValue()).matches());
        this.fields[1].setError(!EMAIL_PATTERN.matcher(this.fields[1].getValue()).matches());
        this.fields[2].setError(!PASSWORD_PATTERN.matcher(this.fields[2].getValue()).matches() || !this.fields[2].getValue().equals(this.fields[3].getValue()));
        this.fields[3].setError(this.fields[2].error());

        for (int i = 0; i < this.fields.length; i++)
        {
            if (this.fields[i].error())
            {
                return;
            }
        }

        Global.setUser(new User(this.fields[0].getValue(), this.fields[1].getValue(), this.fields[2].getValue(), User.Role.cast(role)));
        Database.User.add(Global.getUser());
        Database.User.save();

        Router.redirect(new MenuPage());
    }

    public void handleAction(String action)
    {
        switch (action)
        {
            case "SHIFT_TAB", "UP" -> {
                if (isLocked)
                    return;

                if (selection - 1 >= 0)
                    selection--;
                else
                    selection = fields.length + 2;
            }
            case "TAB", "DOWN" -> {
                if (isLocked)
                    return;

                if (selection + 1 <= fields.length + 2)
                    selection++;
                else
                    selection = 0;
            }
            case "LEFT" -> {
                if (!isLocked)
                    break;

                if (role - 1 >= 0)
                    role--;
                else
                    role = User.Role.values().length - 1;
            }
            case "RIGHT" -> {
                if (!isLocked)
                    break;

                if (role + 1 < User.Role.values().length)
                    role++;
                else
                    role = 0;
            }
            case "ENTER" -> {
                switch (selection)
                {
                    case 4 -> {
                        isLocked = !isLocked;
                    }
                    case 5 -> {
                        SignUp();
                    }
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "F4" -> {
                switch (selection)
                {
                    case 2, 3:
                        fields[selection].setVisibility(!fields[selection].getVisibility());
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