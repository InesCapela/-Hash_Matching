package edu.ufp.inf.sd.server;

import edu.ufp.inf.sd.client.WorkerRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author rmoreira
 */
public interface SessionRI extends Remote {

    public SubjectRI createTaskGroup(WorkerRI owner, String taskName, String[] hashes, String file, int delta) throws RemoteException;

    public SubjectRI createTaskGroupStrategy3(WorkerRI owner, String taskName, String[] hashes, String[] words, int delta) throws RemoteException;

    public SubjectRI getTaskGroup(Integer id) throws RemoteException;

    public ArrayList<SubjectRI> listTaskGroups() throws RemoteException;

    public boolean deleteTaskGroup(Integer id) throws RemoteException;

    public void addCredits(int credits) throws RemoteException;

    public int getCredits() throws RemoteException;

    public void removeCredits(int credits) throws RemoteException;

    public void addCreditsAuthorized(int credits) throws RemoteException;

    public int getCreditsAuthorized() throws RemoteException;

    public void removeCreditsAuthorized(int credits) throws RemoteException;

    public void giveCredits(String username) throws RemoteException;

    public void removeTaskGroup(SubjectRI taskGroup, int id) throws RemoteException;

    public ArrayList<SubjectRI> listTaskGroupsStrategy2() throws RemoteException;

    public ArrayList<SubjectRI> listTaskGroupsStrategy3() throws RemoteException;

    public SubjectRI createTaskGroupStrategy2(WorkerRI owner, String taskName, String[] hashes, String file, int delta, int numChars) throws RemoteException;
}
