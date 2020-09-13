package museum.tops;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class TopEntry<K, V> {

    private final K key;
    private final V value;

}
