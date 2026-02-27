package src;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        try
        {
            Global.init();
            Renderer.refresh();
            Input.setup();
            Input.listen();
        }
        catch (IOException e)
        {
            System.out.println(e.getMessage());
        }
    }
}