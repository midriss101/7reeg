package com.moeworx.a7reeg;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Game  implements  java.io.Serializable{

    List<GamePlayer> players = new ArrayList<GamePlayer>();
    public List<GamePlayer> getPlayers(){
        return players;
    }

    /*
    The game player belonging to our user
     */
    GamePlayer userPlayer;
    public void setUserPlayer(GamePlayer userPlayer){
        this.userPlayer = userPlayer;
    }
    public GamePlayer getUserPlayer(){
        return userPlayer;
    }




    public Game(List<User> users, String userName){
        //init players
        for(Iterator<User> i = users.iterator() ; i.hasNext() ; ){
            GamePlayer gp = new GamePlayer(i.next());
            players.add(gp);
            if(gp.user.getUserName().equals(userName)){
                userPlayer = gp;
            }
        }
    }

    List<Deck> deckObjects = new ArrayList();

    /* the contents (i.e. cards) of 1..n Deck objects, each modelling a 52 cards "total" deck */
    List<Card> deck = new ArrayList();
    public List<Card> getDeck(){
        return deck;
    }


    int numPlayingDecks;
    int getNumPlayingDecks(){
        return  numPlayingDecks;
    }


    public void initDeck(int numPlayingDecks){
        this.numPlayingDecks = numPlayingDecks;


        String[] bgImages = new String[]{"@drawable/card_bg_1" , "@drawable/card_bg_2"};


        //start "single" deck
        for(int i=0; i < numPlayingDecks ; i++){

            //create Deck instance and add to Deck objects
            Deck deckObj = new Deck(bgImages[i]);
            deckObjects.add(deckObj);

            //start card type
            for (int j = 1; j < 5; j++) {

                //start card denomination
                for (int k = 1; k < 14; k++) {
                    //add card: denomination=k; type==j;bgImageString=bgImages[i]
                    deck.add(new Card(k,j,deckObj));
                }
            }

        }
    }


    // ------------------------- BEGIN GAME PARAMETERS -----------------------------------------
    public static int MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED = 6;
    public static int MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIRST_MOVER = -1;

    public static int MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED = 51;
    public static int MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIRST_MOVER = -1;


    int moveToFloorNumberCardsMethod = MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED;
    public int getMoveToFloorNumberCardsMethod(){
        return moveToFloorNumberCardsMethod;
    }
    public void setMoveToFloorNumberCardsMethod(int x){
        moveToFloorNumberCardsMethod = x;
    }


    int moveToFloorValueCardsMethod = MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED;
    public int getMoveToFloorValueCardsMethod(){
        return moveToFloorValueCardsMethod;
    }
    public void setMoveToFloorValueCardsMethod(int x){
        moveToFloorValueCardsMethod = x;
    }

    // ------------------------- END GAME PARAMETERS -------------------------------------------

}
