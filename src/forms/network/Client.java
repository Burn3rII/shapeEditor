package forms.network;

import forms.managers.FileManager;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public class Client {
	
	/***************************************************************************
	 * Attributes.
	 **************************************************************************/
	
	private RemoteServer server;
	private int port = -1;
	private transient FileManager fileManager;
	
	private boolean isConnected = false;
	
	/***************************************************************************
	 * Constructors.
	 **************************************************************************/
       
	/**
     * Constructor for the Client class.
     * @param fileManager The associated file manager.
     */
    public Client(FileManager fileManager) {
    	this.fileManager = fileManager;
    }
    
    /***************************************************************************
	 * Methods.
	 **************************************************************************/
    
    /**
     * Gets the client's port.
     * @return The client's port.
     */
    public int getPort() {
    	return port;
    }
    
    /**
     * Connects the client to the server by allowing the user to enter the server's IP address and port.
     * @return An array containing the IP address and port number to which the client is connected, or an empty array if the user cancels the operation.
     * @throws NotBoundException if the server is not bound in the RMI registry.
     * @throws MalformedURLException if the URL of the server is malformed.
     * @throws RemoteException if an error occurs during communication with the server.
     * @throws BindException 
     */
    public String[] connectToServer(List<Integer> ports) throws NotBoundException, MalformedURLException, RemoteException, BindException {
    	JPanel panel = new JPanel(new GridLayout(2, 2));
    	JTextField ipField = new JTextField("127.0.0.1"); // Default IP address
    	JTextField portField = new JTextField("1099"); // Default port number
    	panel.add(new JLabel("Enter the server IP address (localhost is 127.0.0.1):"));
    	panel.add(ipField);
    	panel.add(new JLabel("Enter the server port number 1024-65535:"));
    	panel.add(portField);
    	
    	int result = JOptionPane.showConfirmDialog(null, panel, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
        
    	if (result == JOptionPane.OK_OPTION) {
    	    String ip = ipField.getText();
    	    String portString = portField.getText();
    	    int portNumber = Integer.parseInt(portString);
    	    
    	    if (ports.contains(portNumber)) {
    	    	throw new BindException("Port already used by one of the servers you started.");
    	    }
    	    
    	    try {
    	    	disconnect(); // Disconnect from any previous server
    	    	
    	    	server = (RemoteServer) Naming.lookup("rmi://" + ip + ":" + portNumber + "/Server");
    	    	
    	    	isConnected = true;
    	    	port = portNumber;
    	    	return new String[] {ip, portString};
    	    } finally {}
    	} else {
    		return new String[] {};
    	}
    }
    
    /**
     * Disconnects the client from the server and releases any associated resources.
     * @throws RemoteException if an error occurs during communication with the server.
     */
    public void disconnect() throws RemoteException {
		try {
            isConnected = false;
            server = null;
        } finally {}
    }
    
    /**
     * Checks if the client is connected to the server.
     * @return true if the client is connected to the server, otherwise false.
     */
    public boolean isConnected() {
    	return isConnected;
    }
    
    /**
     * Restores a file from the server.
     * @throws RemoteException if an error occurs during communication with the server.
     * @throws IOException if an error occurs during file restoration.
     */
    public void restoreFileFromServer() throws RemoteException, IOException {
    	String file = server.getSavedFile(port);
    	fileManager.restoreFileFromString(file);
    }

}