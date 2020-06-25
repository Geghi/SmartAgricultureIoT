package unipi.iot;

import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class CoapObserverClient extends CoapClient {
	private HumidityResource humidityResource;
	CoapObserveRelation coapObserveRelation;

	public CoapObserverClient(HumidityResource humidityResource) {
		super(humidityResource.getResourceURI());
		this.humidityResource = humidityResource;
	}

	public void startObserving() {
		coapObserveRelation = this.observe(new CoapHandler () {
			public void onLoad(CoapResponse response) {
				try {
					String value;
					JSONObject jo = (JSONObject) JSONValue.parseWithException(response.getResponseText());
					Integer lowerThreshold = 10, upperThreshold = 30, index;
	
					if (jo.containsKey("humidity")) {
						value = jo.get("humidity").toString();
						Integer numericValue = Integer.parseInt(value.trim());
	
						if (numericValue < lowerThreshold) {
							index = MainApp.humidityResources.indexOf(humidityResource);
							IrrigationResource irrigationResource = MainApp.irrigationResources.get(index);
							Boolean state = irrigationResource.getState();
							if (!state)
								irrigationResource.setState(true);
						}
	
						if (numericValue > upperThreshold) {
							index = MainApp.humidityResources.indexOf(humidityResource);
							IrrigationResource irrigationResource = MainApp.irrigationResources.get(index);
							Boolean state = irrigationResource.getState();
							if (state) 
								irrigationResource.setState(false);							
						}
	
					} else {
						System.out.println("Humidity value not found.");
						return;
					}
	
					ArrayList<String> resourceValues = humidityResource.getHumidityValues();
					resourceValues.add(value);
					MainApp.humidityResources.get(MainApp.humidityResources.indexOf(humidityResource))
							.setHumidityValues(resourceValues);
	
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
	
			public void onError() {
				System.out.println("Observing Error Detected.");
			}
		});
	}

	public HumidityResource getHumidityResource() {
		return humidityResource;
	}

	public void setHumidityResource(HumidityResource humidityResource) {
		this.humidityResource = humidityResource;
	};
}
