package edu.ufp.inf.sd.client;

import edu.ufp.inf.sd.server.FactoryRI;
import edu.ufp.inf.sd.server.SessionRI;
import edu.ufp.inf.sd.server.SubjectRI;
import edu.ufp.inf.sd.util.rmisetup.SetupContextRMI;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <p>
 * Title: Projecto SD</p>
 * <p>
 * Description: Projecto apoio aulas SD</p>
 * <p>
 * Copyright: Copyright (c) 2017</p>
 * <p>
 * Company: UFP </p>
 *
 * @author Rui S. Moreira
 * @version 3.0
 */
public class Client {

    /**
     * Context for connecting a RMI client to a RMI Servant
     */
    private SetupContextRMI contextRMI;
    /**
     * Remote interface that will hold the Servant proxy
     */
    private FactoryRI factoryRI;

    public static void main(String[] args) {
        if (args != null && args.length < 2) {
            System.err.println("usage: java [options] edu.ufp.sd.inf.rmi.edu.ufp.inf.sd.rmi.helloworld.server.HelloWorldClient <rmi_registry_ip> <rmi_registry_port> <service_name>");
            System.exit(-1);
        } else {
            //1. ============ Setup client RMI context ============
            Client hwc = new Client(args);
            //2. ============ Lookup service ============
            hwc.lookupService();
            //3. ============ Play with service ============
            hwc.playService();
        }
    }

