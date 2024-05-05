package forms.network;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.io.File;

/**
 * @author Simon Antropius
 * @author Maxence Desmonteix
 */

public interface RemoteServer extends Remote {
	
	/**
     * Retrieves the saved file from the server.
     * @return The saved file from the server.
     * @throws RemoteException if a remote communication error occurs.
     */
	File getSavedFile() throws RemoteException;
	
}