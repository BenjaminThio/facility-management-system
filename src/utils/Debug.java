package src.utils;

import java.io.FileWriter;
import java.io.IOException;

public class Debug {
    public static void print(String content)
    {
        try
        {
            FileWriter fileWriter = new FileWriter("debug/output.txt");

            fileWriter.write(content);
            fileWriter.close();
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }
}
