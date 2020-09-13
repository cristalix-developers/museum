package museum.util;

import net.minecraft.server.v1_12_R1.BlockPosition;
import net.minecraft.server.v1_12_R1.Chunk;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.EnumSkyBlock;
import net.minecraft.server.v1_12_R1.IBlockData;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutMapChunk;
import net.minecraft.server.v1_12_R1.PlayerChunk;
import net.minecraft.server.v1_12_R1.TileEntity;
import net.minecraft.server.v1_12_R1.World;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;

import java.util.ArrayList;
import java.util.Collection;

public class FastBlockChanger {

	boolean enabled = true;
	public ArrayList<ChunkUpdater> chunks = new ArrayList<>(64);
	// SmoothLightning (From MC source code)
	final byte sLow = -2;
	final byte sHigh = (byte) (Math.abs(sLow) + 1);

	public boolean isChunkSendable(int x, int z) {
		boolean r = true;
		for (ChunkUpdater c : chunks)
			if (c.x == x && c.z == z && !c.isFBCPacket) r = false;
		return r;
	}

	// Get ChunkUpdater from bukkit's world
	public ChunkUpdater getChunk(org.bukkit.World cw, int cx, int cz, boolean updateIfUnchanged) {
		return getChunk(((CraftWorld) cw).getHandle(), cx, cz, updateIfUnchanged);
	}

	// Get a chunk updater from NMS world
	public ChunkUpdater getChunk(net.minecraft.server.v1_12_R1.World cw, int cx, int cz, boolean updateIfUnchanged) {
		this.enabled = true; // Wake up the task if needed
		for (ChunkUpdater c : chunks)
			if (c.x == cx && c.z == cz && c.w == cw) return c;
		if (!cw.areChunksLoaded(new BlockPosition(cx << 4, 0, cz << 4), 1))
			return new ChunkUpdater(cw, cx, cz, null, updateIfUnchanged); // Prevent StackOverflow
		Chunk gch = cw.getChunkIfLoaded(cx, cz);
		if (gch.isDone()) return new ChunkUpdater(cw, cx, cz, gch, updateIfUnchanged);
		else return new ChunkUpdater(cw, cx, cz, null, updateIfUnchanged);
	}

	public void runTask() {
		int i = chunks.size();
		if (i == 0) {
			this.enabled = false; // Nothing left to do, stopping
			return;
		}
		long end = System.currentTimeMillis();
		long done = end - 3000; // Wait 3 seconds for block changes finish before resending
		end += 10; // Timeout
		if (i > 3) i = 3; // Max 3 chunks
		ChunkUpdater chunkUpdater;
		//Chunk ch;
		while (System.currentTimeMillis() < end && --i > -1) // For safe chunk removing (Iterate backward)
		{
			chunkUpdater = chunks.get(i);
			if (chunkUpdater.lastModified < done) // Forced
			{
				if (chunkUpdater.nChanged > 0 || chunkUpdater.updateIfUnchanged) {
					if (chunkUpdater.chunk != null) {
						chunkUpdater.recalculateLighting();
						//ch = chunkUpdater.ch;
						sendToPlayers(chunkUpdater);
						chunkUpdater.isUpdated = true;
						chunks.remove(i); // Remove chunk updater, without full reset
					}
				} else if (chunkUpdater.nChanged == 0) chunks.remove(i);
			}
		}
	}

	private short sendToPlayers(ChunkUpdater cu) {
		if (cu.w.getWorld().unloadChunk(cu.x, cu.z, true, true)) // nobody there? dont bother, save memory.
			return 0;
		byte n = 0;
		cu.isFBCPacket = true;
		//        byte d = (byte) (cu.chunk.world.spigotConfig.viewDistance*1.75f);
		Collection<TileEntity> lti = cu.chunk.tileEntities.values();
		//        d*=d;
		PacketPlayOutMapChunk pmc = new PacketPlayOutMapChunk(cu.chunk, 65535);
		// TODO: Outdated
		//PacketPlayOutUnloadChunk puc = new PacketPlayOutUnloadChunk(cu.x,cu.z);
		ArrayList<Packet<?>> te = new ArrayList<>(16);
		for (TileEntity ti : lti) {
			Packet<?> p = ti.getUpdatePacket();
			if (p != null) te.add(p);
		}
		// Not using view distance because of randomness (Chunk is kept loaded?)
		PlayerChunk pc = ((net.minecraft.server.v1_12_R1.WorldServer) (cu.w)).getPlayerChunkMap().getChunk(cu.x, cu.z);
		assert pc != null;
		ArrayList<EntityPlayer> pl = new ArrayList<>(pc.c.size());
		pl.addAll(pc.c);
		if (pl.size() == 0) {
			// TODO: Outdated (Auto chunk unload)
			//cu.w.getWorld().unloadChunk(cu.x, cu.z, true, false);
			System.err.println("No player in range to send " + cu.x + "," + cu.z + "  chunk unloaded=" + (cu.w.getWorld().unloadChunk(cu.x, cu.z, true, false)));
		} else for (EntityPlayer p : pl) {
			p.playerConnection.sendPacket(pmc); // Update chunk
			for (Packet<?> packet : te)
				p.playerConnection.sendPacket(packet); // Update TileEntity[]
		}
		cu.isFBCPacket = false;
		return n;
	}

	public class ChunkUpdater {

		private short sections = 0;
		public final int x, z; // Location
		public final World w;
		public Chunk chunk; // Chunk to be updated
		public long lastModified;
		private boolean updateIfUnchanged; // Force update
		private int nChanged; // Count of changed blocks
		private boolean isUpdated = false;
		public boolean isFBCPacket = false; // Block chunk update packet
		//                                x   z   y
		private long[][][] bits = new long[16][16][4]; // One verticle

