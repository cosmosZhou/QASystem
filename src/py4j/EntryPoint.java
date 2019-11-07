package py4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.robot.syntax.SyntacticTree;
import com.robot.util.Instance;
import com.util.Utility;

//import py4j.GatewayServer;

public class EntryPoint {
	public interface Callback {
		void notify(String note);
	}

	public EntryPoint() {
	}

	public static EntryPoint instance = new EntryPoint();

	public void notify(String note) {
		if (callback == null) {
			System.out.println("Callback is not yet registered");
		} else
			callback.notify(note);
	}

	public Callback callback;

	public void register(Callback listener) {
		callback = listener;
		callback.notify("registering Callback is invoked");
	}

	public static void main(String[] args) throws IOException {
//		GatewayServer gatewayServer = new GatewayServer(instance);
//		gatewayServer.start(true);
////				gatewayServer.start();
//		System.out.println("Gateway Server Started");
//		while (true) {
//			String line = Utility.readFromStdin().readLine();
//			switch (line) {
//			case "python":
//			case "":
//				instance.notify("switching to python console!");
//				break;
//			}
//		}

	}

}