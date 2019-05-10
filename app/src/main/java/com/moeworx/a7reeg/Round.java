package com.moeworx.a7reeg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Round  implements  java.io.Serializable{

    Game game;
    public Game getGame(){
        return game;
    }



    List<RoundPlayer> players = new ArrayList<RoundPlayer>();
    public List<RoundPlayer> getPlayers(){
        return players;
    }
    public int getPlayerPosition(RoundPlayer player){
        return getPlayers().indexOf(player);
    }



    /* The round deck: all cards in player hands, bankBuffer, floor and reject*/
    List<Card> deck = new ArrayList<Card>();
    public List<Card> getDeck(){
        return deck;
    }


    List<Card> reject = new ArrayList<Card>();
    public List<Card> getReject(){ return reject;}


    public int getGameDeckNumCards(){
        return game.deck.size();
    }


    RoundPlayer userPlayer;
    public RoundPlayer getUserPlayer(){
        return userPlayer;
    }


    /*
    whether turn is game user's
     */
    public boolean userTurn(){
        return userPlayer.equals(turnPlayer);
    }


    /*
    Winner of last round OR (if first round) GameOwner (userPlayer in single-mode)
     */
    RoundPlayer roundOwner;

    int roundOwnerIndex = -1;
    public int getRoundOwnerIndex(){
        if(roundOwnerIndex==-1){
            roundOwnerIndex = players.indexOf(roundOwner);
        }
        return roundOwnerIndex;
    }


    /* A rotating assignment */
    RoundPlayer turnPlayer;
    RoundPlayer previousTurnPlayer;


    public Round(Game game){
        this.game = game;
    }

    /**
     * Initializes round:
     *  - Creates RoundPlayer for each GamePlayer and adds to players array
     *  - Assigns roundOwner (the one on whose behalf CPU deals cards, and who has the first turn); todo this is currently assigned to user, should be ....
     *  - Initializes and shuffles round deck
     *
     */
    public void initRound(){
        //init players
        for(Iterator<GamePlayer> i = game.getPlayers().iterator() ; i.hasNext() ; ){
            GamePlayer gp = i.next();
            RoundPlayer rp = new RoundPlayer(gp, this);
            players.add(rp);

            //assign userPlayer
            if(rp.gamePlayer.equals(game.userPlayer)){
                userPlayer = rp;
            }
        }

        //assign roundOwner, for now
        //TODO should be last round winner
        roundOwner = userPlayer;

        //assign turnPlayer

        //init deck
        //copy cards from game deck
        for(int i=0 ; i<game.getDeck().size() ; i++){
            Card card = game.getDeck().get(i);
            deck.add(new Card(card.getDenomination() , card.getType() , card.getDeck()));
        }

        shuffleDeck();
System.out.println("@Round.initRound() with " + deck.size() + " cards from " + game.numPlayingDecks + " decks");
    }


    /* shuffles deck position */
    private void shuffleDeck(){
        Collections.shuffle(deck);
    }


    RoundListenerInterface roundListner;
    public void setRoundListener(RoundListenerInterface listenerInterface){
        roundListner = listenerInterface;
    }

    private void dumpRound(){
        //dump deck
        System.out.println("&&&&&&&&&&&&&&&&&&& DUMPING ROUND DECK &&&&&&&&&&&&&&&&&&&&&&&");
        for(Iterator<Card> i = deck.iterator() ; i.hasNext() ; ) {
            Card card = i.next();
            System.out.print(card.getDenominationString() + card.getTypeString() + "\t");
        }
        System.out.println();
    }


    //********************************* GAME DYNAMICS *****************************************


    /**
    Deals cards from the perspective of userPlayer; starting with player whose seating order is just higher than userPlayer and continue clockwise
    Step 1: re-orders getPlayers()
    Step 2: deals cards; TODO deal in 2s
    Step 3: restore original getPlayers() order, which is the play order last ordered by GameActivity.drawToDecideSeating
     */
    public void deal(){
        //Step 1. userPlayer seating order!

        //switch userPlayer
        //case 0: start dealing with lowest seating order (3) -> 3,2,1,0
        //case 1: start dealing with +1 seating order (0) -> 0,3,2,1
        //case 2: start dealing with +1 seating order (1) -> 1,0,3,2
        //case 3: start dealing with +1 seating order (2) -> 2,1,0,3


        //deck
        Iterator<Card> deckIterator = getDeck().iterator();
        //List<RoundPlayer> dealingOrder = new ArrayList<>(currentRound.getPlayers().size());
        int iN = getRoundOwnerIndex();

        //if the round owner is the first in round.getPlayers() (last ordered by GameActivity.drawToDecideSeating to reflect play order)
        // just reverse array
        if(iN==0) {
            //Collections.copy(currentRound.getPlayers(), dealingOrder);
            Collections.reverse(getPlayers());
        }
        //TODO the other cases
        else{
        }


        //Step 2

        for(int dealRoundCtr=0 ; dealRoundCtr<14 ; dealRoundCtr++){

            if(iN==0) {
                //for (int i = currentRound.getPlayers().size(); i < 1; i--) {
                //  RoundPlayer rp = currentRound.getPlayers().get(i);
                for (Iterator<RoundPlayer> i = getPlayers().iterator() ; i.hasNext();) {
                    RoundPlayer rp = i.next();
                    rp.getHand().add(deckIterator.next());
                    deckIterator.remove();
                }
            }

            /*else {
                for (int i = getRoundOwnerIndex() -1; i > -1; i--) {
                    RoundPlayer rp = currentRound.getPlayers().get(i);
                    rp.getHand().add(deckIterator.next());
                    deckIterator.remove();
                }

                //iterate "up through roundPlayers, starting from end, up to userPlayer
                for (int i = currentRound.getPlayers().size() - 1; i <roundOwnerIndex; i--) {
                    RoundPlayer rp = currentRound.getPlayers().get(i);
                    rp.getHand().add(deckIterator.next());
                    deckIterator.remove();
                }
            }
            */
        }



        //Step 3: return players to originalOrder
        if(iN==0) {
            Collections.reverse(getPlayers());
        }
        //TODO the other cases


        //dump
        /*for(Iterator<RoundPlayer> i = getPlayers().iterator() ; i.hasNext() ; ){
            i.next().debugHand();
        }*/
    }




    public int turn = -1;//1..nr. players


    /*
    0: draw
    1: throw reject
     */
    public int turnStage;

    public static int DRAW_TURN_STAGE = 0;
    public static int  THROW_REJECT_TURN_STAGE = 1;

    public static int MAX_TURN_STAGE = 1;


    public void startPlay(){
        nextTurn();
    }




    /**
     - flags users: turnPlayer and previousTurnPlayer
     - updates stage flags: DRAW, REJECT
     - notifies listeners
    TODO Collections.rotate(list,distance);
     */
    private void nextTurn(){
        //increment counter and reset turnStage
        if(turn==getPlayers().size()-1){
            turn = 0;
        }
        else {
            turn++;
        }

        turnStage = DRAW_TURN_STAGE;

        //un-flag previous turnPlayer, if exists
        if(turnPlayer!=null){
            turnPlayer.setMyTurn(false);
            previousTurnPlayer = turnPlayer;
        }

        //change and flag turnPlayer -> new turnPlayer
        turnPlayer = getPlayers().get(turn);
        turnPlayer.setMyTurn(true);

        //notify listeners
        roundListner.onNextTurn();
    }



    /*
    Called by GameActivity to notify that turnPlayer has drawn card
    Notifies listeners
     */
    public void drawComplete(){
//System.out.println("################## @Round.drawComplete() --- " + turnPlayer + " ---- user turn " + userTurn());
        turnStage = Round.THROW_REJECT_TURN_STAGE;

        //notify listeners
        roundListner.playerHasDrawn();
    }

    /*
    Called by GameActivity to notify that turnPlayer has thrown reject
    Assesses round status and if play is still on, rotates the turn
    Notifies listeners
     */
    public void rejectComplete(Card card){

        //roundPlayer stats
        turnPlayer.playsMade++;
        if(userTurn())   turnPlayer.setDrawn(false);

        //System.out.println("################## @Round.rejectComplete() --- " + turnPlayer + " ---- user turn " + userTurn() +
          //      " - plays made " + turnPlayer.playsMade);
        //System.out.println(turnPlayer.getStatusText());

        //notify first since nextTurn() will also notify
        roundListner.playerThrewReject(card);

        //end round or continue play
        if(turnPlayer.getRemainingCards()==0){
            roundListner.endRound(turnPlayer);
        }
        else {
            nextTurn();
        }
    }






    public void moveToFloor(CardSet set , RoundPlayer player){
        roundListner.moveToFloor(set,player);
    }


    public void updateFloor(RoundPlayer player){
        roundListner.updateFloor(player);
    }
    //********************************* GAME DYNAMICS *****************************************



}
