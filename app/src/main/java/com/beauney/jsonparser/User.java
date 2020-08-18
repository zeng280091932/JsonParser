package com.beauney.jsonparser;

/**
 * @author zengjiantao
 * @since 2020-08-18
 */
public class User {
    private String name;

    private String password;

    private boolean isVip;

    public User() {
    }

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public User(String name, String password, boolean isVip) {
        this.name = name;
        this.password = password;
        this.isVip = isVip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", password='" + password + '\'' +
                ", isVip=" + isVip +
                '}';
    }
}
