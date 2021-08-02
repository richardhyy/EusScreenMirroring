package cc.eumc.screenmirroring.task;

import cc.eumc.eusmapdisplay.model.Display;
import cc.eumc.screenmirroring.manager.MirrorManager;
import org.bukkit.Bukkit;

public class ScreenUpdateTask implements Runnable {
    MirrorManager mirrorManager;

    boolean working = false;

    public ScreenUpdateTask(MirrorManager mirrorManager) {
        this.mirrorManager = mirrorManager;
    }

    @Override
    public void run() {
        if (working || Bukkit.getServer().getOnlinePlayers().size() == 0) {
            return;
        }
        working = true;

        mirrorManager.getMirrors().forEach(mirror -> {
            if (mirror.getMapDisplay().isActive()) {
                Display display = mirror.getMapDisplay().getDisplay();
                byte[] flatPixels = mirror.getScreen().getPixels();
                for (int x = 0; x < display.getWidth(); x++) {
                    for (int y = 0; y < display.getHeight(); y++) {
                        display.setPixel(x, y, flatPixels[x + y * display.getWidth()]);
                    }
                }
            }
        });

        working = false;
    }
}
