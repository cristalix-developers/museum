package museum.tops;

import lombok.Getter;
import lombok.Setter;
import museum.data.UserInfo;

@Setter
@Getter
public class PlayerTopEntry<V> extends TopEntry<UserInfo, V> {

    private String userName;
    private String displayName;

    public PlayerTopEntry(UserInfo userInfo, V value) {
        super(userInfo, value);
    }

}
