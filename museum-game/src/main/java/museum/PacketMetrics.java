package museum;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.util.AttributeKey;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import net.minecraft.server.v1_12_R1.EnumProtocol;
import net.minecraft.server.v1_12_R1.Packet;

import java.util.concurrent.atomic.LongAdder;

@UtilityClass
public class PacketMetrics {

	private final AttributeKey<Integer> DECOMPRESS = AttributeKey.newInstance("metric-decompressed-bytes");
	private final AttributeKey<Integer> COMPRESS_ID = AttributeKey.newInstance("metric-compress-id");
	public final Reference2ReferenceMap<Class<? extends Packet<?>>, PacketMetric> METRICS = new Reference2ReferenceOpenHashMap();

	public static void inject(Channel channel) {
		/*channel.pipeline()
				.addBefore("compress", "metric-compressor", new MessageToMessageEncoder<ByteBuf>() {
					@Override
					protected void encode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
						val id = channel.attr(COMPRESS_ID).get();
						val state = channel.attr(NetworkManager.c).get();
						val packet = state.a(EnumProtocolDirection.SERVERBOUND, id).getClass();
						val metric = METRICS.get(packet);
						metric.compressedBytes.add(msg.writerIndex());
						out.add(msg.retain());
					}
				})
				.addBefore("decompress", "metric-decompressor", new ByteToMessageDecoder() {
					@Override
					protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
						channel.attr(DECOMPRESS).set(in.readableBytes());
						out.add(in.retain());
					}
				})
				.addAfter("splitter", "metric-decoder", new ByteToMessageDecoder() {

					@Override
					protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
						in.markReaderIndex();

						val id = readVarInt(in);
						in.resetReaderIndex();
						val state = channel.attr(NetworkManager.c).get();
						val packet = state.a(EnumProtocolDirection.SERVERBOUND, id).getClass();
						val metric = METRICS.get(packet);
						metric.received.increment();
						metric.receivedBytes.add(in.readableBytes());
						metric.decompressedBytes.add(channel.attr(DECOMPRESS).get());
						out.add(in.retain());
					}
				})
				.addBefore("prepender", "metric-encoder", new MessageToMessageDecoder<ByteBuf>() {
					@Override
					protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) throws Exception {
						msg.markReaderIndex();
						val id = readVarInt(msg);
						msg.resetReaderIndex();
						val state = channel.attr(NetworkManager.c).get();
						channel.attr(COMPRESS_ID).set(id);
						val packet = state.a(EnumProtocolDirection.CLIENTBOUND, id).getClass();
						val metric = METRICS.get(packet);
						metric.sent.increment();
						metric.sentBytes.add(msg.writerIndex());
						out.add(msg.retain());
					}
				});*/
	}

	static {
		for (Class<? extends Packet<?>> packet : EnumProtocol.f.keySet()) {
			METRICS.put(packet, new PacketMetric());
		}
	}

	private int readVarInt(ByteBuf buff) {
		int i = 0;
		int j = 0;

		byte b0;
		do {
			b0 = buff.readByte();
			i |= (b0 & 127) << j++ * 7;
			if (j > 5) {
				throw new RuntimeException("VarInt too big");
			}
		} while((b0 & 128) == 128);

		return i;
	}

	@FieldDefaults(level = AccessLevel.PUBLIC, makeFinal = true)
	public static final class PacketMetric {
		LongAdder received = new LongAdder();
		LongAdder receivedBytes = new LongAdder();
		LongAdder sent = new LongAdder();
		LongAdder sentBytes = new LongAdder();
		LongAdder decompressedBytes = new LongAdder();
		LongAdder compressedBytes = new LongAdder();
	}

}