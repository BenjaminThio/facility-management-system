package src.utils;

import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;

import javax.imageio.ImageIO;

public class Image {
    String base64;

    public Image(String path)
    {
        try
        {
            byte[] imageBytes = Files.readAllBytes(Paths.get(path));
            this.base64 = Base64.getEncoder().encodeToString(imageBytes);
        }
        catch (IOException e)
        {
            System.err.println(e.getMessage());
        }
    }

    public String getBase64()
    {
        return this.base64;
    }

    public static void show(String base64)
    {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(imageBytes));

            if (image != null) {
                Frame frame = new Frame("Preview");

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        frame.dispose();
                    }
                });

                ZoomableCanvas canvas = new ZoomableCanvas(image);

                canvas.setSize(image.getWidth(), image.getHeight());

                frame.add(canvas);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}