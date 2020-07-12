package edu.ufp.inf.sd.server;

import edu.ufp.inf.sd.client.WorkerRI;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Timestamp;
import java.util.*;

public class SubjectImpl extends UnicastRemoteObject implements SubjectRI {

    private String[] hashes;
    private Integer id;
    private String name;
    private boolean stop;
    private ArrayList<Task> task = new ArrayList<>();
    private ArrayList<Task> tasksInWork = new ArrayList<>();
    private ArrayList<WorkerRI> workers = new ArrayList<>();
    private WorkerRI owner;
    public SessionRI sessionRI;
    private HashMap<String, String> hashesCracked = new HashMap<>();
    public boolean paused;
    private Integer strategy;
    private String[] words;
    private int numChars;

    protected SubjectImpl(WorkerRI owner, SessionRI sessionRI, String name, String[] hashes, String file, int delta) throws RemoteException {
        super();

        Random rand = new Random();
        this.id = rand.nextInt(100);
        this.name = name;
        this.stop = false;
        this.sessionRI = sessionRI;
        this.owner = owner;
        this.hashes = hashes;
        this.strategy = 0;

        BufferedReader reader;
        try {
            // Create a URL for the desired page
            URL url = new URL(file); //My text file location
            //First open the connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute

            //reader = new BufferedReader(new FileReader(path));
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            int count = 0;
            int line = 1;
            String word = reader.readLine();
            while (word != null) {
                count++;

                //read next line
                word = reader.readLine();

                // split file and assign to task
                if (count == delta || word == null) {
                    task.add(new Task(name, file, line, count, hashes));
                    line = line + count;
                    count = 0;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SubjectImpl(WorkerRI owner, SessionRI sessionRI, String name, String[] hashes, String file, int delta, int numChars) throws RemoteException {
        super();

        Random rand = new Random();
        this.id = rand.nextInt(100);
        this.name = name;
        this.stop = false;
        this.sessionRI = sessionRI;
        this.owner = owner;
        this.hashes = hashes;
        this.strategy = 2;
        this.numChars = numChars;

        BufferedReader reader;
        try {
            // Create a URL for the desired page
            URL url = new URL(file); //My text file location
            //First open the connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(60000); // timing out in a minute

            reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            int count = 0;
            ArrayList<String> wordsList = new ArrayList<>();

            String word = reader.readLine();
            while (word != null) {

                if (word.length() == numChars) {
                    count++;
                    wordsList.add(word);
                }

                //read next line
                word = reader.readLine();

                // split file and assign to task
                if (count == delta || word == null) {
                    task.add(new Task(name, wordsList, hashes));
                    wordsList.clear();
                    count = 0;
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected SubjectImpl(WorkerRI owner, SessionRI sessionRI, String name, String[] hashes, String[] words, int delta) throws RemoteException {
        super();

        Random rand = new Random();
        this.id = rand.nextInt(100);
        this.name = name;
        this.stop = false;
        this.sessionRI = sessionRI;
        this.owner = owner;
        this.hashes = hashes;
        this.words = words;
        this.strategy = 3;

        int count = 0;

        ArrayList<String> wordsList = new ArrayList<>();

        for (String s: words) {
            count++;
            wordsList.add(s);

            // split file and assign to task
            if (count == delta || count == words.length) {
                task.add(new Task(name, wordsList, hashes));
                count = 0;
                wordsList.clear();
            }
        }
    }

    public Integer getId() {
        return id;
    }

    public boolean isStop() {
        return stop;
    }

    @Override
    public int getTasksSize() throws RemoteException {
        return task.size();
    }

    public ArrayList<Task> getTask() {
        return task;
    }

    public synchronized void setTask(ArrayList<Task> task) {
        this.task = task;
    }

    @Override
    public SessionRI getOwnerSession() throws RemoteException {
        return sessionRI;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    public String[] getHashes() {
        return hashes;
    }

    public HashMap<String, String> getHashesCracked() {
        return hashesCracked;
    }

    @Override
    public String toString() {
        return "TaskGroup {" + "id=" + id + ", name=" + name + ", subtasks=" + task.size() + '}';
    }

    @Override
    public void attach(WorkerRI wrkRI) throws RemoteException {
        if (!workers.contains((wrkRI))) {
            workers.add(wrkRI);
        }
    }

    @Override
    public void detach(WorkerRI wrkRI) throws RemoteException {

    }

    @Override
    public Task giveTask() throws RemoteException {

        Task t = task.get(0);
        //System.out.println("TRYING TO GIVE: " + t.toString());

        // Has enough credits for this task
        //System.out.println("NEEDED= " + ((t.getDelta() - t.getHashes().length) + (10 * t.getHashes().length)));
        if (sessionRI.getCreditsAuthorized() >= ((t.getDelta() - t.getHashes().length) + (10 * t.getHashes().length))) {
            // Remove creditsAuthorized for this task
            sessionRI.removeCreditsAuthorized(((t.getDelta() - t.getHashes().length) + (10 * t.getHashes().length)));
            // Update task timestamp
            long timestamp = System.currentTimeMillis();
            t.setStartedAt(timestamp);
            // Add this task to tasksInWork
            tasksInWork.add(t);
            // Remove this task from available tasks list
            task.remove(t);

            // returh this task to work on
            return t;
        }

        // Calculate how many lines the owner can afford
        // How many matching lines he can find
        int canAfford = 0;
        boolean noMoreFunds = false;
        for (int i = 0; i < t.getHashes().length; i++) {
            if (sessionRI.getCreditsAuthorized() >= 10) {
                canAfford++;
                sessionRI.removeCreditsAuthorized(10);
            } else {
                noMoreFunds = true;
                break;
            }
        }
        // How many lines he can aditionally test
        if (!noMoreFunds) { // funds were not all used in case of finding match
            while (sessionRI.getCreditsAuthorized() >= 1) {
                canAfford++;
                sessionRI.removeCreditsAuthorized(1);
            }
        }

        // System.out.println("CAN AFFORD= " + canAfford);

        if (canAfford > 0) {
            // Add to tasksInWork the task he can work
            Task newTask = new Task(t.getName(), t.getFile(), t.getStart(), canAfford, t.getHashes());
            // Update task timestamp and add to tasksInWork
            long timestamp = System.currentTimeMillis();
            newTask.setStartedAt(timestamp);
            tasksInWork.add(newTask);
            //System.out.println(newTask.toString());

            // Add the remaining lines of this task to available task list
            Task remainingTask = new Task(t.getName(), t.getFile(), t.getStart() + canAfford, (t.getDelta() - canAfford), t.getHashes());
            task.add(remainingTask);
            //System.out.println(remainingTask.toString());

            // Remove original task from list
            task.remove(t);

            return newTask;
        }

        return null;
    }

    public Integer getStrategy() {
        return strategy;
    }

    public ArrayList<Task> getTasksInWork() {
        return tasksInWork;
    }

    public synchronized void setTasksInWork(ArrayList<Task> tasksInWork) {
        this.tasksInWork = tasksInWork;
    }

    @Override
    public void finishedTask(int creditsUsed, SessionRI workerSession, Task task) throws RemoteException {
        // Remove used credits
        sessionRI.removeCredits(creditsUsed);
        // Give used credits
        workerSession.addCredits(creditsUsed);
        // Remove task from tasksInWork
        tasksInWork.removeIf(t -> t.getId().equals(task.getId()));
        //owner.printTaskStatus(tasksInWork.size(), this.task.size());
    }

    @Override
    public void hashFound(String text, String hash) throws RemoteException {
        // Save result
        hashesCracked.put(text, hash);

        // remove hash from taskgroup
        List<String> taskGroupHasheslist = new ArrayList<>(Arrays.asList(hashes));
        taskGroupHasheslist.remove(hash);
        hashes = taskGroupHasheslist.toArray(new String[0]);

        boolean allFound = false;
        // No more hashes to search for = all found
        if (hashes.length == 0) {
            allFound = true;
            System.out.println("ALL FOUND!");
        }

        // Remove found hash from every subtask so they dont need to search for it
        for (Task t : task) {
            for (int i = 0; i < t.getHashes().length; i++) {
                List<String> list = new ArrayList<>(Arrays.asList(t.getHashes()));
                if (t.getHashes()[i].equals(hash)) {
                    list.remove(t.getHashes()[i]);
                }
                t.setHashes(list.toArray(new String[0]));
            }
        }

        if (allFound) {
            // Stop all workers
            setStop(true);
            System.out.println("STOPPING ALL WORKERS...");
            owner.notifyAllHashesFound(id, hashesCracked);
        } else {
            // Notify owner that one hs was found
            owner.notifyOneHashFound(text, hash);
            // Notify all workers that one hash was found
            notifyAllWorkers();
        }

    }

    public void notifyAllWorkers() {
        for (WorkerRI wrkr : workers) {
            try {
                wrkr.update();
            } catch (RemoteException ex) {
                System.out.println(ex.toString());
            }
        }
    }

    @Override
    public String printTaskGroupInfo() throws RemoteException {
        return this.toString();
    }

    @Override
    public boolean pauseResumeTaskGroup() throws RemoteException {
        paused = !paused;
        return paused;
    }

    public boolean isPaused() {
        return paused;
    }
//    @Override
//    public Task getTask() throws RemoteException {
//        return task;
//    }
//
//    @Override
//    public void pauseTask() throws RemoteException {
//        //task.setPaused(!task.isPaused());
//        setState(new State("pause", task.isPaused()));
//    }
//
//    @Override
//    public void attach(ObserverRI obsRI) {
//        if(!this.observers.contains(obsRI)) this.observers.add(obsRI);
//    }
//
//    @Override
//    public void detach(ObserverRI obsRI) {
//        this.observers.remove(obsRI);
//    }
//
//    @Override
//    public State getState() {
//        return this.subjectState;
//    }
//
//    @Override
//    public void setState(State state) {
//        this.subjectState = state;
//        this.notifyAllObservers();
//    }
//
//    public void notifyAllObservers() {
//        for(ObserverRI obs : observers){
//            try{
//                obs.update();
//            } catch (RemoteException ex){
//                System.out.println(ex.toString());
//            }
//        }
//    }
}