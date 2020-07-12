package edu.ufp.inf.sd.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public class FactoryImpl extends UnicastRemoteObject implements FactoryRI {

    private DBMockup db;// = new DBMockup();
    //private ThreadPool pool;// = new ThreadPool(10);
    private HashMap<String, SessionRI> sessions;// = new HashMap();

    /**
     * Uses RMI-default sockets-based transport. Runs forever (do not
     * passivates) hence, does not need rmid (activation deamon) Constructor
     * must throw RemoteException due to super() export().
     */
    public FactoryImpl() throws RemoteException {
        // Invokes UnicastRemoteObject constructor which exports remote object
        super();
        //db = new DBMockup();
        db = DBMockup.getInstance();
        //pool = new ThreadPool(10);
        sessions = new HashMap();
    }

    @Override
    public boolean register(String uname, String pw) throws RemoteException {
        if (!db.exists(uname, pw)) {
            db.register(uname, pw);
            return true;
        }
        return false;
    }


    @Override
    public SessionRI login(String uname, String pw) throws RemoteException {
        User user = db.getUser(uname, pw);
        if (db.exists(uname, pw)) {
            if (!sessions.containsKey(uname)) {
                return new SessionImpl(db, user);
            } else {
                return sessions.get(uname);
            }
        }
        return null;
    }
}
