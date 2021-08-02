package cc.eumc.screenmirroring.model;

import java.util.Arrays;

public class Screen {
    private final byte[] pixels;
//    private BufferedImage bufferedImage;
    private final int width;
    private final int height;
    private final int length;
//    private float scaleX;
//    private float scaleY;

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        this.length = width * height;

        this.pixels = new byte[length];
    }

    public void fillPixels(int startIndex, byte[] pixels) {
        System.arraycopy(pixels, 0, this.pixels, startIndex, pixels.length);
    }

//    /**
//     * Zoom to fit and set image
//     * @param image original image
//     */
//    public void setImage(BufferedImage image) {
//        calculateScale(image.getWidth(), image.getHeight());
//        if (scaleX == 1 && scaleY == 1) {
//            this.bufferedImage = image;
//        }
//        BufferedImage after = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//        AffineTransform affineTransform = new AffineTransform();
//        affineTransform.scale(scaleX, scaleY);
//        AffineTransformOp scaleOp = new AffineTransformOp(affineTransform, AffineTransformOp.TYPE_BILINEAR);
//        after = scaleOp.filter(image, after);
//        this.bufferedImage = after;
//    }
//
//    public void calculateScale(int originalWidth, int originalHeight) {
//        this.scaleX = (float)width / originalWidth;
//        this.scaleY = (float)height / originalHeight;
//    }

    public int[] toScreenCoordinates(int x, int y) {
        int[] coordinates = new int[] { x, y };
        // TODO: MapDisplay coordinates to screen coordinates
        return coordinates;
    }

    public byte[] getPixels() {
        return Arrays.copyOf(pixels, pixels.length);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    //    public BufferedImage getBufferedImage() {
//        return bufferedImage;
//    }
}
