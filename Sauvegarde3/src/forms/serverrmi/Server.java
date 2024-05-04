package forms.serverrmi;
        
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
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
    
	static Registry servRegistry = null;
	
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
            servRegistry = registry;
            servRegistry.bind("serverFile", stub);

            System.out.println("Server ready on port " + port);
			
        } catch (Exception e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
    public static void startServer(int port) throws Exception, RemoteException {
        // Create and export the remote object
        Server obj = new Server();
        ServerInterface stub = (ServerInterface) UnicastRemoteObject.exportObject(obj, 0);

        // Start the registry on the entered port and bind the stub to it
        Registry registry = LocateRegistry.createRegistry(port);
        servRegistry = registry;
        servRegistry.bind("serverFile", stub);
        
        System.out.println("Server ready on port " + port);
	}
    
	@Override
    public void saveDistant(String[] strings) throws RemoteException, IOException  {
        try {
        	String path = "shapeEditor-main/Sauvegarde3/ServerFiles/";
            String filename = "RMIfile1";

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(path + filename))) {
                for (String str : strings) {
                    writer.write(str);
                    writer.newLine();
                }
            } catch (FileNotFoundException e) {
            	throw new IOException("Error finding file", e);
            } catch (IOException e){
            	e.printStackTrace();
                throw new RemoteException("Error saving file", e);
            }
        } catch (RemoteException e){
            e.printStackTrace();
            throw e;
        } 
    }

	@Override
	public String[] loadDistant() throws RemoteException, FileNotFoundException {
	    try {
	        String path = "shapeEditor-main/Sauvegarde3/ServerFiles/";
	        String filename = "RMIfile1";
	        List<String> lines = new ArrayList<>();

	        try (BufferedReader reader = new BufferedReader(new FileReader(path + filename))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                lines.add(line);
	            }
	        }
	        
	        return lines.toArray(new String[0]);
	    } catch (FileNotFoundException e) {
	        throw new RemoteException("Error finding file", e);
	    } catch (IOException e) {
	        throw new RemoteException("Error loading file", e);
	    }
	}
    
    public static void close() throws AccessException, RemoteException, NotBoundException {
    	servRegistry.unbind("serverFile");
    	UnicastRemoteObject.unexportObject(servRegistry, true);
    	servRegistry = null;
    	System.out.println("Server stopped");
    }

}