package server;

import common.message.audio.FloatControlAudioMessage;
import common.message.audio.PlainAudioMessage;
import common.message.connection.LinkageMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;
import java.util.UUID;

public class ServerChannelHandler extends ChannelInboundHandlerAdapter {

    private static final UUID NIL_UUID = new UUID(0,0);
    private UUID uuid = NIL_UUID;

    private final FloatControlAudioMessage.AudioData AUDIO_DATA = new FloatControlAudioMessage.AudioData(0,0,null);
    private final FloatControlAudioMessage TO_SEND = new FloatControlAudioMessage(uuid, AUDIO_DATA);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println("Client connected at " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (uuid != null) {
            Server.getConnections().remove(uuid);
            ctx.close();
        }
        System.out.println("Client disconnected at " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        if (msg instanceof LinkageMessage) {

            LinkageMessage message = (LinkageMessage) msg;

            boolean success = false;
            System.out.println("Connection message received.");

            if (uuid == NIL_UUID) {

                int code = message.getCode();

                System.out.println("Code " + code);
                UUID uuid = Server.getPending().get(code);

                if (uuid != null) {

                    this.uuid = uuid;
                    Server.getPending().remove(code);
                    Server.getConnections().put(uuid, new Connection(uuid, Bukkit.getPlayer(uuid), ctx.channel()));

                    message.setUUID(uuid);
                    ctx.writeAndFlush(message);
                    System.out.println("Accepted code");
                    success = true;
                }
            }

            if (!success) {

                ctx.writeAndFlush(message); // returns the original message the nil_uud
                System.out.println("Failed to accept code from " + ctx.channel().remoteAddress());
                ctx.fireChannelInactive();
                ctx.close();
            }


        } else if (msg instanceof PlainAudioMessage) {

            if (uuid == NIL_UUID) return;

            PlainAudioMessage received = (PlainAudioMessage) msg;

            if (received.getUUID() == NIL_UUID) return;


            Player player = Server.getConnections().get(uuid).getPlayer();

            List<Player> players = player.getWorld().getPlayers();

            for (Player p : players) {

                if (p.getUniqueId() == uuid) continue;

                Thread proximityVoiceThread = new Thread(()-> {

                    Connection con = Server.getConnections().get(p.getUniqueId());
                    if (con != null) {

                        Location origin = player.getLocation();

                        double distance = con.getPlayer().getLocation().distanceSquared(origin);

                        if (distance < Server.VC_DIST_SQUARED) {

                            float gain = (float) ((distance / Server.VC_DIST_SQUARED) * -30.0f);

                            Location destination = p.getLocation();

                            Vector player_facing = destination.getDirection();
                            Vector sound_direct = destination.subtract(origin).toVector().normalize();

                            player_facing.setY(0);
                            sound_direct.setY(0);

                            double sound_dir_x = sound_direct.getX();
                            double sound_dir_z = sound_direct.getZ();
                            double facing_x = player_facing.getX();
                            double facing_z = player_facing.getZ();
                            double pan = Math.sin(Math.atan2(sound_dir_x, sound_dir_z) - Math.atan2(facing_x, facing_z))
                                    * (sound_dir_x*sound_dir_x + sound_dir_z*sound_dir_z);

                            TO_SEND.setUUID(p.getUniqueId());
                            AUDIO_DATA.setGain(gain);
                            AUDIO_DATA.setPan((float) pan);
                            AUDIO_DATA.setSound(received.getSound());

                            con.getChannel().writeAndFlush(TO_SEND);

                        }

                    }
                });
                proximityVoiceThread.start();

            }

        } else {

            System.out.println("Unexpected message received " + msg.toString() + ' ' + msg.getClass());
            ctx.fireChannelInactive();
            ctx.close();
        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.fireChannelInactive();
        ctx.close();
    }
}
