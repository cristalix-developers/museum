package museum.packages;

import lombok.*;

import java.io.Serializable;
import java.util.Map;

/**
 * @author func 29.10.2020
 * @project museum
 */
@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class MuseumMetricsPackage extends MuseumPackage {

	private final String serverName;
	private final int online;
	private final double tps;
	private final long freeMemory;
	private final long allocatedMemory;
	private final long totalMemory;
	private final Map<String, PacketMetric> metrics;

	@Setter
	@Getter
	@AllArgsConstructor
	@RequiredArgsConstructor
	public static class PacketMetric implements Serializable, Cloneable {
		private long received;
		private long receivedBytes;
		private long sent;
		private long sentBytes;
		private long decompressedBytes;
		private long compressedBytes;

		@Override
		public PacketMetric clone() {
			return new PacketMetric(
					received,
					receivedBytes,
					sent,
					sentBytes,
					decompressedBytes,
					compressedBytes
			);
		}
	}
}
