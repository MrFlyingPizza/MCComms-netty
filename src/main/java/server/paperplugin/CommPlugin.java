package server.paperplugin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;
import server.Server;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CommPlugin extends JavaPlugin {

    Random random = new Random();

    final float defaultRange = 40.0f;
    float configRange;

    CommPlugin instance = this;

    @Override
    public void onEnable() {

        FileConfiguration config = this.getConfig();
        config.addDefault("port", 24456);
        config.addDefault("vcrange", 40.0f);
        config.options().copyDefaults(true);
        saveConfig();

        int port = config.getInt("port");

        try {
            Server.getInstance().start(port);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onPlayerQuit(PlayerQuitEvent event) {
                UUID uuid = event.getPlayer().getUniqueId();
                Server.getConnections().get(uuid).getChannel().closeFuture();
                Server.getConnections().remove(uuid);
            }
        }, this);

        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onDisable(PluginDisableEvent event) {
                Server.getInstance().stop();
            }
        }, this);

        Objects.requireNonNull(getCommand("voiceclientcount")).setExecutor(
                (sender, command, label, args)->{
                    sender.sendMessage("Number of registered clients: " + Server.getConnections().size());
                    return true;
                });

        Objects.requireNonNull(getCommand("pendingcodecount")).setExecutor(
                (sender, command, label, args)->{
                    sender.sendMessage("Number of pending codes: " + Server.getPending().size());
                    return true;
                });

        Objects.requireNonNull(getCommand("setproximityrange")).setExecutor(
                (sender, command, label, args)->{
                    if (args.length == 1) {
                        try {
                            configRange = Integer.parseInt(args[0]);
                            Server.VC_DIST_SQUARED = configRange * configRange;
                            config.set("vcrange", configRange);
                            sender.sendMessage("Set proximity chat range to " + configRange);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("Argument must be a decimal value!");
                        }

                    } else if (args.length == 0) {
                        configRange = defaultRange;
                        sender.sendMessage("Set proximity chat range to default - " + defaultRange);
                    } else {
                        return false;
                    }
                    return true;
                });

        Objects.requireNonNull(getCommand("authvoiceclient")).setExecutor(

                (sender, command, label, args) -> {

                    if (sender instanceof Player) {

                        Player player = (Player) sender;

                        if (Server.getConnections().containsKey(player.getUniqueId())) {

                            player.sendRawMessage("Your VoIP client is already connected.");

                        } else {
                            int code = 100000 + random.nextInt(900000);
                            while (Server.getPending().containsKey(code)) code = 100000 + random.nextInt(900000);

                            Server.getPending().put(code, player.getUniqueId());
                            int finalCode = code;
                            Server.getExecutorService().schedule(()->Server.getPending().remove(finalCode), 2, TimeUnit.MINUTES);
                            player.sendRawMessage("Connect with the following code in VoIP client: " + code);

                        }

                        return true;
                    }

                    return false;
                });

    }

    public static class PlayerOnlineEventListener implements Listener {

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent e) {



        }

    }

}
