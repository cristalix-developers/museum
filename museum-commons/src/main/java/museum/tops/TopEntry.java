package museum.tops;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.io.Serializable;

@Data
@RequiredArgsConstructor
@EqualsAndHashCode
public class TopEntry<K, V> implements Serializable {

    private final K key;
    private final V value;

}
