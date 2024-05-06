package forms.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.List;

import java.io.IOException;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public interface RemoteServer extends Remote {
	
	/**
     * Gets the list of ports used by a server.
     * @return The list of ports used by a server.
     */
	public List<Integer> getPorts() throws RemoteException;
	
	/**
     * Retrieves the content of the saved file as a string.
     * @param port The port number used to construct the file name.
     * @return A string containing the content of the saved file.
     * @throws RemoteException If a remote communication error occurs.
     * @throws IOException If an I/O error occurs while retrieving the file content.
     */
	String getSavedFile(int port) throws RemoteException, IOException;
	
}