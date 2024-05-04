package forms.serverrmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void saveDistant(String[] strings) throws RemoteException, IOException;
    String[] loadDistant() throws RemoteException, IOException;
}