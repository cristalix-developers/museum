package ru.func.museum.packages;

import lombok.*;
import ru.cristalix.core.network.CorePackage;
import ru.func.museum.data.UserInfo;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@RequiredArgsConstructor
public class UserInfoPackage extends CorePackage {

    // request
    private final UUID uuid;

    // response
    private UserInfo userInfo;

}
