package unipi.iot;

public class Resource {
	private String path;
	private String address;

	public Resource(String path, String address) {
		this.path = path;
		this.address = address;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getResourceURI() {
		return "coap://[" + this.address + "]:5683/" + this.path;
	}
}
