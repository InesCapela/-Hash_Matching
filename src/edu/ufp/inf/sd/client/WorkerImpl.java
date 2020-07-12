package edu.ufp.inf.sd.client;

import edu.ufp.inf.sd.server.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;

public class WorkerImpl extends UnicastRemoteObject implements WorkerRI {

    public SubjectRI subjectRI;
    public SessionRI workerSession;
    public Task task;

    //private ThreadPool pool;// = new ThreadPool(10);
    ExecutorService pool = Executors.newFixedThreadPool(20);

    /**
     * Uses RMI-default sockets-based transport. Runs forever (do not
     * passivates) hence, does not need rmid (activation deamon) Constructor
     * must throw RemoteException due to super() export().
     */
    public WorkerImpl(SessionRI workerSession) throws RemoteException {
        super();
        this.workerSession = workerSession;

        //pool = new ThreadPool(10);
    }

    public void setSubjectRI(SubjectRI subjectRI) throws RemoteException {
        this.subjectRI = subjectRI;
        this.subjectRI.attach(this);
    }

    @Override
    public void addWorkerCredits(int credits) throws RemoteException {
        workerSession.addCredits(credits);
    }

    @Override
    public void removeWorkerCredits(int credits) throws RemoteException {
        workerSession.removeCredits(credits);
    }

    @Override
    public int getWorkerCredits() throws RemoteException {
        return workerSession.getCredits();
    }

    @Override
    public int getWorkerCreditsAuthorized() throws RemoteException {
        return workerSession.getCreditsAuthorized();
    }

    @Override
    public void addOwnerCredits(int credits) throws RemoteException {
        SessionRI sessionRI = subjectRI.getOwnerSession();
        sessionRI.addCredits(credits);
    }

    @Override
    public void removeOwnerCredits(int credits) throws RemoteException {
        SessionRI sessionRI = subjectRI.getOwnerSession();
        sessionRI.removeCredits(credits);
    }

    @Override
    public int getOwnerCredits() throws RemoteException {
        SessionRI sessionRI = subjectRI.getOwnerSession();
        return sessionRI.getCredits();
    }

    @Override
    public int getOwnerCreditsAuthorized() throws RemoteException {
        SessionRI sessionRI = subjectRI.getOwnerSession();
        return sessionRI.getCreditsAuthorized();
    }

    @Override
    public void notifyAllHashesFound(int id, HashMap<String, String> hashesCracked) throws RemoteException {
        System.out.println("\n=====================");
        System.out.println(" ALL HASHES CRACKED");
        System.out.println("=====================");
        System.out.println(hashesCracked + "\n");

        // Remove taskgroup
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        workerSession.removeTaskGroup(subjectRI, id);
    }

    @Override
    public void notifyOneHashFound(String text, String hash) throws RemoteException {
        System.out.println("\n===================");
        System.out.println(" ONE HASH CRACKED");
        System.out.println("===================");
        System.out.println("{ " + text + " = " + hash + " }");
    }

    @Override
    public void printTaskStatus(int tasksInWork, int remainingTasks) throws RemoteException {
        System.out.println("TASKS IN WORK=" + tasksInWork);
        System.out.println("REMAINING TASKS=" + remainingTasks);
    }

    @Override
    public void update() throws RemoteException {
        if (task != null) {
            task.setHashes(subjectRI.getHashes());
        }
    }

    @Override
    public void beginStrategy2and3() throws RemoteException {

        int creditsUsed;

        while (subjectRI.getTasksSize() > 0 && !subjectRI.isStop()) {

            while (subjectRI.isPaused()) {
                System.out.println("\n=================");
                System.out.println(" Task paused...");
                System.out.println("=================");

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            creditsUsed = 0;

            // Pedir subtask pra trabalhar
            task = subjectRI.giveTask();

            if (task == null) {
                System.out.println("!! OWNER DOES NOT HAVE ENOUGH MONEY !!");
                return;
            }


            try {
                for (String s: task.getWords()) {

                    Executor ex = new Executor(task, subjectRI, s);
                    Future<Integer> futureCall = pool.submit(ex);
                    Integer result = futureCall.get(); // Here the thread will be blocked until the result came back.

                    creditsUsed = creditsUsed + result;
                }

                subjectRI.finishedTask(creditsUsed, workerSession, task);

                //System.in.read();

            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        if (subjectRI.getTasksSize() == 0){
            workerSession.removeTaskGroup(subjectRI, subjectRI.getId());
        }
    }

    @Override
    public void beginTask() throws RemoteException {

        int count;
        int creditsUsed;

        while (subjectRI.getTasksSize() > 0 && !subjectRI.isStop()) {

            while (subjectRI.isPaused()) {
                System.out.println("\n=================");
                System.out.println(" Task paused...");
                System.out.println("=================");

                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }

            count = 0;

            // Pedir subtask pra trabalhar

            task = subjectRI.giveTask();

            if (task == null) {
                System.out.println("!! OWNER DOES NOT HAVE ENOUGH MONEY !!");
                return;
            }

            BufferedReader reader;
            try {
                URL url = new URL(task.getFile());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(60000);

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // skip to start line
                //String word = reader.lines().skip(task.getStart()-1).findFirst().get();
                String word = null;
                while (count < task.getStart()) {
                    word = reader.readLine();
                    count++;
                }

                count = 0;
                creditsUsed = 0;

                // while end we didnt reach the end of the file and user has credits
                while (word != null && !subjectRI.isStop()) {
                    count++;

                    // Finished task
                    if (count > task.getDelta()) {
                        System.out.println("\nCHEGUEI AO FIM DA TASK: start=" + task.getStart() + ", delta=" + task.getDelta() + "\n");
                        break;
                    }

                    Executor ex = new Executor(task, subjectRI, word);
                    Future<Integer> futureCall = pool.submit(ex);
                    Integer result = futureCall.get(); // Here the thread will be blocked until the result came back.

                    creditsUsed = creditsUsed + result;

                    word = reader.readLine();
                }

                //System.out.println("USED->>" + creditsUsed);
                subjectRI.finishedTask(creditsUsed, workerSession, task);

                //System.in.read();

                reader.close();
            } catch (IOException | InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        if (subjectRI.getTasksSize() == 0){
            workerSession.removeTaskGroup(subjectRI, subjectRI.getId());
        }
    }
}
