package cc.eumc.screenmirroring.task;

import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.screenmirroring.EusScreenMirroring;
import cc.eumc.screenmirroring.model.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenshotRefreshTask implements Runnable {
    EusScreenMirroring eusScreenMirroring;
    MapDisplay mapDisplay;

    boolean working = false;

    public ScreenshotRefreshTask(EusScreenMirroring eusScreenMirroring, MapDisplay mapDisplay) {
        this.eusScreenMirroring = eusScreenMirroring;
        this.mapDisplay = mapDisplay;
    }

    @Override
    public void run() {
        if (working) {
            return;
        }

        working = true;
        Screen screen = eusScreenMirroring.getScreen();
        if (screen == null) {
            return;
        }

        try {
            Robot robot = new Robot();

            Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage image = robot.createScreenCapture(capture);

            screen.setScreenshot(image);
            mapDisplay.getDisplay().drawImage(0, 0, screen.getScreenshot(), null);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        working = false;
    }
}
