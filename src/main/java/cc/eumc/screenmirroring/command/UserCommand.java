package cc.eumc.screenmirroring.command;

import cc.eumc.screenmirroring.EusScreenMirroring;
import cc.eumc.screenmirroring.model.Mirror;
import org.apache.logging.log4j.util.Strings;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class UserCommand implements CommandExecutor, TabCompleter {
    private final EusScreenMirroring plugin;
    private final String[] commands = { "create", "get", "list", "connection" };

    public UserCommand(EusScreenMirroring plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender.hasPermission("screenmirroring.user")) {
            if (args.length == 0) {
                sendMessage(sender, "Available commands: " + Strings.join((Iterable<?>) command, ','));
                return true;
            }

            switch (args[0].toLowerCase()) {
                case "create" -> {
                    if (sender instanceof Player player) {
                        int width = 4;
                        int height = 3;
                        if (args.length == 2) {
                            try {
                                width = Integer.parseInt(args[0]);
                            } catch (Exception ex) {
                                sendMessage(sender, "§cError parsing width: " + ex.getMessage());
                                return true;
                            }
                        }
                        if (args.length == 3) {
                            try {
                                height = Integer.parseInt(args[1]);
                            } catch (Exception ex) {
                                sendMessage(sender, "§cError parsing height: " + ex.getMessage());
                                return true;
                            }
                        }

                        Mirror mirror = plugin.getMirrorManager().createMirror(player.getUniqueId(), width, height);
                        player.getInventory().addItem(plugin.getEusMapDisplayPlugin().getMapManager().getMapItem(mirror.getMapDisplay()));

                    } else {
                        sendPlayerOnly(sender);
                    }
                }

                case "get" -> {
                    if (args.length == 1) {
                        sendMessage(sender, "&eUsage: /screenmirroring get <UUID> [PlayerName] [column(x)] [line(y)]");
                        return true;
                    }

                    short id;
                    Player player;
                    int column = -1; // -1 for all
                    int line = -1;

                    try { // parsing args
                        id = Short.parseShort(args[1]);
                        if (args.length > 2) { // with [PlayerName] [..?]
                            player = Bukkit.getPlayer(args[2]);
                            if (player == null) {
                                sendMessage(sender, "&ePlayer %s not found.".formatted(args[2]));
                                return true;
                            }

                            if (args.length > 3) { // with [column] [..?]
                                column = args[3].equals("*") ? -1 : Integer.parseInt(args[3]);

                                if (args.length > 4) { // with [line]
                                    line = args[4].equals("*") ? -1 : Integer.parseInt(args[4]);
                                }
                            }
                        } else if (sender instanceof Player) {
                            player = (Player) sender;
                        } else {
                            sendPlayerOnly(sender);
                            return true;
                        }
                    } catch (Exception ex) {
                        sendMessage(sender, String.format("&cError: %s", ex.getMessage()));
                        return true;
                    }

                    Mirror mirror = plugin.getMirrorManager().getMirror(id);

                    if (mirror == null) {
                        sendMessage(sender, String.format("Mirro %s does not exist.", id));
                        return true;
                    }

                    for (int x = (column == -1 ? 0 : column); x <= (column == -1 ? mirror.getWindowWidth() - 1 : column); x++) {
                        for (int y = (line == -1 ? 0 : line); y <= (line == -1 ? mirror.getWindowHeight() - 1 : line); y++) {
                            player.getInventory().addItem(plugin.getEusMapDisplayPlugin().getMapManager().getMapItem(mirror.getMapDisplay(), x, y));
                        }
                    }
                }

                case "list" -> {
                    AtomicReference<Integer> count = new AtomicReference<>(0);
                    plugin.getMirrorManager().getMirrorMap().forEach((id, mirror) -> {
                        if (sender instanceof ConsoleCommandSender || mirror.getOwnerUUID().equals(((Player)sender).getUniqueId().toString())) {
                            sendMessage(sender, String.format("%d (%d x %d) | Passcode: %s", id, mirror.getWindowWidth(), mirror.getWindowHeight(), mirror.getPassword()));
                            count.getAndSet(count.get() + 1);
                        }
                    });
                    if (count.get() == 0) {
                        sendMessage(sender, "No mirrors. Use /screenmirroring create [width] [height] to create one.");
                    }
                }

                case "connection" -> {
                    sendMessage(sender, "Ports:");
                    sendMessage(sender, Strings.join(plugin.getConfig().getIntegerList("General.ScreenServerPorts"), ','));
                }
            }
        }
        else {
            sendMessage(sender, "§eSorry.");
        }
        return true;
    }

    private void sendMessage(CommandSender receiver, String message) {
        receiver.sendMessage("[EusScreenMirroring] " + message);
    }

    private void sendPlayerOnly(CommandSender receiver) {
        sendMessage(receiver, "&ePlayer only command.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("screenmirroring.user")) return new ArrayList<>();

        if (args.length > 1)
            return new ArrayList<>();
        else if (args.length == 1)
            return Arrays.stream(commands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        else
            return Arrays.asList(commands);
    }
}
