package museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import museum.tops.PlayerTopEntry;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class TopPackage extends MuseumPackage {

    // request
    private final TopType topType;
    private final int limit;

    // response
    private List<PlayerTopEntry<Object>> entries;

    public enum TopType {

        MONEY,
        INCOME,
        EXPERIENCE,;

    }

}
