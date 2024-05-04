package forms.serverrmi;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
    void saveDistant(String[] strings) throws RemoteException;
    String[] loadDistant() throws RemoteException;
}