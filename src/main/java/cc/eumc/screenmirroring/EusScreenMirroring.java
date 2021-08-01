package cc.eumc.screenmirroring;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.screenmirroring.command.RemoteDesktopCommand;
import cc.eumc.screenmirroring.model.Screen;
import cc.eumc.screenmirroring.task.ScreenshotRefreshTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class EusScreenMirroring extends JavaPlugin {
    private EusMapDisplay eusMapDisplayPlugin;
    private ScreenshotRefreshTask screenshotRefreshTask;
    private Screen screen = null;

    @Override
    public void onEnable() {
        try {
            this.eusMapDisplayPlugin = (EusMapDisplay) getServer().getPluginManager().getPlugin("EusMapDisplay");
        } catch (Exception ex) {
            getLogger().severe("Failed getting EusMapDisplay object.");
            setEnabled(false);
        }

        getServer().getPluginCommand("screenmirroring").setExecutor(new RemoteDesktopCommand(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public EusMapDisplay getEusMapDisplayPlugin() {
        return eusMapDisplayPlugin;
    }

    public Screen getScreen() {
        return screen;
    }

    public void setScreen(Screen screen) {
        this.screen = screen;
    }

    public ScreenshotRefreshTask getScreenshotRefreshTask() {
        return screenshotRefreshTask;
    }

    public void setScreenshotRefreshTask(ScreenshotRefreshTask screenshotRefreshTask) {
        this.screenshotRefreshTask = screenshotRefreshTask;
    }
}
