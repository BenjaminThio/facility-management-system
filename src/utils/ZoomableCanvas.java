package src.utils;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

class ZoomableCanvas extends Canvas {
    private final Image image;
    private double zoomFactor = 1.0;
    private Image offscreen; 
    private int offsetX = 0;
    private int offsetY = 0;
    private int lastMouseX;
    private int lastMouseY;

    public ZoomableCanvas(Image image) {
        this.image = image;
        this.setBackground(new Color(32, 32, 32));

        this.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                double oldZoom = zoomFactor;

                if (e.getWheelRotation() < 0) {
                    zoomFactor *= 1.1; 
                } else {
                    zoomFactor /= 1.1; 
                }
                
                if (zoomFactor < 0.1) zoomFactor = 0.1;

                double scaleChange = zoomFactor / oldZoom;
                int mouseX = e.getX();
                int mouseY = e.getY();
                int centerX = getWidth() / 2;
                int centerY = getHeight() / 2;

                offsetX = (int) (mouseX - centerX - (mouseX - centerX - offsetX) * scaleChange);
                offsetY = (int) (mouseY - centerY - (mouseY - centerY - offsetY) * scaleChange);

                repaint();
            }
        });

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int deltaX = e.getX() - lastMouseX;
                int deltaY = e.getY() - lastMouseY;

                offsetX += deltaX;
                offsetY += deltaY;

                lastMouseX = e.getX();
                lastMouseY = e.getY();

                repaint();
            }
        });
    }

    @Override
    public void update(Graphics g) {
        int width = getWidth();
        int height = getHeight();

        if (width <= 0 || height <= 0) return;

        if (offscreen == null || offscreen.getWidth(this) != width || offscreen.getHeight(this) != height) {
            offscreen = createImage(width, height);
        }

        Graphics offgc = offscreen.getGraphics();
        offgc.setColor(getBackground());
        offgc.fillRect(0, 0, width, height);
        paint(offgc);
        g.drawImage(offscreen, 0, 0, this);
    }

    @Override
    public void paint(Graphics g) {
        int canvasWidth = getWidth();
        int canvasHeight = getHeight();
        int imgWidth = image.getWidth(this);
        int imgHeight = image.getHeight(this);

        if (imgWidth <= 0 || imgHeight <= 0) return;

        double imgAspect = (double) imgWidth / imgHeight;
        double canvasAspect = (double) canvasWidth / canvasHeight;
        int baseWidth, baseHeight;

        if (canvasAspect > imgAspect) {
            baseHeight = canvasHeight;
            baseWidth = (int) (canvasHeight * imgAspect);
        } else {
            baseWidth = canvasWidth;
            baseHeight = (int) (canvasWidth / imgAspect);
        }

        int drawWidth = (int) (baseWidth * zoomFactor);
        int drawHeight = (int) (baseHeight * zoomFactor);
        int x = ((canvasWidth - drawWidth) / 2) + offsetX;
        int y = ((canvasHeight - drawHeight) / 2) + offsetY;

        g.drawImage(image, x, y, drawWidth, drawHeight, this);
    }
}