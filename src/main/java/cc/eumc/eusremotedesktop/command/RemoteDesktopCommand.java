package cc.eumc.eusremotedesktop.command;

import cc.eumc.eusmapdisplay.EusMapDisplay;
import cc.eumc.eusmapdisplay.event.DisplayEventHandler;
import cc.eumc.eusmapdisplay.manager.GlobalMapManager;
import cc.eumc.eusmapdisplay.model.MapDisplay;
import cc.eumc.eusremotedesktop.EusRemoteDesktop;
import cc.eumc.eusremotedesktop.model.Screen;
import cc.eumc.eusremotedesktop.task.ScreenshotRefreshTask;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.map.MapPalette;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RemoteDesktopCommand implements CommandExecutor {
    EusRemoteDesktop plugin;

    public RemoteDesktopCommand(EusRemoteDesktop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("remotedesktop.use")) {
            if (sender instanceof Player player) {
                int width = 4;
                int height = 3;
                if (args.length == 1) {
                    try {
                        width = Integer.parseInt(args[0]);
                    } catch (Exception ex) {
                        sendMessage(sender, "§cError parsing width: " + ex.getMessage());
                        return true;
                    }
                }
                if (args.length == 2) {
                    try {
                        height = Integer.parseInt(args[1]);
                    } catch (Exception ex) {
                        sendMessage(sender, "§cError parsing height: " + ex.getMessage());
                        return true;
                    }
                }

                Screen screen = plugin.getScreen() == null ? new Screen(width * 128, height * 128) : plugin.getScreen();
                plugin.setScreen(screen);
                GlobalMapManager mapManager = plugin.getEusMapDisplayPlugin().getMapManager();
                MapDisplay mapDisplay = mapManager.createMap(width, height, player.getWorld());
                for (ItemStack item : mapManager.getMapItem(mapDisplay)) {
                    player.getInventory().addItem(item);
                }
                Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new ScreenshotRefreshTask(plugin, mapDisplay), 10, 10);
            } else {
                sendMessage(sender, "§eYou must be a player.");
            }
        }
        else {
            sendMessage(sender, "§eSorry.");
        }
        return true;
    }

    private void sendMessage(CommandSender receiver, String message) {
        receiver.sendMessage("[EusRemoteDesktop] " + message);
    }
}
