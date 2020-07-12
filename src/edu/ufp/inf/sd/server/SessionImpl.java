package edu.ufp.inf.sd.server;

import edu.ufp.inf.sd.client.WorkerRI;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SessionImpl extends UnicastRemoteObject implements SessionRI {
    DBMockup db;
    User user;

    public SessionImpl(DBMockup db, User u) throws RemoteException {
        super();
        this.db = db;
        this.user = u;
    }

    @Override
    public SubjectRI createTaskGroup(WorkerRI owner, String taskName, String[] hashes, String file, int delta) throws RemoteException {
        SubjectRI subjectRI = new SubjectImpl(owner, this, taskName, hashes, file, delta); // criar tarefa
        db.saveTask(this.user, subjectRI); // adicionar tarefa a base de dados
        return subjectRI;
    }

    @Override
    public SubjectRI createTaskGroupStrategy2(WorkerRI owner, String taskName, String[] hashes, String file, int delta, int numChars) throws RemoteException {
        SubjectRI subjectRI = new SubjectImpl(owner, this, taskName, hashes, file, delta, numChars); // criar tarefa
        db.saveTask(this.user, subjectRI); // adicionar tarefa a base de dados
        return subjectRI;
    }

    @Override
    public SubjectRI createTaskGroupStrategy3(WorkerRI owner, String taskName, String[] hashes, String[] words, int delta) throws RemoteException {
        SubjectRI subjectRI = new SubjectImpl(owner, this, taskName, hashes, words, delta); // criar tarefa
        db.saveTask(this.user, subjectRI); // adicionar tarefa a base de dados
        return subjectRI;
    }

    @Override
    public SubjectRI getTaskGroup(Integer id) throws RemoteException {
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        for (User user : taskList.keySet()) {
            if (!taskList.get(user).isEmpty()) {
                for (SubjectRI subjectRI : taskList.get(user)) {
                    if (subjectRI.getId().equals(id)) {
                        return subjectRI; // return taskgroup
                    }
                }
            }
        }
        return null;
    }

    public synchronized void removeTaskGroup(SubjectRI taskGroup, int id) throws RemoteException {
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        HashMap<User, SubjectRI> userSubjectHashMap = new HashMap<>();

        for (User user : taskList.keySet()) {
            if (!taskList.get(user).isEmpty()) {
                for (SubjectRI subjectRI : taskList.get(user)) {
                    if (subjectRI.getId().equals(id)) {
                        //taskList.get(user).remove(subjectRI);
                        userSubjectHashMap.put(user, subjectRI);
                    }
                }
            }
        }

        // remove
        for (Map.Entry<User, SubjectRI> entry : userSubjectHashMap.entrySet()) {
            taskList.get(entry.getKey()).remove(entry.getValue());
        }
    }

    @Override
    public ArrayList<SubjectRI> listTaskGroups() throws RemoteException {
        ArrayList<SubjectRI> subjectRIS = new ArrayList<>();
        HashMap<User, ArrayList<SubjectRI>> taskHashMap = db.getTasksList();
        for (User user : taskHashMap.keySet()) {
            if (!taskHashMap.get(user).isEmpty()) {
                subjectRIS.addAll(taskHashMap.get(user));
            }
        }
        return subjectRIS;
    }

    @Override
    public ArrayList<SubjectRI> listTaskGroupsStrategy2() throws RemoteException {
        ArrayList<SubjectRI> subjectRIS = new ArrayList<>();
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        for (User user : taskList.keySet()) {
            for (SubjectRI subjectRI : taskList.get(user)) {
                if (subjectRI.getStrategy().equals(2)) {
                    subjectRIS.add(subjectRI);
                }
            }
        }

        return subjectRIS;
    }

    @Override
    public ArrayList<SubjectRI> listTaskGroupsStrategy3() throws RemoteException {
        ArrayList<SubjectRI> subjectRIS = new ArrayList<>();
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        for (User user : taskList.keySet()) {
            for (SubjectRI subjectRI : taskList.get(user)) {
                if (subjectRI.getStrategy().equals(3)) {
                    subjectRIS.add(subjectRI);
                }
            }
        }

        return subjectRIS;
    }

    @Override
    public boolean deleteTaskGroup(Integer id) throws RemoteException {
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        for (User user : taskList.keySet()) {
            if (!taskList.get(user).isEmpty()) {
                for (SubjectRI subjectRI : taskList.get(user)) {
                    if (subjectRI.getId().equals(id)) {
                        return taskList.get(user).remove(subjectRI);// remove task from hashmap
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void addCredits(int credits) throws RemoteException {
        user.setCredits(user.getCredits() + credits);
        user.setCreditsAuthorized(user.getCreditsAuthorized() + credits);
    }

    @Override
    public int getCredits() throws RemoteException {
        return user.getCredits();
    }

    @Override
    public void removeCredits(int credits) throws RemoteException {
        user.setCredits(Math.max(user.getCredits() - credits, 0));
        //user.setCreditsAuthorized(Math.max(user.getCreditsAuthorized() - credits, 0));
    }

    @Override
    public void addCreditsAuthorized(int credits) throws RemoteException {
        user.setCreditsAuthorized(user.getCreditsAuthorized() + credits);
    }

    @Override
    public int getCreditsAuthorized() throws RemoteException {
        return user.getCreditsAuthorized();
    }

    @Override
    public void removeCreditsAuthorized(int credits) throws RemoteException {
        user.setCreditsAuthorized(Math.max(user.getCreditsAuthorized() - credits, 0));
    }

    @Override
    public void giveCredits(String username) throws RemoteException {
        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();
        for (User user : taskList.keySet()) {
            if (!taskList.get(user).isEmpty()) {

            }
        }
    }
}
