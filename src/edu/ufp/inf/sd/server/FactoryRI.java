package edu.ufp.inf.sd.server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author rmoreira
 */
public interface FactoryRI extends Remote {
    public boolean register(String uname, String pw) throws RemoteException;

    public SessionRI login(String uname, String pw) throws RemoteException;
}
