package com.moeworx.a7reeg;

public class GamePlayer  implements  java.io.Serializable{

    User user;

    public GamePlayer(User user){
        this.user = user;
    }


    public String toString(){
        return user.toString();
    }

    public boolean isCpu(){
        return user.isCpu();
    }


    int points;
    public int getPoints(){
        return points;
    }
    public void setPoints(int points){
        this.points = points;
    }


}
