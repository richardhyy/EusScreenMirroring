package cc.eumc.screenmirroringclient.timer;

import cc.eumc.screenmirroringclient.model.Screen;

import java.awt.*;
import java.util.TimerTask;
import java.util.function.BiConsumer;

public class MouseTrackTimer extends TimerTask {
    Screen screen;
    BiConsumer<Screen, int[]> callback;

    boolean working = false;

    public MouseTrackTimer(Screen screen, BiConsumer<Screen, int[]> afterMouseLocationUpdate) {
        this.screen = screen;
        this.callback = afterMouseLocationUpdate;
    }

    @Override
    public void run() {
        if (working) {
            return;
        }
        working = true;

        Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
        callback.accept(screen, screen.toScaledScreenCoordinates(mouseLocation.x, mouseLocation.y));

        working = false;
    }
}
