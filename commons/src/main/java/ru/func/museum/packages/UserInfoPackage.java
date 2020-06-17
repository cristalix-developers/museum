package ru.func.museum.packages;

import lombok.*;
import ru.cristalix.core.network.CorePackage;

import java.util.UUID;

@EqualsAndHashCode(callSuper = false)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoPackage extends CorePackage {

    // request
    private UUID user;

    // response
    private int coins;

}
