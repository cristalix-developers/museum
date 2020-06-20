package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class GreetingPackage extends MuseumPackage {

    // request
    private final String password;
    private final String serverName;

    // no response

}
