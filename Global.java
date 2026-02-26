import java.io.IOException;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Global {
    public static User user = new User("Benjamin Thio Zi Liang", "benjaminthio@utar.edu.my", "1", User.Role.STUDENT);
    public static Terminal terminal;

    public static void init() throws IOException {
        terminal = TerminalBuilder.builder().system(true).build();
        terminal.enterRawMode();
    }
}
