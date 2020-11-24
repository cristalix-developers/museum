package museum.data.model;

import museum.data.Binding;

public interface BindableModel extends Model {

	Binding getBinding();

	void setBinding(Binding binding);

}
