package edu.ufp.inf.sd.server;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Polling implements Runnable {

    private DBMockup db;

    public Polling() {
        db = DBMockup.getInstance();
    }

    @Override
    public void run() {
        ArrayList<Task> tasksToChange = new ArrayList<>();
        HashMap<SubjectRI, ArrayList<Task>> taskMap = new HashMap<>();

        boolean unfinishedFound = false;

        System.out.println("POLLING THREAD CREATED!");

        HashMap<User, ArrayList<SubjectRI>> taskList = db.getTasksList();

        while (true) {
            for (User user : taskList.keySet()) {
                for (SubjectRI subjectRI : taskList.get(user)) {
                    try {
                        if (subjectRI.getTasksInWork().size() > 0) {
                            for (Task t : subjectRI.getTasksInWork()) {
                                long tenMinutesAgo = System.currentTimeMillis() - 10 * 1000;// * 60
                                if (t.getStartedAt() < tenMinutesAgo) {
                                    // task is in worke for more than 10 minutes! restore it to the list
                                    unfinishedFound = true;
                                    tasksToChange.add(t);
                                    System.out.println("old task!");
                                    System.out.println(t.toString());
                                }
                            }
                            taskMap.put(subjectRI, tasksToChange);
                            taskMap.toString();
                        }
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                }
            }

            // Restore the unfinished tasks of every subject
            if (unfinishedFound) {
                for (Map.Entry<SubjectRI, ArrayList<Task>> entry : taskMap.entrySet()) {
                    try {
                        // Remove tasks in work
                        ArrayList<Task> tasksInWork = entry.getKey().getTasksInWork();
                        tasksInWork.removeAll(entry.getValue());
                        entry.getKey().setTasksInWork(tasksInWork);

                        // Add them to tasks
                        ArrayList<Task> tasks = entry.getKey().getTask();
                        tasks.addAll(entry.getValue());
                        entry.getKey().setTask(tasks);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
                tasksToChange.clear();
                unfinishedFound = false;
            }


            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
