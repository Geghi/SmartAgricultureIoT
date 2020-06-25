package unipi.iot;

public class IrrigationResource extends Resource {
	private Boolean state;

	public IrrigationResource(String path, String address) {
		super(path, address);
		state = false;
	}

	public Boolean getState() {
		return state;
	}

	public void setState(Boolean state) {
		this.state = state;
	}
}
