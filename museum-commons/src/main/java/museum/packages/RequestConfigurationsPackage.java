package museum.packages;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
@NoArgsConstructor
public class RequestConfigurationsPackage extends MuseumPackage {

    // no request

    // no response
    private String configData, guisData, itemsData;

}
