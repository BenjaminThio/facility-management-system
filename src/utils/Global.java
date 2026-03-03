package src.utils;
import java.io.IOException;
import java.util.List;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import src.models.Facility;
import src.models.User;

public class Global {
    public static User user = new User("Benjamin Thio Zi Liang", "benjaminthio@utar.edu.my", "1", User.Role.STUDENT);
    public static Terminal terminal;
    public static List<Facility> facilities;

    public static void init() throws IOException {
        terminal = TerminalBuilder.builder().system(true).build();
        terminal.enterRawMode();
        facilities = Database.loadFacilities();
    }
}