		private ChunkUpdater(World w, int x, int z, Chunk ch, boolean update) {
			this.updateIfUnchanged = update;
			this.nChanged = 0;
			this.chunk = ch;
			this.w = w;
			this.x = x;
			this.z = z;
			this.lastModified = System.currentTimeMillis() + 30000; // Idle reset
			chunks.add(this);
		}

		public void setBlock(BlockPosition b, IBlockData i) // Low level set
		{
			if (this.chunk == null) {
				this.chunk = this.w.getChunkIfLoaded(x, z);
				if (this.chunk == null) return;
			}
			int x = (b.getX() % 16 + 16) % 16;
			int z = (b.getZ() % 16 + 16) % 16;
			if (x > 15 || x < 0 || z > 15 || z < 0 || b.getY() > 255 || b.getY() < 0)
				return; // Set block outside of chunk TODO: Throw?
			if (!chunk.isDone()) {
				System.err.println(String.format("Unfinished chunk at %d, %d. Loaded: %s", this.x, this.z, chunk.bukkitChunk.isLoaded()));
			}
			if (!chunk.bukkitChunk.isLoaded()) {
				boolean l = chunk.bukkitChunk.load(false);
				System.err.println(String.format("Unloaded chunk at %d, %d. Loaded: %s %n", this.x, this.z, l));
			}
			if (isUpdated) // Needs to be reset?
			{
				sections = 0;
				enabled = true; // Wake up task
				isUpdated = false;
				bits = new long[16][16][4];
				chunks.add(this); // Re-add after updating and removing
				lastModified = System.currentTimeMillis() + 30000; // 30s
			} else lastModified = System.currentTimeMillis();
			// Not using material because of lags

			/*IBlockData iBlockData=*/
			chunk.a(b, i); // Fast set block
			++nChanged;
			sections |= 1 << (b.getY() >> 4); // Modified y sections
			long nb = ((long) 1 << (b.getY() % 64));
			bits[x][z][b.getY() / 64] |= nb; // Force relight
			// System.out.println("Set bit "+Long.toBinaryString(cb)+" at "+((b.getX()%16 + 16)%16)+","+((b.getZ()%16+16)%16)+","+(b.getY()/64)+" to "+Long.toBinaryString(nb)+" ("+Long.toBinaryString(cb|nb)+")");
		}

		private void relight(BlockPosition bp) {
			w.c(EnumSkyBlock.SKY, bp);
			w.c(EnumSkyBlock.BLOCK, bp);
		}

		private void finalizeRelightBlocks() {
			long[][][] newbits = new long[16][16][4];
			int bx, bz, by, by64, px = x << 4, pz = z << 4;
			long bb;
			byte l;
			boolean isTransparent = false;
			// Set relight bit where needed
			for (bx = 0; bx < 16; ++bx)
				for (bz = 0; bz < 16; ++bz)
					for (isTransparent = false, by = 3; by > -1; --by) {
						l = 64;
						by64 = by * 64;
						bb = bits[bx][bz][by];
						while (--l > -1) {
							if (isTransparent) // If air bellow
							{
								if (w.getType(new BlockPosition(px + bx, l + by64, pz + bz)).getMaterial().isSolid()) {
									newbits[bx][bz][by] |= ((long) 1 << by64); // Relight
									isTransparent = false;
								}
							}
							if ((bb & (1L << l)) > 0) {
								isTransparent = true;
								newbits[bx][bz][by] |= ((long) 1 << by64); // Relight
							}
						}
					}
			// Add newbits to bits
			for (bx = 0; bx < 16; ++bx)
				for (bz = 0; bz < 16; ++bz)
					for (isTransparent = false, by = 3; by > -1; --by)
						bb = bits[bx][bz][by] | newbits[bx][bz][by]; // Update all bits TODO: ...
		}

		private void recalculateLighting() {
			// Fix lightning
			finalizeRelightBlocks();
			int bx, bz, by, by64, px = x << 4, pz = z << 4;
			long bb;
			byte l;
			BlockPosition bp;
			BlockPosition bpt;
			boolean doRelight;
			int gl, gl1;
			for (bx = 0; bx < 16; ++bx)
				for (bz = 0; bz < 16; ++bz)
					for (by = 3; by > -1; --by) {
						l = 64;
						by64 = by * 64;
						bb = bits[bx][bz][by];
						while (--l > -1) {
							if ((bb & (1L << l)) > 0) {
								bp = new BlockPosition(px + bx, l + by64, pz + bz);
								relight(bp);
								gl = w.getLightLevel(bp);
								int tx, tz;
								for (tx = -sLow; tx < sHigh; ++tx)
									for (tz = sLow; tz < sHigh; ++tz) {
										doRelight = false;
										if (tx != 0 && tz != 0) // If not calculated
										{
											if (!(tx + bx < 0 || tx + bx > 15 || tz + bz < 0 || tz + bz > 15)) // Inside chunk
												if ((bits[tx + bx][tz + bz][by] & (1L << l)) == 0) // If not being relighted
													doRelight = true;
												else doRelight = true;
										}
										if (doRelight) {
											bpt = bp.east(tx).north(tz);
											gl1 = w.getLightLevel(bpt);
											if (Math.abs(gl - gl1) > 3) // Relight if diff is big
											{
												relight(bpt);
											}
										}
									}
							}
						}
					}
			isUpdated = true; // Inactive
		}

	}

}