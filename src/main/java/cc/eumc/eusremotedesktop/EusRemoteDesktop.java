package cc.eumc.eusremotedesktop;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusremotedesktop.command.RemoteDesktopCommand;
import cc.eumc.eusremotedesktop.model.Screen;
import cc.eumc.eusremotedesktop.task.ScreenshotRefreshTask;
import org.bukkit.plugin.java.JavaPlugin;

public final class EusRemoteDesktop extends JavaPlugin {
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

        getServer().getPluginCommand("remotedesktop").setExecutor(new RemoteDesktopCommand(this));
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
