package com.moeworx.a7reeg;

public class User  implements  java.io.Serializable{

    String userName;
    public String getUserName(){
        return userName;
    }


    public User(String userName){
        this.userName = userName;
    }


    public String toString(){
        return getUserName();
    }

    boolean cpu = false;

    public boolean isCpu() {
        return cpu;
    }

    public void setCpu(boolean cpu) {
        this.cpu = cpu;
    }
}
