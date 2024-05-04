package forms.serverrmi;
        
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server implements ServerInterface {
        
    public Server() {}
    
    public static void main(String[] args) {
        try {
            // Create and export the remote object
            Server obj = new Server();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Ask for a port number from the console
            Scanner scanner = new Scanner(System.in);
            int port;
            do {
                System.out.print("Enter a port number (1024-65535, default is 1099): ");
                while (!scanner.hasNextInt()) {
                    System.out.println("That's not a number!");
                    scanner.next();
                }
                port = scanner.nextInt();
            } while (port < 1024 || port > 65535);
            scanner.close();

            // Start the registry on the entered port and bind the stub to it
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("Server", stub);

            System.out.println("Server ready on port " + port);
			
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
	public static void startServer(int port) throws Exception {
        // Create and export the remote object
        Server obj = new Server();
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

        // Start the registry on the entered port and bind the stub to it
        Registry registry = LocateRegistry.createRegistry(port);
        registry.bind("Server", stub);
		
        // Add a shutdown hook to unbind the remote object and stop the registry
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run(){
                try {
					registry.unbind("SaveDistant");
					UnicastRemoteObject.unexportObject(registry, true);
				} catch (AccessException e) {
					System.err.println("Error stopping server (AccessException): " + e.toString());
				} catch (RemoteException e) {
					System.err.println("Error stopping server (RemoteException): " + e.toString());
				} catch (NotBoundException e) {
					System.err.println("Error stopping server (NotBoundException): " + e.toString());
				}
                System.out.println("Server stopped");
            }
        });
        
        System.out.println("Server ready on port " + port);
	}
    
    public void saveDistant(String[] strings) throws RemoteException  {
        try {
            String filename = "shapeEditor-main/Sauvegarde3/ServerFiles/RMIfile1";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
                for (String str : strings) {
                    writer.write(str);
                    writer.newLine();
                }
            }
        } catch (IOException e){
            throw new RemoteException("Error saving file", e);
        }
    }

    public String[] loadDistant() throws RemoteException {
        try {
            String filename = "shapeEditor-main/Sauvegarde3/ServerFiles/RMIfile1";
            List<String> lines = new ArrayList<>();

            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            }

            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RemoteException("Error loading file", e);
        }
    }

}