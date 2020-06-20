package ru.cristalix.museum.packages;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class BulkSaveUserPackage extends MuseumPackage {

    // request
    private final List<SaveUserPackage> packages;

    // no response

}
