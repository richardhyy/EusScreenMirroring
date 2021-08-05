package cc.eumc.screenmirroringclient.model;

import org.bukkit.map.MapPalette;

import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Screen {
    private byte[] flattenedPixels;
    private BufferedImage screenshot;
    private final int width;
    private final int height;
    private float scaleX;
    private float scaleY;

    private int threads;
    private final Map<Integer, Byte> paletteCache = new ConcurrentHashMap<>();

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.flattenedPixels = new byte[height * width];
        this.threads = Runtime.getRuntime().availableProcessors();
    }

    /**
     * Zoom to fit and set screenshot
     * @param screenshot original screenshot
     */
    public void setScreenshot(BufferedImage screenshot) {
        calculateScale(screenshot.getWidth(), screenshot.getHeight());
        if (scaleX == 1 && scaleY == 1) {
            this.screenshot = screenshot;
        } else {
            BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            AffineTransform affineTransform = new AffineTransform();
            affineTransform.scale(scaleX, scaleY);
            AffineTransformOp scaleOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
            after = scaleOp.filter(screenshot, after);
            this.screenshot = after;
        }
//        System.out.println("%d x %d".formatted(this.screenshot.getWidth(), this.screenshot.getHeight()));
        try {
            this.flattenedPixels = MapPalette.imageToBytesThreaded(this.screenshot, Runtime.getRuntime().availableProcessors(), 10, paletteCache);
        } catch (InterruptedException e) {
            System.out.println("<!> Can't keep up!");
        }
    }

    public void calculateScale(int originalWidth, int originalHeight) {
        this.scaleX = (float)width / originalWidth;
        this.scaleY = (float)height / originalHeight;
    }

    public int[] toScaledScreenCoordinates(int actualX, int actualY) {
        return new int[] { (int) (scaleX * actualX), (int) (scaleY * actualY) };
    }

    public byte[] getFlattenedPixels() {
        return flattenedPixels;
    }
}