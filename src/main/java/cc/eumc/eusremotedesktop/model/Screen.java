package cc.eumc.eusremotedesktop.model;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class Screen {
    private BufferedImage screenshot;
    private final int width;
    private final int height;
    private float scaleX;
    private float scaleY;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Zoom to fit and set screenshot
     * @param screenshot original screenshot
     */
    public void setScreenshot(BufferedImage screenshot) {
        calculateScale(screenshot.getWidth(), screenshot.getHeight());
        if (scaleX == 1 && scaleY == 1) {
            this.screenshot = screenshot;
        }
        BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        AffineTransform affineTransform = new AffineTransform();
        affineTransform.scale(scaleX, scaleY);
        AffineTransformOp scaleOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
        after = scaleOp.filter(screenshot, after);
        this.screenshot = after;
    }

    public void calculateScale(int originalWidth, int originalHeight) {
        this.scaleX = (float)width / originalWidth;
        this.scaleY = (float)height / originalHeight;
    }

    public int[] toScreenCoordinates(int x, int y) {
        int[] coordinates = new int[] { x, y };
        // TODO: MapDisplay coordinates to screen coordinates
        return coordinates;
    }

    public BufferedImage getScreenshot() {
        return screenshot;
    }
}
