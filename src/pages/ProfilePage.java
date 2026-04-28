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
import src.models.User;
import src.pages.cores.Page;
import src.utils.Database;
import src.utils.Global;
import src.utils.Renderer;
import src.utils.Router;

public class ProfilePage extends Page {
    private static final int FIELD_LENGTH = 31;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?=.{3,32}$)[a-zA-Z]+(?: [a-zA-Z]+)*$");
    private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile("^(?:\\+?60|0)1[0-9][ -]?\\d{3,4}[ -]?\\d{4}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^a-zA-Z\\d\\s])\\S{8,32}$");

    private InputField[] fields = new InputField[3];
    private Database.Faculty faculty;

    public ProfilePage() {
        User user = Global.getUser();
        fields[0] = new InputField(user.getName(), FIELD_LENGTH, "Enter your full name", true);
        fields[1] = new InputField(user.getPhoneNumber(), FIELD_LENGTH, "Enter your phone number", true);
        fields[2] = new InputField(user.getPassword(), FIELD_LENGTH, "Enter your password", false);
        this.faculty = user.getFaculty();
    }

    @Override
    public void render(StringBuilder frame) {
        ArrayList<ArrayList<AnsiBuilder>> table = new ArrayList<>();

        for (int i = 0; i < fields.length; i++) {
            fields[i].setBackgroundColor(selection == i ? Ansi.BG_LIGHT_GREEN : fields[i].error() ? Ansi.BG_RED : Ansi.BG_WHITE);
            fields[i].setPlaceholderColor(selection == i || fields[i].error() ? Ansi.FG_BLACK : Ansi.FG_DARK_GRAY);
        }

        table.add(new ArrayList<>(Arrays.asList(
            new AnsiBuilder()
                .append(new Container("My Profile", FIELD_LENGTH, Container.Alignment.CENTER, Ansi.BG_BLACK, Ansi.FG_LIGHT_GRAY).toAnsi())
        )));

        AnsiBuilder form = new AnsiBuilder();
        User user = Global.getUser();

        form
            .append("Email (Read-only):\n")
            .append(new Container(user.getEmail(), FIELD_LENGTH, Container.Alignment.LEFT, Ansi.BG_DARK_GRAY, Ansi.FG_WHITE).toAnsi())
            .append("\n\n")
            .append("Role (Read-only):\n")
            .append(new Container(user.getRole().name(), FIELD_LENGTH, Container.Alignment.LEFT, Ansi.BG_DARK_GRAY, Ansi.FG_WHITE).toAnsi())
            .append("\n\n")
            .append("Full Name:\n")
            .append(fields[0].toAnsiBuilder())
            .append('\n')
            .append("Phone Number:\n")
            .append(fields[1].toAnsiBuilder())
            .append('\n')
            .append("Password:\n")
            .append(fields[2].toAnsiBuilder())
            .append('\n')
            .append("Faculty:\n")
            .append(new Container(
                faculty == null ? "NULL" : faculty.getName(),
                FIELD_LENGTH,
                Container.Alignment.CENTER,
                selection == 3 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE,
                Ansi.FG_BLACK
            ).toAnsi())
            .append("\n\n        ")
            .append(new Ansi(" Save Changes ", selection == 4 ? Ansi.BG_LIGHT_GREEN : Ansi.BG_WHITE, Ansi.FG_BLACK));

        table.add(new ArrayList<>(Arrays.asList(form)));
        Table.render(frame, table);
    }

    @Override
    public void updateCaret() {
        if (selection >= 0 && selection < fields.length) {
            Renderer.showInputCaret();
            // Automatically maps the caret Y-offset to the specific field spacing
            fields[selection].updateCaret(1, (selection * 3) + 10);
        } else {
            Renderer.hideInputCaret();
        }
    }

    private void saveChanges() {
        fields[0].setError(!USERNAME_PATTERN.matcher(fields[0].getValue()).matches());
        fields[1].setError(!PHONE_NUMBER_PATTERN.matcher(fields[1].getValue()).matches());
        fields[2].setError(!PASSWORD_PATTERN.matcher(fields[2].getValue()).matches());

        Renderer.refresh();

        if (this.faculty == null) {
            JOptionPane.showMessageDialog(null, "Please select a faculty.", "Missing Faculty", JOptionPane.WARNING_MESSAGE);
            return;
        }

        for (InputField field : fields) {
            if (field.error()) return;
        }

        User user = Global.getUser();
        user.setName(fields[0].getValue());
        user.setPhoneNumber(fields[1].getValue());
        user.setPassword(fields[2].getValue());
        user.setFaculty(this.faculty);

        Database.User.save();
        
        JOptionPane.showMessageDialog(null, "Profile updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
        Router.back();
    }

    @Override
    public void handleAction(String action) {
        switch (action) {
            case "SHIFT_TAB", "UP" -> {
                if (selection - 1 >= 0) selection--;
                else selection = 4;
            }
            case "TAB", "DOWN" -> {
                if (selection + 1 <= 4) selection++;
                else selection = 0;
            }
            case "ENTER" -> {
                if (selection == 3) {
                    Router.redirect(new ListViewPage(
                        Arrays.stream(Database.Faculty.values()).map(Database.Faculty::getName).toArray(String[]::new),
                        (index) -> {
                            this.faculty = Database.Faculty.cast(index);
                        }
                    ));
                } else if (selection == 4) {
                    saveChanges();
                }
            }
            case "ESC" -> Router.back();
            case "F4" -> {
                if (selection == 2) fields[2].setVisibility(!fields[2].getVisibility());
            }
        }

        if (selection >= 0 && selection < fields.length) {
            fields[selection].handleInput(action);
        }
        Renderer.refresh();
    }
}