package edu.ufp.inf.sd.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class simulates a DBMockup for managing users and books.
 *
 * @author rmoreira
 */
public class DBMockup {

    private static DBMockup dbMockup = null;
    private final ArrayList<User> users;// = new ArrayList();
    private HashMap<User, SessionRI> sessions;// Sessions array
    private HashMap<User, ArrayList<SubjectRI>> taskLists;// Lista de tarefas de um utilizador


    /**
     * This constructor creates and inits the database with some books and users.
     */
    public DBMockup() {
        users = new ArrayList();
        //Add one user
        users.add(new User("guest", "ufp"));
        sessions = new HashMap<>();
        taskLists = new HashMap<>();
    }

    public synchronized static DBMockup getInstance() {
        if (dbMockup == null)
            dbMockup = new DBMockup();
        return dbMockup;
    }

    /**
     * Registers a new user.
     *
     * @param u username
     * @param p passwd
     */
    public void register(String u, String p) {
        if (!exists(u, p)) {
            users.add(new User(u, p));
        }
    }

    /**
     * Checks the credentials of an user.
     *
     * @param u username
     * @param p passwd
     * @return
     */
    public boolean exists(String u, String p) {
        for (User usr : this.users) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPword().compareTo(p) == 0) {
                return true;
            }
        }
        return false;
    }

    public User getUser(String u, String p) {
        for (User usr : this.users) {
            if (usr.getUname().compareTo(u) == 0 && usr.getPword().compareTo(p) == 0) {
                return usr;
            }
        }
        return null;
    }

    public void saveTask(User u, SubjectRI s) {
        if (this.taskLists.containsKey(u)) {
            this.taskLists.get(u).add(s);
        } else if (!(this.taskLists.containsKey(u))) {
            ArrayList<SubjectRI> taskArray = new ArrayList<>();
            taskArray.add(s);
            this.taskLists.put(u, taskArray);
        }
    }

    public HashMap<User, ArrayList<SubjectRI>> getTasksList() {
        return this.taskLists;
    }

}
