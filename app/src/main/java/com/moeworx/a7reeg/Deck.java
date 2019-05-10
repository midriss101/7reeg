package com.moeworx.a7reeg;

/*
Basically only a holder of BG image, and ensures that Card.equals() also considers the fact that two identical cards
(e.g. 2 aces of spades) are different because they come from different decks
 */
public class Deck  implements  java.io.Serializable{

    String bgImageFileName;

    public Deck(String bgImageFileName){
        this.bgImageFileName = bgImageFileName;
    }


    //@Override
    /*public boolean equals(Object obj) {
        return this.hashCode()==obj.hashCode();
    }*/

    public String toString(){
        return  bgImageFileName;
    }
}