    public Client(String[] args) {
        try {
            //List ans set args
            SetupContextRMI.printArgs(this.getClass().getName(), args);
            String registryIP = args[0];
            String registryPort = args[1];
            String serviceName = args[2];
            //Create a context for RMI setup
            contextRMI = new SetupContextRMI(this.getClass(), registryIP, registryPort, new String[]{serviceName});
        } catch (RemoteException e) {
            Logger.getLogger(Client.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    private Remote lookupService() {
        try {
            //Get proxy to rmiregistry
            Registry registry = contextRMI.getRegistry();
            //Lookup service on rmiregistry and wait for calls
            if (registry != null) {
                //Get service url (including servicename)
                String serviceUrl = contextRMI.getServicesUrl(0);
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to lookup service @ {0}", serviceUrl);

                //============ Get proxy to HelloWorld service ============
                factoryRI = (FactoryRI) registry.lookup(serviceUrl);
            } else {
                Logger.getLogger(this.getClass().getName()).log(Level.INFO, "registry not bound (check IPs). :(");
                //registry = LocateRegistry.createRegistry(1099);
            }
        } catch (RemoteException | NotBoundException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
        return factoryRI;
    }

    private void playService() {
        try {

            int taskid;

            // get username and password
            System.out.println("\n\n ==== AUTHENTICATION ====\n");
            System.out.print("USERNAME -> ");
            String user = System.console().readLine();
            System.out.print("PASSWORD -> ");
            String pw = System.console().readLine();

            // registar user
            if (this.factoryRI.register(user, pw)) {
                System.out.println("\nUser: [" + user + "] FOI AGORA REGISTADO!\n");
            } else {
                System.out.println("\nUser: [" + user + "] JA EXISTE!\n");
            }

            // autenticar
            SessionRI sessionRI = this.factoryRI.login(user, pw);

            // create worker
            WorkerRI worker = new WorkerImpl(sessionRI);

            int option = -1;

            while (option != 0) {
                System.out.println("\n\n[1] CREATE HASH TASKGROUP");
                System.out.println("[2] FIND HASH MATCH");
                System.out.println("[3] DELETE TASKGROUP");
                System.out.println("[4] READ MY CREDITS");
                System.out.println("[5] ADD CREDITS");
                System.out.println("[6] LIST HASHES FOUND");
                System.out.println("[7] LIST HASHES TO FIND");
                System.out.println("[8] LIST TASKGROUPS STATUS");
                System.out.println("[9] PAUSE/RESUME");
                System.out.println("[10] CREATE TASKGROUP STRATEGY 3");
                System.out.println("[11] JOIN TASKGROUP STRATEGY 3");
                System.out.println("[12] CREATE TASKGROUP STRATEGY 2");
                System.out.println("[13] JOIN TASKGROUP STRATEGY 2");
                System.out.println("\n[0] EXIT");
                System.out.print(" -> ");
                option = Integer.parseInt(System.console().readLine());
                if (option < 0 || option > 13) {
                    System.out.println("\n\n!!! OPÇÃO NÃO EXISTE !!!\n");
                    continue;
                }

                // CREATE TASKGROUP
                if (option == 1) {
                    // start client
                    //worker.addWorkerCredits(10000);
//                    System.out.println("MY CREDITS: " + worker.getWorkerCredits());
//                    System.out.println("MY CREDITSAUTHORIZED: " + worker.getWorkerCreditsAuthorized());

                    // create hashes to find
                    String[] hashes = {
                            "ba9d649173d7ffc9d754c29f50d8f45d36183c02e87cdb915d4eba9d3972b5f258b3e9224a1a5a48834e89eab16d15c27926e55796921ca3cfd43a7cbf47ad5c",
                            "4693f3725e35649c868abb5acbfa4a9d9edf274b756b2f4fff4de2b5e51e5d4634102771967810d272007436332ab8216b4d97c36e9d36965596b2621f0549dd",
                            "279d837afef15475736b910544b31b2c7c4bcf071ccf47d7c3af037a128a3fa5be4e2575c094afc1a65e8f837e354a33befcf97340453a05eec8e2235090e2bd",
                            "f386bbe7c93cad078b035ed613d644cd8ba9916503100def97cf19f09d6e0b69532e50837b1ad5e8e2da6d69f4b37799aa21a3d0295d5d925eed891b52ff6be3",
                            "cantfind"
                    };

                    //  create and attach taskGroup to client
                    sessionRI.createTaskGroup(worker, "Tarefa 1", hashes, "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/darkc0de.txt", 1000);

                    System.out.println("====================");
                    System.out.println(" TASKGROUP CREATED");
                    System.out.println("====================");
                }

                // SEARH HASHES
                if (option == 2) {
                    // listar tarefas
                    ArrayList<SubjectRI> taskGroups = sessionRI.listTaskGroups();

                    if (taskGroups.size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : taskGroups) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    System.out.print("JUNTAR A TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI taskGroupToWork = sessionRI.getTaskGroup(taskid);

                    // Attach worker
                    worker.setSubjectRI(taskGroupToWork);

                    // Start working
                    worker.beginTask();
                }

                // DELETE TASKGROUP
                if (option == 3) {
                    // listar tarefas
                    if (sessionRI.listTaskGroups().size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : sessionRI.listTaskGroups()) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    // delete tarefa
                    System.out.print("QUAL A TAREFA QUE PRETENDE ELIMINAR?\n -> ");
                    int id = Integer.parseInt(System.console().readLine());
                    sessionRI.deleteTaskGroup(id);
                    System.out.println("TASK [" + id + "] DELETED!");
                }

                if (option == 4) {
                    System.out.println("\n==========================");
                    System.out.println(" MY CREDITS: " + worker.getWorkerCredits());
                    System.out.println("==========================");
                }

                // ADD CREDITS
                if (option == 5) {
                    System.out.println("\n====================================");
                    System.out.println(" How many credits you want to add? ");
                    System.out.println("====================================\n");
                    System.out.print(" -> ");
                    int credits = Integer.parseInt(System.console().readLine());
                    while (credits < 0) {
                        System.out.print("\nINVALID CREDITS!\n\n -> ");
                        credits = Integer.parseInt(System.console().readLine());
                    }
                    worker.addWorkerCredits(credits);
                }

                // LIST HASHES FOUND
                if (option == 6) {
                    // listar tarefas
                    if (sessionRI.listTaskGroups().size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : sessionRI.listTaskGroups()) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    System.out.print("SELECT TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI subject1 = sessionRI.getTaskGroup(taskid);
                    System.out.println("\n===============");
                    System.out.println(" Hashes Found ");
                    System.out.println("===============\n");
                    System.out.println(subject1.getHashesCracked());
                }

                // LIST HASHES TO FIND
                if (option == 7) {
                    // listar tarefas
                    if (sessionRI.listTaskGroups().size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : sessionRI.listTaskGroups()) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    System.out.print("SELECT TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI subject2 = sessionRI.getTaskGroup(taskid);
                    System.out.println("\n===============");
                    System.out.println(" Hashes To Find ");
                    System.out.println("===============\n");
                    System.out.println(Arrays.toString(subject2.getHashes()));
                }

                if (option == 8) {
                    // listar tarefas
                    if (sessionRI.listTaskGroups().size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : sessionRI.listTaskGroups()) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }
                }

                // PAUSE/RESUME
                if (option == 9) {
                    // listar tarefas
                    if (sessionRI.listTaskGroups().size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : sessionRI.listTaskGroups()) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }
                    System.out.print("SELECT TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI subject3 = sessionRI.getTaskGroup(taskid);

                    if (subject3.pauseResumeTaskGroup()) {
                        System.out.println("\n==========");
                        System.out.println(" PAUSED ");
                        System.out.println("==========\n");
                    } else {
                        System.out.println("\n==========");
                        System.out.println(" RESUMED ");
                        System.out.println("==========\n");
                    }
                }

                // STRATEGY 3
                if (option == 10) {
                    System.out.print("INSERT THE CHARACTERS DICTIONARY\n -> ");
                    String dic = System.console().readLine();

                    System.out.print("INSERT THE SIZE OF DICTIONARY\n -> ");
                    int dicSize = Integer.parseInt(System.console().readLine());

                    // generate all combinations
                    ArrayList<String> listWords = generateCombinations(dic, dicSize);

                    // Convert to String[]
                    String[] words = new String[listWords.size()];
                    words = listWords.toArray(words);

                    System.out.println("wordlistcreated=" + Arrays.toString(words));

                    String[] hashes = {
                            "6fefb8cb871836f2dd7649a53311bff9be8fb076d23d1936f40f8f9d3cebca8133a50782b9d52ddf9a1e467bfbee61a2cdad9d2ce65e9031648b34ae1d01cfa3"
                    };

                    sessionRI.createTaskGroupStrategy3(worker, "Taskgroup strategy3", hashes, words, 10);

                    System.out.println("====================");
                    System.out.println(" TASKGROUP CREATED");
                    System.out.println("====================");
                }

                // SEARH HASHES STRATEGY 3
                if (option == 11) {
                    // listar tarefas
                    ArrayList<SubjectRI> taskGroups = sessionRI.listTaskGroupsStrategy3();
                    if (taskGroups.size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : taskGroups) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    System.out.print("JUNTAR A TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI taskGroupToWork = sessionRI.getTaskGroup(taskid);

                    // Attach worker
                    worker.setSubjectRI(taskGroupToWork);

                    // Start working
                    worker.beginStrategy2and3();
                }

                // STRATEGY 2
                if (option == 12) {

                    // create hashes to find
                    String[] hashes = {
                            "ba9d649173d7ffc9d754c29f50d8f45d36183c02e87cdb915d4eba9d3972b5f258b3e9224a1a5a48834e89eab16d15c27926e55796921ca3cfd43a7cbf47ad5c"
                    };

                    System.out.print("NUM CHARS -> ");
                    int numChars = Integer.parseInt(System.console().readLine());

                    //  create and attach taskGroup to client
                    sessionRI.createTaskGroupStrategy2(worker, "Tarefa Strategy 2", hashes, "https://raw.githubusercontent.com/danielmiessler/SecLists/master/Passwords/darkc0de.txt", 5, numChars);

                    System.out.println("====================");
                    System.out.println(" TASKGROUP CREATED");
                    System.out.println("====================");
                }

                // SEARH HASHES
                if (option == 13) {
                    // listar tarefas
                    ArrayList<SubjectRI> taskGroups = sessionRI.listTaskGroupsStrategy2();
                    if (taskGroups.size() == 0) {
                        System.out.println("\n !!! THERE ARE NO TASKGROUPS !!!\n");
                        continue;
                    }
                    for (SubjectRI s : taskGroups) {
                        System.out.println("[" + s.printTaskGroupInfo() + "]");
                    }

                    System.out.print("JUNTAR A TASKGROUP -> ");
                    taskid = Integer.parseInt(System.console().readLine());

                    // get TASKGROUP
                    SubjectRI taskGroupToWork = sessionRI.getTaskGroup(taskid);

                    // Attach worker
                    worker.setSubjectRI(taskGroupToWork);

                    // Start working
                    worker.beginStrategy2and3();
                }
            }

            Logger.getLogger(this.getClass().getName()).log(Level.INFO, "going to finish, bye. ;)");
        } catch (RemoteException | IllegalArgumentException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Set<String> permutation(String str) {
        Set<String> result = new HashSet<>();
        if (str == null) {
            return null;
        } else if (str.length() == 0) {
            result.add("");
            return result;
        }

        char firstChar = str.charAt(0);
        String rem = str.substring(1);
        Set<String> words = permutation(rem);
        for (String newString : words) {
            for (int i = 0; i <= newString.length(); i++) {
                result.add(charAdd(newString, firstChar, i));
            }
        }
        return result;
    }

    public ArrayList<String> generateCombinations(String str, int length) {

        ArrayList<String> allCombs = new ArrayList<>();

        // Creating array of string length
        char[] pool = new char[str.length()];
        // Copy character by character into array
        for (int i = 0; i < str.length(); i++) {
            pool[i] = str.charAt(i);
        }

        int[] indexes = new int[length];
        // In Java all values in new array are set to zero by default
        // in other languages you may have to loop through and set them.

        int pMax = pool.length;  // stored to speed calculation

        StringBuilder newComb = new StringBuilder();

        while (indexes[0] < pMax){ //if the first index is bigger then pMax we are done

            // print the current permutation
            for (int i = 0; i < length; i++){
                newComb.append(pool[indexes[i]]);
            }
            //System.out.println(newComb);
            if (!allCombs.contains(newComb.toString())){
                allCombs.add(newComb.toString());
            }
            newComb.setLength(0);

            // increment indexes
            indexes[length-1]++; // increment the last index
            for (int i = length-1; indexes[i] == pMax && i > 0; i--){ // if increment overflows
                indexes[i-1]++;  // increment previous index
                indexes[i]=0;   // set current index to zero
            }
        }

        return allCombs;
    }

    public static String charAdd(String str, char c, int j) {
        String first = str.substring(0, j);
        String last = str.substring(j);
        return first + c + last;
    }
}
