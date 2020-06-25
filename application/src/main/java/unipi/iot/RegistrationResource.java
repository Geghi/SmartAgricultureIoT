package unipi.iot;

import java.net.InetAddress;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.server.resources.CoapExchange;

public class RegistrationResource extends CoapResource {

	public RegistrationResource(String name) {
		super(name);
	}

	public void handleGET(CoapExchange exchange) {
		exchange.accept();

		InetAddress inetAddress = exchange.getSourceAddress();
		CoapClient client = new CoapClient("coap://[" + inetAddress.getHostAddress() + "]:5683/.well-known/core");
		CoapResponse response = client.get();
		
		String code = response.getCode().toString();
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}

		String responseText = response.getResponseText();
		Integer startIndex = 0, endIndex;

		while (true) {
			startIndex = responseText.indexOf("</");
			if (startIndex == -1)
				break;
			endIndex = responseText.indexOf(">");
			String path = responseText.substring(startIndex + 2, endIndex);
			responseText = responseText.substring(endIndex + 1);

			if (path.contains("res_humidity")) {
				HumidityResource humidityResource = new HumidityResource(path, inetAddress.getHostAddress());
				if (!MainApp.humidityResources.contains(humidityResource)) {
					MainApp.humidityResources.add(humidityResource);
					observeHumidity(humidityResource);
				}
			} else if (path.contains("res_irrigation")) {
				IrrigationResource irrigationResource = new IrrigationResource(path, inetAddress.getHostAddress());
				if (!MainApp.irrigationResources.contains(irrigationResource))
					MainApp.irrigationResources.add(irrigationResource);
			}
		}
	}

	private static void observeHumidity(HumidityResource humidityResource) {
		MainApp.coapObserverClients.add(new CoapObserverClient(humidityResource));
		MainApp.coapObserverClients.get(MainApp.coapObserverClients.size() - 1).startObserving();
	}
}