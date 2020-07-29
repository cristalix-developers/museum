package ru.cristalix.core.hub;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.server.v1_12_R1.PacketDataSerializer;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import ru.cristalix.core.nbt.NbtSupport;

@Getter
public class SimpleBukkitHubItem extends HubItem {

	private byte[] encodedData;

	public SimpleBukkitHubItem(String realmType, Material material, int data, String... description) {
		super(realmType, 0, null, description, material.getId(), (short) data);
	}

	public SimpleBukkitHubItem(HubItem original) {
		super(original.getRealmType(), original.getPosition(), original.getNbt(), original.getDescription(), original.getType(), original.getData());

		ItemStack stack = new ItemStack(this.getType(), 1, this.getData());
		NbtSupport.support().setNbt(stack, this.getNbt());
		ByteBuf buffer = Unpooled.buffer();
		new PacketDataSerializer(buffer).a(CraftItemStack.asNMSCopy(stack));
		this.encodedData = new byte[buffer.readableBytes()];
		buffer.readBytes(this.encodedData);

	}

}