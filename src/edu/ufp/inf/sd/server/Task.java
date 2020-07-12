package edu.ufp.inf.sd.server;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * @author rmoreira
 */
public class Task implements Serializable {

    private Integer id;
    private String name;
    private boolean paused;
    private String file;
    private int start;
    private int delta;
    private String[] hashes;
    private final ArrayList<String> words = new ArrayList<>();
    private long startedAt;

    public Task(String name, String file, int start, int delta, String[] hashes) {
        Random rand = new Random();
        this.id = rand.nextInt(100000);
        this.name = name;
        this.file = file;
        this.start = start;
        this.delta = delta;
        this.hashes = hashes;
    }

    public Task(String name, ArrayList<String> words, String[] hashes) {
        Random rand = new Random();
        this.id = rand.nextInt(100000);
        this.name = name;
        this.words.addAll(words);
        this.hashes = hashes;
    }

    public long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(long startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public String toString() {
        return "\nSubTask {" + "id=" + id + ", belongsTo=" + name + ", startLine= " + start + ", delta= " + delta +'}';
    }

    public ArrayList<String> getWords() {
        return this.words;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPaused() {
        return paused;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public String[] getHashes() {
        return hashes;
    }

    public void setHashes(String[] hashes) {
        this.hashes = hashes;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }
}
