package src;
import java.io.IOException;

import src.utils.Global;
import src.utils.Input;
import src.utils.Renderer;

public class Main
{
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