package edu.ufp.inf.sd.server;

import java.io.Serializable;

/**
 * @author rmoreira
 */
public class User implements Serializable {

    private String uname;
    private String pword;
    private Integer credits;
    private Integer creditsAuthorized;

    public User(String uname, String pword) {
        this.uname = uname;
        this.pword = pword;
        this.credits = 0;
        this.creditsAuthorized = 0;
    }

    public Integer getCredits() {
        return credits;
    }

    public void setCredits(Integer credits) {
        this.credits = credits;
    }

    public Integer getCreditsAuthorized() {
        return creditsAuthorized;
    }

    public void setCreditsAuthorized(Integer creditsAuthorized) {
        this.creditsAuthorized = creditsAuthorized;
    }

    @Override
    public String toString() {
        return "User{" + "uname=" + uname + ", pword=" + pword + ", credits=" + credits + '}';
    }

    /**
     * @return the uname
     */
    public String getUname() {
        return uname;
    }

    /**
     * @param uname the uname to set
     */
    public void setUname(String uname) {
        this.uname = uname;
    }

    /**
     * @return the pword
     */
    public String getPword() {
        return pword;
    }

    /**
     * @param pword the pword to set
     */
    public void setPword(String pword) {
        this.pword = pword;
    }
}
