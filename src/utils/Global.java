package src.utils;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import src.models.User;

public class Global {
    private static final String SESSION_PATH = "database/session.txt";
    private static User user = null; // new User("Benjamin Thio Zi Liang", "benjaminthio@utar.edu.my", "1", User.Role.STUDENT);
    private static Terminal terminal;

    public static void clearSession()
    {
        try (FileChannel fileChannel = FileChannel.open(Paths.get(SESSION_PATH), StandardOpenOption.WRITE))
        {
            fileChannel.truncate(0);
            user = null;
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static void setSession(String email)
    {
        try
        {
            Files.writeString(Paths.get(SESSION_PATH), email);
            user = Database.User.get(email);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static void setSession(User newUser)
    {
        try
        {
            Files.writeString(Paths.get(SESSION_PATH), newUser.getEmail());
            user = newUser;
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public static String getSession()
    {
        try
        {
            Path path = Paths.get(SESSION_PATH);

            if (Files.exists(path))
            {
                return Files.readString(path).trim();
            }
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }

        return null;
    }

    public static void setUser(User newUser)
    {
        user = newUser;
    }

    public static User getUser()
    {
        return user;
    }

    public static Terminal getTerminal()
    {
        return terminal;
    }

    public static void init() throws IOException {
        user = Database.User.get(getSession());
        terminal = TerminalBuilder.builder().system(true).build();
        terminal.enterRawMode();
    }
}
