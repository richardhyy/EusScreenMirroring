package cc.eumc.eusremotedesktop.task;

import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusremotedesktop.EusRemoteDesktop;
import cc.eumc.eusremotedesktop.model.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;

public class ScreenshotRefreshTask implements Runnable {
    EusRemoteDesktop eusRemoteDesktop;
    MapDisplay mapDisplay;

    boolean working = false;

    public ScreenshotRefreshTask(EusRemoteDesktop eusRemoteDesktop, MapDisplay mapDisplay) {
        this.eusRemoteDesktop = eusRemoteDesktop;
        this.mapDisplay = mapDisplay;
    }

    @Override
    public void run() {
        if (working) {
            return;
        }

        working = true;
        Screen screen = eusRemoteDesktop.getScreen();
        if (screen == null) {
            return;
        }

        try {
            Robot robot = new Robot();

            Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
            BufferedImage image = robot.createScreenCapture(capture);

            screen.setScreenshot(image);
            mapDisplay.getDisplay().plotImage(0, 0, screen.getScreenshot(), null);
        } catch (AWTException e) {
            e.printStackTrace();
        }
        working = false;
    }
}
