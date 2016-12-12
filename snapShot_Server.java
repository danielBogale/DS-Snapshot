package snapShot;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class snapShot_Server {
	public static void main(String[] args) {
		try {
			Registry reg = LocateRegistry.createRegistry(1199);
			snapShot_Implementation sn = new snapShot_Implementation();
			reg.rebind("mySnap", sn);
			System.out.println("Server is ready . . .");
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		
	}

}
