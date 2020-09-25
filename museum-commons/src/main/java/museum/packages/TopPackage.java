package museum.packages;

import lombok.*;
import museum.data.UserInfo;
import museum.tops.TopEntry;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class TopPackage extends MuseumPackage {

    // request
    private final TopType topType;
    private final int limit;

    // response
    private List<TopEntry<UserInfo, Object>> entries;

    public enum TopType {

        MONEY,
        INCOME,
        EXPERIENCE,;

    }

}
