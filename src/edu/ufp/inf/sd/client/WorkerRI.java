package edu.ufp.inf.sd.client;

import edu.ufp.inf.sd.server.SubjectRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.HashMap;


public interface WorkerRI extends Remote {
    public void beginTask() throws RemoteException;

    public void setSubjectRI(SubjectRI subjectRI) throws RemoteException;

    public void addWorkerCredits(int credits) throws RemoteException;

    public void removeWorkerCredits(int credits) throws RemoteException;

    public int getWorkerCredits() throws RemoteException;

    public int getWorkerCreditsAuthorized() throws RemoteException;

    public void addOwnerCredits(int credits) throws RemoteException;

    public void removeOwnerCredits(int credits) throws RemoteException;

    public int getOwnerCredits() throws RemoteException;

    public int getOwnerCreditsAuthorized() throws RemoteException;

    public void notifyAllHashesFound(int id, HashMap<String, String> hashesCracked) throws RemoteException;

    public void notifyOneHashFound(String text, String hash) throws RemoteException;

    public void printTaskStatus(int tasksInWork, int remainingTasks) throws RemoteException;

    public void update() throws RemoteException;

    public void beginStrategy2and3() throws RemoteException;

}
