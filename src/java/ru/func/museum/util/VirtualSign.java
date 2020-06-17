package ru.func.museum.util;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import ru.cristalix.core.lib.Preconditions;
import ru.func.museum.player.pickaxe.Pickaxe;

import java.util.*;
import java.util.function.Consumer;

/**
 * @author func 04.06.2020
 * @project Museum
 */
@NoArgsConstructor
public final class VirtualSign {
    private final Map<Integer, String> defaultText = Maps.newHashMapWithExpectedSize(4);
    private final Collection<UUID> viewers = Sets.newHashSet();

    public VirtualSign(@NonNull List<String> text) {
        Preconditions.checkState(text.size() == 4, "text size must be equal to 4!");
        for (int i = 0; i < text.size(); i++) {
            defaultText.put(i, text.get(i));
        }
    }

    public boolean hasViewer(UUID uuid) {
        return viewers.contains(uuid);
    }

    public void setDefaultText(int line, String text) {
        defaultText.put(line, text == null ? null : ChatColor.translateAlternateColorCodes('&', text));
    }

    public Optional<String> getDefaultText(int line) {
        return Optional.ofNullable(defaultText.get(line));
    }

    public void openSign(Player p, Consumer<String[]> response) {
        UUID uuid = p.getUniqueId();
        viewers.add(uuid);
        PlayerConnection connection = ((CraftPlayer) p).getHandle().playerConnection;
        Location location = p.getLocation();
        BlockPosition position = new BlockPosition(location.getX(), 0, location.getZ());
        if (!defaultText.isEmpty()) {
            NBTTagCompound compound = new NBTTagCompound();
            for (int i = 0; i < 4; i++) {
                compound.setString("Text" + (i + 1), IChatBaseComponent.ChatSerializer.a(new ChatComponentText(defaultText.getOrDefault(i, ""))));
            }
            PacketPlayOutBlockChange blockChange = new PacketPlayOutBlockChange(Pickaxe.WORLD, position);
            blockChange.block = Blocks.STANDING_SIGN.getBlockData();
            connection.sendPacket(blockChange);
            connection.sendPacket(new PacketPlayOutTileEntityData(position, 9, compound));
        }
        connection.sendPacket(new PacketPlayOutOpenSignEditor(position));
        Channel channel = connection.networkManager.channel;
        ChannelFutureListener listener = future -> viewers.remove(uuid);
        channel.closeFuture().addListener(listener);
        channel.pipeline().addAfter("decoder", "sign_handler", new MessageToMessageDecoder<Packet>() {
            @Override
            protected void decode(ChannelHandlerContext channelHandlerContext, Packet packet, List<Object> out) {
                try {
                    if (packet instanceof PacketPlayInUpdateSign) {
                        PacketPlayInUpdateSign updateSign = (PacketPlayInUpdateSign) packet;
                        try {
                            String[] lines = updateSign.b.clone();
                            MinecraftServer.getServer().a(() -> {
                                response.accept(lines);
                                return null;
                            });
                        } catch (Throwable t) {
                            t.printStackTrace();
                            ((CraftPlayer) p).disconnect(t.getClass().getName() + ":" + t.getMessage());
                        } finally {
                            channel.closeFuture().removeListener(listener);
                            channel.pipeline().remove("sign_handler");
                            viewers.remove(uuid);
                        }
                    }
                } finally {
                    out.add(packet);
                }
            }
        });
        channel.flush();
    }
}