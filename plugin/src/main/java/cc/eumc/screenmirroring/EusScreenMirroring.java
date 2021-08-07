package cc.eumc.screenmirroring;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.screenmirroring.command.UserCommand;
import cc.eumc.screenmirroring.manager.MirrorManager;
import cc.eumc.screenmirroring.manager.ScreenServerManager;
import cc.eumc.screenmirroring.task.ScreenUpdateTask;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;

public final class EusScreenMirroring extends JavaPlugin {
    private static EusScreenMirroring instance;

    private EusMapDisplay eusMapDisplayPlugin;
    private Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private MirrorManager mirrorManager;
    private ScreenServerManager screenServerManager;

    private BukkitTask screenUpdateTask;

    private File mirrorFolder;

    @Override
    public void onEnable() {
        instance = this;

        try {
            this.eusMapDisplayPlugin = (EusMapDisplay) getServer().getPluginManager().getPlugin("EusMapDisplay");
        } catch (Exception ex) {
            getLogger().severe("Failed getting EusMapDisplay object.");
            setEnabled(false);
        }

        this.mirrorFolder = new File(getDataFolder(), "mirror");
        if (!mirrorFolder.exists()) {
            mirrorFolder.mkdirs();
        }

        saveDefaultConfig();
        reloadConfig();

        this.mirrorManager = new MirrorManager(this);
        this.screenServerManager = new ScreenServerManager(this);

        UserCommand userCommand = new UserCommand(this);
        PluginCommand pluginCommand = getServer().getPluginCommand("screenmirroring");
        assert pluginCommand != null;
        pluginCommand.setExecutor(userCommand);
        pluginCommand.setTabCompleter(userCommand);

        this.screenUpdateTask = getServer().getScheduler().runTaskTimerAsynchronously(this, new ScreenUpdateTask(mirrorManager), 20, 0);
    }

    @Override
    public void onDisable() {
        screenUpdateTask.cancel();
        screenServerManager.stopServers();
        mirrorManager.saveAll();
    }

    public void sendSevere(String message) {
        Bukkit.getServer().getLogger().severe(prefixForEachLine(message));
    }

    public void sendWarn(String message) {
        Bukkit.getServer().getLogger().warning(prefixForEachLine(message));
    }

    public void sendInfo(String message) {
        Bukkit.getServer().getLogger().info(prefixForEachLine(message));
    }

    public String prefixForEachLine(String text) {
        String prefix = "[ScreenMirroring] ";
        String[] lines = text.split("\n");
        for (int i=0; i<lines.length; i++) {
            lines[i] = prefix + lines[i];
        }
        return String.join("\n", lines);
    }

    public EusMapDisplay getEusMapDisplayPlugin() {
        return eusMapDisplayPlugin;
    }

    public File getMirrorFolder() {
        return mirrorFolder;
    }

    public Gson getGson() {
        return gson;
    }

    public MirrorManager getMirrorManager() {
        return mirrorManager;
    }

    public ScreenServerManager getScreenServerManager() {
        return screenServerManager;
    }

    public static EusScreenMirroring getInstance() {
        return instance;
    }
}
