package forms.managers;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import forms.serverrmi.Server;

public class ServerManager {
	
	private static String ipServer = "127.0.0.1"; // Local host IP by default
	private static int portServer = 1099; // Port used by RMI by default
	
	public boolean createServer() {
		JPanel panel = new JPanel(new GridLayout(2, 2));
        //String portString = JOptionPane.showInputDialog("Enter a port number between 1024-65535 (default is 1099):");
        JTextField portField = new JTextField("1099");
    	panel.add(new JLabel("Enter the server port number 1024-65535 (default is 1099):"));
    	panel.add(portField);
        
    	int result = JOptionPane.showConfirmDialog(null, panel, "Enter port number", JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            try {
            	String portString = portField.getText();
            	int port = Integer.parseInt(portString);
            	portServer = port;
            	Server.startServer(portServer);
            	StateManager.setServerState(true); // If the server is created then the application automatically connects to the local host
    	        return true;
            } catch (NumberFormatException e1) {
                System.err.println("Could not parse string to an integer: " + e1.toString());
                StateManager.setServerState(false);
                return false;
            }
        }
        StateManager.setServerState(false);
        return false;

	}

	public boolean connectServer() {
    	JPanel panel = new JPanel(new GridLayout(2, 2));
    	JTextField ipField = new JTextField("127.0.0.1");
    	//ipField.setText("127.0.0.1"); // Predefine the IP address
    	JTextField portField = new JTextField("1099");
    	//portField.setText("1099"); // Predefine the port number
    	panel.add(new JLabel("Enter the server IP address (localhost is 127.0.0.1):"));
    	panel.add(ipField);
    	panel.add(new JLabel("Enter the server port number 1024-65535 (default is 1099):"));
    	panel.add(portField);
    	
    	int result = JOptionPane.showConfirmDialog(null, panel, "Connect to Server", JOptionPane.OK_CANCEL_OPTION);
        
    	if (result == JOptionPane.OK_OPTION) {
    	    String ip = ipField.getText();
    	    String portString = portField.getText();
    	    try {
    	    	int port = Integer.parseInt(portString);
    	    	ServerManager.ipServer = ip;
    	    	ServerManager.portServer = port;
                if (Server.isServerRunning(ip, port)) { // Check if the server is running
                    StateManager.setServerState(true);
                    return true;
                } else {
                    //System.err.println("The server is not running.");
                    StateManager.setServerState(false);
                    return false;
                }
	            //StateManager.setServerState(true);
    	    	//return true;
    	    } catch (NumberFormatException e1) {
    	    	System.err.println("Could not parse string to an integer: " + e1.toString());
	            StateManager.setServerState(false);
    	    	return false;
    	    }
    	}
        StateManager.setServerState(false);
    	return false;
	}
	
	public String[] fetchServerConfig() {
		return new String[] {ipServer, Integer.toString(portServer)};
	}

}
