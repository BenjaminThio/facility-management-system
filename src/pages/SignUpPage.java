package src.pages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import src.components.Ansi;
import src.components.AnsiBuilder;
import src.components.Container;
import src.components.Table;
import src.components.fields.InputField;
import src.models.RGB;
import src.models.User;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class SignUpPage extends Page {
    private static final int FIELD_LENGTH = 31;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.{3,32}$)[a-zA-Z]+(?: [a-zA-Z]+)*$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[0-9a-zA-Z]{5,32}@utar.edu.my$");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^(?:\\+?60|0)1[0-9][ -]?\\d{3,4}[ -]?\\d{4}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,32}$");

    InputField[] fields = {
        new InputField(FIELD_LENGTH, "Enter your full name"),
        new InputField(FIELD_LENGTH, "username@utar.edu.my"),
        new InputField(FIELD_LENGTH, "Enter your phone number"),
        new InputField(FIELD_LENGTH, "Create new password", false),
        new InputField(FIELD_LENGTH, "Confirm your password", false)
    };
    int role = 0;
    boolean isLocked = false;

    @Override
    public void render(StringBuilder frame)
    {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        fields[0].setBackgroundColor(selection == 0 ? Ansi.BG_LIGHT_GREEN : fields[0].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[1].setBackgroundColor(selection == 1 ? Ansi.BG_LIGHT_GREEN : fields[1].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[2].setBackgroundColor(selection == 2 ? Ansi.BG_LIGHT_GREEN : fields[2].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[3].setBackgroundColor(selection == 3 ? Ansi.BG_LIGHT_GREEN : fields[3].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
        fields[4].setBackgroundColor(selection == 4 ? Ansi.BG_LIGHT_GREEN : fields[4].error() ? Ansi.BG_RED : Ansi.BG_WHITE);

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder()
                .append(new Container("Sign Up", 31, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));

        AnsiBuilder form = new AnsiBuilder();

        form
            .append("Full Name:" + " ".repeat(FIELD_LENGTH - "Full Name: ".length()) + "ⓘ\n")
            .append(fields[0].toAnsiBuilder())
            .append('\n')
            .append("Email:" + " ".repeat(FIELD_LENGTH - "Email: ".length()) + "ⓘ\n")
            .append(fields[1].toAnsiBuilder())
            .append('\n')
            .append("Phone Number:" + " ".repeat(FIELD_LENGTH - "Phone Number: ".length()) + "ⓘ\n")
            .append(fields[2].toAnsiBuilder())
            .append('\n')
            .append("Password:" + " ".repeat(FIELD_LENGTH - "Password: ".length()) + "ⓘ\n")
            .append(fields[3].toAnsiBuilder())
            .append('\n')
            .append("Confirm Password:\n")
            .append(fields[4].toAnsiBuilder())
            .append('\n')
            .append("Role:\n");

        if (isLocked)
        {
            form
                .append(" ")
                .append(new Ansi(" STUDENT ", User.Role.cast(role) == User.Role.STUDENT ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK))
                .append(" ")
                .append(new Ansi("  STAFF  ", User.Role.cast(role) == User.Role.STAFF ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK))
                .append(" ")
                .append(new Ansi("  ADMIN  ", User.Role.cast(role) == User.Role.ADMIN ? Ansi.BG_LIGHT_BLUE : Ansi.BG_WHITE, Ansi.FG_BLACK))
                .append('\n');
        }
        else
        {
            switch (selection)
            {
                case 5 -> {
                    form
                        .append(" ")
                        .append(new Ansi(" STUDENT ", Ansi.FG_BLACK).background(new RGB(0, User.Role.cast(role) == User.Role.STUDENT ? 255 : 100, 0)))
                        .append(" ")
                        .append(new Ansi("  STAFF  ", Ansi.FG_BLACK).background(new RGB(0, User.Role.cast(role) == User.Role.STAFF ? 255 : 100, 0)))
                        .append(" ")
                        .append(new Ansi("  ADMIN  ", Ansi.FG_BLACK).background(new RGB(0, User.Role.cast(role) == User.Role.ADMIN ? 255 : 100, 0)))
                        .append('\n');
                }
                default -> {
                    form
                        .append(" ")
                        .append(new Ansi(" STUDENT ", User.Role.cast(role) == User.Role.STUDENT ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                        .append(" ")
                        .append(new Ansi("  STAFF  ", User.Role.cast(role) == User.Role.STAFF ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                        .append(" ")
                        .append(new Ansi("  ADMIN  ", User.Role.cast(role) == User.Role.ADMIN ? Ansi.BG_WHITE : Ansi.BG_DARK_GRAY, Ansi.FG_BLACK))
                        .append('\n');
                }
            }
        }

        form.append("\n           ");
        form.append(new Ansi(" Sign Up ", selection == 6 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK));

        table.add(new ArrayList<>(Arrays.asList(form)));

        Table.render(frame, table);
    }

    @Override
    public void updateCaret()
    {
        if (selection >= 0 && selection < fields.length)
        {
            fields[selection].updateCaret(1, selection * 3 + 4);
        }
    }

    private void signUp()
    {
        this.fields[0].setError(Database.User.isExists(
            this.fields[0].getValue()) || !USERNAME_PATTERN.matcher(this.fields[0].getValue()).matches());
        this.fields[1].setError(Database.User.isExists(this.fields[1].getValue()) || !EMAIL_PATTERN.matcher(this.fields[1].getValue()).matches());
        this.fields[2].setError(!PHONE_NUMBER_PATTERN.matcher(this.fields[2].getValue()).matches());
        this.fields[3].setError(!PASSWORD_PATTERN.matcher(this.fields[3].getValue()).matches() || !this.fields[3].getValue().equals(this.fields[4].getValue()));
        this.fields[4].setError(this.fields[3].error());

        for (int i = 0; i < this.fields.length; i++)
        {
            if (this.fields[i].error())
            {
                return;
            }
        }

        Global.setUser(new User(this.fields[0].getValue(), this.fields[1].getValue().toLowerCase(), this.fields[2].getValue(), this.fields[3].getValue(), User.Role.cast(role)));
        Database.User.add(Global.getUser());
        Database.User.save();

        Router.clear();
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
                    case 5 -> {
                        isLocked = !isLocked;
                    }
                    case 6 -> {
                        signUp();
                    }
                }
            }
            case "ESC" -> {
                Router.back();
            }
            case "F4" -> {
                switch (selection)
                {
                    case 3, 4:
                        fields[selection].setVisibility(!fields[selection].getVisibility());
                }
            }
            case "F1" -> {
                switch (selection)
                {
                    case 0 -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "Username requirements:\n" +
                            "1. Must be 3–32 characters long.\n" +
                            "2. Letters only (A–Z, a–z).\n" +
                            "3. Spaces are allowed between words only (no leading, trailing, or extra spaces).",
                            "Username Guidelines",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    case 1 -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "Email requirements:\n" +
                            "1. Must be a valid UTAR email address\n" +
                            "2. Format: username@utar.edu.my\n" +
                            "3. Username must be 5–32 characters (letters and numbers only)",
                            "Email Guidelines",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    case 2 -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "Phone number requirements:\n" +
                            "1. Must be a valid Malaysian mobile number\n" +
                            "2. Can start with 0 or +60\n" +
                            "3. Format: 01X-XXXXXXX or +601X-XXXXXXX\n" +
                            "4. Spaces or dashes are allowed",
                            "Phone Number Guidelines",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }
                    case 3 -> {
                        JOptionPane.showMessageDialog(
                            null,
                            "Password requirements:\n" +
                            "1. 8 to 32 characters\n" +
                            "2. At least one uppercase letter (A–Z)\n" +
                            "3. At least one lowercase letter (a–z)\n" +
                            "4. At least one number (0–9)\n" +
                            "5. At least one special character (e.g., !@#$%)\n" +
                            "6. No spaces allowed",
                            "Password Guidelines",
                            JOptionPane.INFORMATION_MESSAGE
                        );
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