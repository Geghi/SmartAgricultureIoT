package unipi.iot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class MainApp {

	public static ArrayList<CoapObserverClient> coapObserverClients = new ArrayList<CoapObserverClient>();
	public static ArrayList<HumidityResource> humidityResources = new ArrayList<HumidityResource>();
	public static ArrayList<IrrigationResource> irrigationResources = new ArrayList<IrrigationResource>();

	public static void main(String[] args) throws IOException, InterruptedException {

		runServer();
		showOperations();

		while (true) {
			try {
				Integer selectedOperation = insertInputLine();
				Integer index;

				switch (selectedOperation) {
				case 0:
					showResources();
					break;
				case 1:
					if ((index = getNodeFromId()) != null)
						changeIrrigationState("ON", irrigationResources.get(index), true);
					break;
				case 2:
					if ((index = getNodeFromId()) != null)
						changeIrrigationState("OFF", irrigationResources.get(index), false);
					break;
				case 3:
					showResourcesInformation();
					break;
				case 4:
					if ((index = getNodeFromId()) != null) {
						String value = getLastHumidityValue(humidityResources.get(index));
						System.out.println("Last value registered: " + value);
					}
					break;
				case 5:
					if ((index = getNodeFromId()) != null)
						showSingleResourceInformation(index);
					break;
				case 6:
					System.exit(0);
					break;
				default:
					showOperations();
					break;
				}

			} catch (Exception e) {
				System.out.println("Invalid input. Try Again\n");
				showOperations();
				e.printStackTrace();
			}
		}
	}

	public static void runServer() {
		new Thread() {
			public void run() {
				Server server = new Server();
				server.startServer();
			}
		}.start();
	}

	public static Integer getNodeFromId() {
		System.out.print("Insert the node id: ");
		Integer index = insertInputLine();
		System.out.println();
		if (index == -1)
			return null;
		if (humidityResources.size() > index)
			return index;
		System.out.println("The selected node does not exists.");
		return null;
	}

	public static Integer insertInputLine() {
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			String line = bufferedReader.readLine();
			Integer value = -1;
			if (isNumeric(line))
				value = Integer.parseInt(line);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null)
			return false;
		try {
			@SuppressWarnings("unused")
			Integer number = Integer.parseInt(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}

	public static void showResources() {
		System.out.println("Resources List:");
		for (int i = 0; i < humidityResources.size(); i++) {
			HumidityResource humidityResource = humidityResources.get(i);
			IrrigationResource irrigationResource = irrigationResources.get(i);
			System.out.println(
					+i + "\tHumidity Resource: " + humidityResource.getAddress() + " " + humidityResource.getPath());
			System.out.println(+i + "\tIrrigation Resource: " + irrigationResource.getAddress() + " "
					+ irrigationResource.getPath() + "\n");
		}
	}

	public static void showResourcesInformation() {
		System.out.println("Rerouce Information: \n");
		for (int i = 0; i < humidityResources.size(); ++i) {
			showSingleResourceInformation(i);
			System.out.println("\n -------------------------------------------- \n");
		}
	}

	public static void changeIrrigationState(String state, IrrigationResource irrigationResource, Boolean value) {
		CoapClient client = new CoapClient(irrigationResource.getResourceURI());
		CoapResponse response = client.post("state=" + state, MediaTypeRegistry.TEXT_PLAIN);
		String code = response.getCode().toString();
		if (!code.startsWith("2")) {
			System.out.println("Error: " + code);
			return;
		}
		irrigationResource.setState(value);
		System.out.println("Irrigation state changed to: " + state);
	}

	public static String getLastHumidityValue(HumidityResource humidityResource) {
		ArrayList<String> list = humidityResource.getHumidityValues();
		if (list.isEmpty())
			return "N/A";
		return list.get(list.size() - 1);
	}

	public static void showSingleResourceInformation(Integer index) {
		IrrigationResource irrigationResource = irrigationResources.get(index);
		String stateValue = irrigationResource.getState() ? "ON" : "OFF";
		System.out.println(index + "\t" + irrigationResource.getAddress() + " " + irrigationResource.getPath()
				+ "\n\t\tState: " + stateValue + "\n");

		HumidityResource humidityResource = humidityResources.get(index);
		System.out.println(index + "\t" + humidityResource.getAddress() + " " + humidityResource.getPath());
		ArrayList<String> list = humidityResource.getHumidityValues();
		for (int j = 0; j < list.size(); ++j)
			System.out.println("\t\tId: " + j + "\tValue: " + list.get(j));
	}

	public static void showOperations() {
		System.out.println("Commands List:");
		System.out.println("0: show resources");
		System.out.println("1: start irrigation");
		System.out.println("2: stop irrigation");
		System.out.println("3: nodes status");
		System.out.println("4: show last humidity value");
		System.out.println("5: show single resource");
		System.out.println("6: exit");
	}
}
