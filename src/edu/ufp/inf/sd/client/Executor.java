package edu.ufp.inf.sd.client;

import edu.ufp.inf.sd.server.SubjectRI;
import edu.ufp.inf.sd.server.Task;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.Callable;

public class Executor implements Callable<Integer> {

    private Task task;
    private SubjectRI subject;
    private String word;

    public Executor(Task task, SubjectRI subject, String word) {
        this.task = task;
        this.subject = subject;
        this.word = word;
    }

    @Override
    public Integer call() throws Exception {

        int creditsUsed;

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] messageDigest = md.digest(word.getBytes());
            // Convert byte array into signum representation
            BigInteger no = new BigInteger(1, messageDigest);
            // Convert message digest into hex value
            String hashtext = no.toString(16);
            // Add preceding 0s to make it 32 bit
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }

            // compare the HashText
            String finalHashtext = hashtext;

            //System.out.println("HASH -> " + finalHashtext);
            boolean found = Arrays.asList(task.getHashes()).contains(finalHashtext);
            if (found) {
                System.out.println("FOUND! { " + word + " = " + hashtext + " }");
                // 10 credits used
                creditsUsed = 10;
                // Remove hash from hashes string
                subject.hashFound(word, hashtext);
                //System.out.println("==" + word + " - " + hashtext);
            } else {
                // 1 credit used
                creditsUsed = 1;
            }

        } catch (NoSuchAlgorithmException | RemoteException e) {
            throw new RuntimeException(e);
        }
        return creditsUsed;
    }
}
