package forms.serverrmi;
        
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.Socket;
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
            
            /*
            // Start the registry on the default port and bind the stub to it
            Registry registry = LocateRegistry.createRegistry(1099);
            registry.bind("ServerInterface", stub);
			*/
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
	public static void startServer(int port) {
        try {
            // Create and export the remote object
            Server obj = new Server();
            ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

            // Start the registry on the entered port and bind the stub to it
            Registry registry = LocateRegistry.createRegistry(port);
            registry.bind("Server", stub);
			
	        // Add a shutdown hook to unbind the remote object and stop the registry
	        Runtime.getRuntime().addShutdownHook(new Thread() {
	            public void run() {
	                try {
	                    registry.unbind("SaveDistant");
	                    UnicastRemoteObject.unexportObject(registry, true);
	                    System.out.println("Server stopped");
	                } catch (Exception e) {
	                    System.err.println("Error stopping server: " + e.toString());
	                }
	            }
	        });
	        
            System.out.println("Server ready on port " + port);
			
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }		
	}
    
/*
    @Override
    public void saveDistant(String[] strings) throws RemoteException  {
    	try {
    		String filename = "test_rmi_save";
    		// Serialization
    		FileOutputStream file = new FileOutputStream(filename);
    		ObjectOutputStream out = new ObjectOutputStream(file);
    		for (String str : strings) { // Save each object in the file
    			out.writeObject(str);
    		}

    		out.close();
    		file.close();
    		
    		
    		}  catch (Exception e){
    		throw new RemoteException("Error saving file", e);
    	}
    }
    
    @Override
    public String[] loadDistant() throws RemoteException {
        try {
            String filename = "test_rmi_save";
            List<String> lines = new ArrayList<>();

            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
                Object obj;
                while ((obj = in.readObject()) != null) {
                    if (obj instanceof String) {
                        lines.add((String) obj);
                    }
                }
            } catch (EOFException e) {
                // End of file reached, do nothing
            } catch (ClassNotFoundException e) {
                throw new RemoteException("Error loading file", e);
            }

            return lines.toArray(new String[0]);
        } catch (IOException e) {
            throw new RemoteException("Error loading file", e);
        }
    }
*/    
	
    public static boolean isServerRunning(String ip, int port) {
        try (Socket socket = new Socket(ip, port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
	
    public void saveDistant(String[] strings) throws RemoteException  {
        try {
            String filename = "test_rmi_save";

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
            String filename = "test_rmi_save";
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