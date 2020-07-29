package museum.data;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class SkeletonInfo implements Info {

	private final String prototypeAddress;
	private List<String> unlockedFragmentAddresses;

	public SkeletonInfo(String prototypeAddress) {
	    this.prototypeAddress = prototypeAddress;
	    this.unlockedFragmentAddresses = new ArrayList<>();
    }

}
