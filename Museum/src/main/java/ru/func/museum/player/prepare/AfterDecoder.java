package ru.func.museum.player.prepare;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import net.minecraft.server.v1_12_R1.EnumHand;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayInUseItem;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.func.museum.App;
import ru.func.museum.player.Archaeologist;

import java.util.List;
import java.util.UUID;

/**
 * @author func 31.05.2020
 * @project Museum
 */
public class AfterDecoder implements Prepare {
    @Override
    public void execute(Player player, Archaeologist archaeologist, App app) {
        ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline().addAfter("decoder",
                UUID.randomUUID().toString(), new MessageToMessageDecoder<Packet>() {
                    @Override
                    protected void decode(ChannelHandlerContext channelHandlerContext, Packet packet, List<Object> list) {
                        if (archaeologist.isOnExcavation() && packet instanceof PacketPlayInUseItem) {
                            PacketPlayInUseItem pc = (PacketPlayInUseItem) packet;
                            if (pc.c.equals(EnumHand.OFF_HAND) || archaeologist.getLastExcavation()
                                    .getExcavation()
                                    .getExcavationGenerator()
                                    .fastCanBreak(pc.a.getX(), pc.a.getY(), pc.a.getZ())
                            ) {
                                return;
                            }
                        }
                        list.add(packet);
                    }
                }
        );
    }
}
