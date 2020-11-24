package museum.data.model;

import lombok.Data;
import museum.data.Binding;

import java.util.UUID;

@Data
public class CollectorModel implements BindableModel {

	private final UUID uuid;
	private final String address;
	private Binding binding;

}
