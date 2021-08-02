package cc.eumc.screenmirroringclient.timer;

import cc.eumc.screenmirroringclient.model.Screen;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.TimerTask;
import java.util.function.Consumer;

public class ScreenShotTimer extends TimerTask {
    Robot robot;
    Screen screen;
    Consumer<Screen> callback;

    boolean working = false;

    public ScreenShotTimer(Screen screen, Consumer<Screen> afterScreenshotSet) throws AWTException {
        this.robot = new Robot();
        this.screen = screen;
        this.callback = afterScreenshotSet;
    }

    @Override
    public void run() {
        if (working) {
            return;
        }
        working = true;

        Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = robot.createScreenCapture(capture);

        screen.setScreenshot(image);

        this.callback.accept(screen);
        working = false;
    }
}
