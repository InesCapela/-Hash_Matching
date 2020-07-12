package edu.ufp.inf.sd.server;

import edu.ufp.inf.sd.client.WorkerRI;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;

public interface SubjectRI extends Remote {

    public void attach(WorkerRI wrkRI) throws RemoteException;

    public void detach(WorkerRI wrkRI) throws RemoteException;

    public Task giveTask() throws RemoteException;

    public void finishedTask(int creditsUsed, SessionRI workerSession, Task task) throws RemoteException;

    public void hashFound(String text, String hash) throws RemoteException;

    public String printTaskGroupInfo() throws RemoteException;

    public Integer getId() throws RemoteException;

    public boolean isStop() throws RemoteException;

    public int getTasksSize() throws RemoteException;

    public SessionRI getOwnerSession() throws RemoteException;

    public String[] getHashes() throws RemoteException;

    public HashMap<String, String> getHashesCracked() throws RemoteException;

    public boolean pauseResumeTaskGroup() throws RemoteException;

    public boolean isPaused() throws RemoteException;

    public Integer getStrategy() throws RemoteException;

    public ArrayList<Task> getTasksInWork() throws RemoteException;

    public void setTasksInWork(ArrayList<Task> tasksInWork) throws RemoteException;

    public ArrayList<Task> getTask() throws RemoteException;

    public void setTask(ArrayList<Task> task) throws RemoteException;
}