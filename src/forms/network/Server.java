package forms.network;

import forms.managers.FileManager;
        
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Server extends UnicastRemoteObject implements RemoteServer {
	
	private static final long serialVersionUID = 1L;
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private transient FileManager fileManager;
	private boolean isConnected = false;
	private List<Integer> ports = new ArrayList<>();
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
        
	/** Constructs a Server object with the specified FileManager.
     * @param fileManager The associated file manager.
     * @throws RemoteException if there is an RMI communication error.
     */
    public Server(FileManager fileManager) throws RemoteException {
    	this.fileManager = fileManager;
    }
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/
    
    /**
     * Gets the list of ports used by a server.
     * @return The list of ports used by a server.
     */
    public List<Integer> getPorts() throws RemoteException {
    	return ports;
    }
    
    /**
     * Allows the user to choose a server and get its port.
     * @return The port of the chosen server.
     */
    public int choosePort() {
		Integer[] portArray = ports.toArray(new Integer[0]);

        Integer port = (Integer) JOptionPane.showInputDialog(
            null, 
            "Choose a server :", 
            "Server choice", 
            JOptionPane.PLAIN_MESSAGE, 
            null, 
            portArray, 
            portArray[0]
        );
        
        return port;
    }
    
    /**
     * Starts the server by binding it to a specified port.
     * @return The port number to which the server is bound, or -1 if the user cancels the operation, or -2 if a server is already started on this port.
     * @throws RemoteException if there is an RMI communication error.
     * @throws MalformedURLException if the URL is invalid.
     * @throws NotBoundException 
     * @throws BindException 
     */
    public int start(int clientPort) throws RemoteException, MalformedURLException, NotBoundException, BindException {
    	JSpinner spinner = new JSpinner(new SpinnerNumberModel(1099, 1024, 65535, 1));
        
        int option = JOptionPane.showOptionDialog(null, spinner, "Server port number ? (1024-65535)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
            	int port = (int) spinner.getValue();
            	
            	if (port == clientPort) {
        	    	throw new BindException("Port already used by the client.");
        	    }
            	
            	if (!ports.contains(port)) {
            		Registry registry = LocateRegistry.createRegistry(port);
                    registry.rebind("Server", this);
                    
                    isConnected = true;
                    
                    ports.add(port);
                    return port;
            	} else {
            		return -2;
            	}
            	
            } finally {}
        } else {
        	return -1;
        }
    }
    
    /**
     * Stops the server and releases the ports.
     * @throws RemoteException if there is an RMI communication error.
     */
    public void stop() throws RemoteException {
		try {
            UnicastRemoteObject.unexportObject(this, true); // Unexport the server object to remove it from the RMI runtime
            ports.clear();

            isConnected = false;
        } finally {}
    }

    
    /** Checks if the server is connected.
     * @return true if the server is connected, otherwise false.
     */
    public boolean isConnected() {
    	return isConnected;
    }
    
    /** Saves a file on the server started on the specified port.
     * @param port The port of the server on which to save the file.
     * @throws RemoteException if there is an RMI communication error.
     * @throws IOException if an I/O error occurs during file writing.
     */
    public void saveFile(int port) throws RemoteException, IOException  {
        fileManager.writeFile("files/RMIfile" + port);
    }

    /**
     * Retrieves the content of the saved file as a string.
     * @param port The port number used to construct the file name.
     * @return A string containing the content of the saved file.
     * @throws RemoteException If a remote communication error occurs.
     * @throws IOException If an I/O error occurs while retrieving the file content.
     */
    public String getSavedFile(int port) throws RemoteException, IOException {
    	return fileManager.fileToString("files/RMIfile" + port);
    	
    }

}