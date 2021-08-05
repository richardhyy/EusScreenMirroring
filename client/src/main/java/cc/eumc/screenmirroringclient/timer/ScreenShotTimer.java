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
    boolean paused = false;

    public ScreenShotTimer(Screen screen, Consumer<Screen> afterScreenshotSet) throws AWTException {
        this.robot = new Robot();
        this.screen = screen;
        this.callback = afterScreenshotSet;
    }

    @Override
    public void run() {
        if (working || paused) {
            return;
        }
        working = true;

//        System.out.println("Stage 1");
//        long startTime = System.currentTimeMillis();
        Rectangle capture = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        BufferedImage image = robot.createScreenCapture( capture);
//        System.out.println("T = " + (System.currentTimeMillis() - startTime));

//        System.out.println("Stage 2 start");
//        startTime = System.currentTimeMillis();
        screen.setScreenshot(image);
//        System.out.println("T = " + (System.currentTimeMillis() - startTime));

//        System.out.println("Stage 3 start");
//        startTime = System.currentTimeMillis();
        this.callback.accept(screen);
//        System.out.println("T = " + (System.currentTimeMillis() - startTime));
        working = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
