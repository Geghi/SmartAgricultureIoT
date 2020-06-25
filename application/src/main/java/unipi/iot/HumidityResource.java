package unipi.iot;

import java.util.ArrayList;

public class HumidityResource extends Resource {
	private ArrayList<String> humidityValues = new ArrayList<String>();

	public HumidityResource(String path, String address) {
		super(path, address);
	}

	public ArrayList<String> getHumidityValues() {
		return this.humidityValues;
	}

	public void setHumidityValues(ArrayList<String> list) {
		int valuesLimit = 5;
		if (list.size() > valuesLimit)
			list.remove(0);
		this.humidityValues = list;
	}
}
