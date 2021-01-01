package server;

import io.netty.channel.Channel;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Connection {

    private UUID uuid;
    private Player player;
    private Channel channel;

    public Connection(UUID uuid, Player player, Channel channel) {
        this.uuid = uuid;
        this.player = player;
        this.channel = channel;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

}
