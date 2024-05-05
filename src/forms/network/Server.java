package forms.network;

import forms.managers.FileManager;
        
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;

import java.io.File;
import java.io.IOException;

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
     * Starts the server by binding it to a specified port.
     * @return The port number to which the server is bound, or -1 if the user cancels the operation.
     * @throws RemoteException if there is an RMI communication error.
     * @throws MalformedURLException if the URL is invalid.
     */
    public int start() throws RemoteException, MalformedURLException {
    	JSpinner spinner = new JSpinner(new SpinnerNumberModel(1099, 1024, 65535, 1));
        
        int option = JOptionPane.showOptionDialog(null, spinner, "Server port number ? (1024-65535)", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);
        
        if (option == JOptionPane.OK_OPTION) {
            try {
            	int port = (int) spinner.getValue();
            	
            	Registry registry = LocateRegistry.createRegistry(port);
                registry.rebind("Server", this);
                
                isConnected = true;
                
                return port;
            } finally {}
        } else {
        	return -1;
        }
    }
    
    /**
     * Stops the server and releases the port.
     * @throws RemoteException if there is an RMI communication error.
     */
    public void stop() throws RemoteException {
        try {
            UnicastRemoteObject.unexportObject(this, true); // Unexport the server object to remove it from the RMI runtime

            isConnected = false;
        } finally {}
    }

    
    /** Checks if the server is connected.
     * @return true if the server is connected, otherwise false.
     */
    public boolean isConnected() {
    	return isConnected;
    }
    
    /** Saves a file on the server.
     * @throws RemoteException if there is an RMI communication error.
     * @throws IOException if an I/O error occurs during file writing.
     */
    public void saveFile() throws RemoteException, IOException  {
        fileManager.writeFile("RMIfile1");
    }

    /**
     * Retrieves the saved file from the server.
     * @return The saved file from the server.
     * @throws RemoteException if a remote communication error occurs.
     */
    public File getSavedFile() throws RemoteException {
    	return new File("RMIfile1");
    }

}