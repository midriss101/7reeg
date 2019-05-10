package com.moeworx.a7reeg;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GameActivity extends AppCompatActivity implements RoundListenerInterface{

    Game game;
    Game getGame(){
        return game;
    }

    List<User> users;
    User user;


    Round currentRound;
    public Round getCurrentRound(){
        return currentRound;
    }


    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPlayers();

        //init game objects
        initGameObjects();

        //init game scene
        initGameScene3();

        //TODO this is a workaround for the following
        //in startRound(): if listeners are added before Round.startPlay(), the ensuing call to listener.onNextTurn will result in a NulllPointerException as BankButton is not yet initiated
        //if Round.startPlay() is called first, listeners are notified before GameActivity is notified
        //only applies to first turn
        onNextTurn();
    }




    String userName;

    /**
     * Initializes players with one player representing the user (userPlayer) and three CPU players
     */
    private void initPlayers(){
        Intent intent = getIntent();

        //user
        userName = ((Intent) intent).getStringExtra(MainActivity.EXTRA_USER);

        //for now::
        //init users, game, game players,
        users = new ArrayList<User>();

        user = new User(userName);
        users.add(user);

        User u = new User("player 1");
        u.setCpu(true);
        users.add(u);

        u = new User("player 2");
        u.setCpu(true);
        users.add(u);

        u = new User("player 3");
        u.setCpu(true);
        users.add(u);
    }


    /**
     * - Initializes game
     * - Initializes deck - todo deck is initialized as a double deck, change this to a configurable game parameter that defaults to 2.
     * - draw to decide seating
     * - start first round
     */
    private void initGameObjects(){
        game = new Game(users, userName);

        //init deck, assume 2 "single" decks for now
        game.initDeck(2);

        drawToDecideSeating();

        startRound();
    }





    HashMap<RoundPlayer, PlayerLayoutWrapper> playerAvatars = new HashMap();



    /*
    User player deck:
        Data model is userPlayer hand... the whole thing, not only the ones still in hand... inc. what's on floor.
        This is necessary to make changes to underlying array from CardContainerLayout.
        When presenting cards in UI, floor cards will be filtered out. This is done in CardContainerLayout.
    bankBuffer:
        Displays card drawn from bankBuffer by user player, to be dragged onto hand or reject.
        No data model, addCards() will be called by bankButton.onClick()

     */

    ConstraintLayout rootLayout;
    CardContainerLayoutWrapper deck, reject, bankBuffer;
    CardContainerLayoutWrapper floor0, floor1, floor2, floor3;
    PlayerLayoutWrapper avatar0, avatar1, avatar2, avatar3;





    //activity_game_3/4
    private void initGameScene3(){
        setContentView(R.layout.activity_game_4);
        rootLayout = findViewById(R.id.rootLayout);

        //bankButton
        ImageButton bankButton = (ImageButton) findViewById(R.id.bankButton);
        bankButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                drawFromBank();
            }
        });


        //User player: create and add to map but don't display
        avatar0 = new PlayerLayoutWrapper(this,null,currentRound.userPlayer,LinearLayout.VERTICAL , Color.DKGRAY , getResources().getDrawable(R.drawable.avatar_1_24));
        playerAvatars.put(currentRound.userPlayer,avatar0);

        //player 1:
        LinearLayout xL = findViewById(R.id.avatar1);
        avatar1 = new PlayerLayoutWrapper(this,xL,currentRound.getPlayers().get(1),LinearLayout.VERTICAL , Color.RED , getResources().getDrawable(R.drawable.avatar_1_24));
        playerAvatars.put(currentRound.getPlayers().get(1),avatar1);
        avatar1.init();


        //player 2
        xL = findViewById(R.id.avatar2);
        avatar2 = new PlayerLayoutWrapper(this,xL,currentRound.getPlayers().get(2),LinearLayout.HORIZONTAL , Color.BLUE , getResources().getDrawable(R.drawable.avatar_2_24));
        playerAvatars.put(currentRound.getPlayers().get(2),avatar2);
        avatar2.init();

        //player 3
        xL = findViewById(R.id.avatar3);
        avatar3 = new PlayerLayoutWrapper(this,xL,currentRound.getPlayers().get(3),LinearLayout.VERTICAL , Color.GREEN , getResources().getDrawable(R.drawable.avatar_3_24));
        playerAvatars.put(currentRound.getPlayers().get(3),avatar3);
        avatar3.init();

        //deck
        xL = findViewById(R.id.deck);
        deck = new CardContainerLayoutWrapper(this , xL , "deck" , CardContainerLayoutWrapper.CARD_CONTAINER_TYPE , 0 , Color.BLACK);
        deck.setup();
        deck.setCards(currentRound.getUserPlayer().getHand());
        deck.layoutContainer();

        xL = findViewById(R.id.reject);
        reject = new CardContainerLayoutWrapper(this , xL , "reject" , CardContainerLayoutWrapper.CARD_CONTAINER_TYPE , 0 , Color.BLACK);
        reject.numCardsToShow=4;
        reject.setup();
        reject.setCards(currentRound.getReject());




        xL = findViewById(R.id.bankBuffer);
        bankBuffer = new CardContainerLayoutWrapper(this , xL , "bank_buffer" , CardContainerLayoutWrapper.CARD_CONTAINER_TYPE , 0 , Color.BLACK);
        bankBuffer.setup();

        xL = findViewById(R.id.floor0);
        floor0 = new CardContainerLayoutWrapper(this , xL , "floor0" , CardContainerLayoutWrapper.CARD_SET_CONTAINER_TYPE , 0 , avatar0.colorCode);
        floor0.setup();
        floor0.setCardSets(avatar0.player.floorSets);
        avatar0.floor = floor0;

        xL = findViewById(R.id.floor1);
        floor1 = new CardContainerLayoutWrapper(this , xL , "floor1" , CardContainerLayoutWrapper.CARD_SET_CONTAINER_TYPE , 90 , avatar1.colorCode);
        floor1.setup();
        floor1.setCardSets(avatar1.player.floorSets);
        avatar1.floor = floor1;

        xL = findViewById(R.id.floor2);
        floor2 = new CardContainerLayoutWrapper(this , xL , "floor2" , CardContainerLayoutWrapper.CARD_SET_CONTAINER_TYPE , 180 , avatar2.colorCode);
        floor2.setup();
        floor2.setCardSets(avatar2.player.floorSets);
        avatar2.floor = floor2;

        xL = findViewById(R.id.floor3);
        floor3 = new CardContainerLayoutWrapper(this , xL , "floor3" , CardContainerLayoutWrapper.CARD_SET_CONTAINER_TYPE , 270 , avatar3.colorCode);
        floor3.setup();
        floor3.setCardSets(avatar3.player.floorSets);
        avatar3.floor = floor3;

    }




    /*
    - remove card from drawnCardBuffer
    - remove card from sourceDataModel
    - add card to deck
    - add card to targetDataModelAf
     */
    void bank2deck(CardLayout cardLayout, int destinationIndex){
debugUserPlay("b2d before");
        bankBuffer.removeCard(cardLayout);
        deck.insertCard(cardLayout.card , destinationIndex);
debugUserPlay("b2d after insert");
        doUserPlayerSetAssessment(destinationIndex);
debugUserPlay("b2d after set assessment");
        currentRound.drawComplete();
    }

    //6
    void reject2deck(CardLayout cardLayout, int destinationIndex){
        //todo if user player doesn't move to floor (i.e. if user's next action is throw reject & reject card is not the same one drawn from reject)
            //don't accept new reject
            //return drawn card to reject
            //don't call round.drawComplete so user can still draw from bank
debugUserPlay("r2d before");
        reject.removeCard(cardLayout);
        deck.insertCard(cardLayout.card , destinationIndex);
debugUserPlay("r2d after insert");
        doUserPlayerSetAssessment(destinationIndex);
debugUserPlay("r2d after set assessment");
        currentRound.drawComplete();
    }

    void bankToReject(CardLayout cardLayout){
        bankBuffer.removeCard(cardLayout);
        currentRound.reject.add(cardLayout.card);
        currentRound.drawComplete();
        currentRound.rejectComplete(cardLayout.card);
    }

    //3
    void deck2deck(CardLayout cardLayout , int targetPosition){
        //
System.out.println("d2d: targetP=" +targetPosition + " - " + deck.layout.getChildCount() + " cardLayouts - " + deck.cards.size() + " cards");

        if(targetPosition>deck.layout.getChildCount() || targetPosition==deck.layout.getChildCount())  {
System.out.println("moving targetPosition to max value " + (deck.layout.getChildCount()-1));
            targetPosition = deck.layout.getChildCount()-1;
        }

debugUserPlay("d2d before");
        //todo before moving card: if it is part of a formed set, remove card from set and if so break/unhighlight the set
        //change card index
        int p = deck.changeCardPosition(cardLayout , targetPosition);
debugUserPlay("d2d after position change");
        doUserPlayerSetAssessment(p);
debugUserPlay("d2d after set assessment");
    }

    //2
    void deck2reject(CardLayout cardLayout){
        deck.removeCard(cardLayout);
        reject.addCard(cardLayout.card);
        currentRound.rejectComplete(cardLayout.card);
    }

    boolean checkUserPlayerFloorMove = false;

    //4
    /*
    Move set to floor
        Condition:  card should be part of a set
                    drop target should be floor0
        1. determine set of dragged card
        2. display on floor
        3. Sets a flag to be checked by Round.rejectComplete() to analyze if userPlayer floor sets fulfill requirements, if not it will call reverseDeck2Floor
    Move card to floor
        Condition:  player.onFloor should be true
                    the drop target should be a set to which dragged card is addable

    //todo userPlayer covering own set

     */
    /*void deck2floor(CardLayout cardLayout){
        debugUserPlay("d2f before");
        CardSet set = currentRound.userPlayer.getPotentialCardSet(cardLayout.card);
        deck.removeAllRetainModel(set);
        floor0.addCardSet(set);
        checkUserPlayerFloorMove = true;
        debugUserPlay("d2f after");
    }*/



    /*
    1. determine action
    2. determine target set
    3. card addable? add
     */
    boolean deck2floor(CardLayout cardLayout , String floorName , float x, float y){
        debugUserPlay("d2f before");

        CardContainerLayoutWrapper floor = null;
        if(floorName.equals("floor0")){
            floor = floor0;
        }
        else if(floorName.equals("floor1")){
            floor = floor1;
        }
        else if(floorName.equals("floor2")){
            floor = floor2;
        }
        else if(floorName.equals("floor3")){
            floor = floor3;
        }

        CardSet setToCover = null;


        //1
        //1.1   dest=floor0
        if(floor.equals(floor0)) {
            //1.1.1 card is part of a set on hand => move set to floor0
            CardSet set = currentRound.userPlayer.getPotentialCardSet(cardLayout.card);
            if(set!=null){
                deck.removeAllRetainModel(set);
                floor0.addCardSet(set);
                checkUserPlayerFloorMove = true;
            }

            //1.1.2 drop target is a set that accepts the dragged card => card to set at drop point
            else setToCover = getTargetSetToCover(floor,x,y);

            //combination of the above???
        }

        //1.2   dest=floor1..3
        else {
            //1.2.1 drop target is a set that accepts the dragged card => card to set at drop point
            setToCover = getTargetSetToCover(floor,x,y);
        }


        if(setToCover==null) {

            debugUserPlay("d2f after:: setToCover NULL - returning false ");return false;
        }


        //3
        if(setToCover.addable(cardLayout.card)){
System.out.println("d2f:: set to cover: " + setToCover.getContentString() + ":: " + cardLayout.card.getContentString() + " is addable");

            //remove
            deck.removeCardRetainModel(cardLayout);
            if(!floor.equals(floor0)){
                currentRound.userPlayer.addedToOtherPlayersFloorSets.add(cardLayout.card);
            }

            //add
            setToCover.addCard(cardLayout.card);
            if(!floor.equals(floor0)){
                setToCover.player.addedFromOtherPlayersToFloorSets.add(cardLayout.card);
            }
            floor.layoutContainer();
            debugUserPlay("d2f after - true ");return true;
        }

        debugUserPlay("d2f after - false ");return false;
    }



    private CardSet getTargetSetToCover(CardContainerLayoutWrapper floor , float x , float y){
        CardSet theOne = null;

        //2.1 if target floor has no sets return
        if(floor.cardSets.size()==0){
            return null;
        }

        //2.2 if target floor has 1 set, theOne
        else if(floor.cardSets.size()==1){
            theOne = floor.cardSets.get(0);
        }

        //2.3 if target floor has more sets: i0=0...for i1..iN
        else{
System.out.println("@GA.getTargetSetToCover() - target set position x " +x + " OR y " + y);



            int theIndex = -1;
            float[] indices = null;
            float xOrY = -1;

            if(floor.rotation==0 || floor.rotation==180) {
                indices = floor.getSetsXs();
                xOrY = x;
            }
            else if(floor.rotation==90 || floor.rotation==270){
                indices = floor.getSetsYs();
                xOrY = y;
            }

            if(indices==null){
System.out.println("NULL INDICES");
            }


System.out.println("sets indices for " + floor.containerName + " -- xOrY=" + xOrY);
StringBuffer sb = new StringBuffer();
for(int i=0 ; i < indices.length ; i++){
    sb.append(indices[i]);
    if(i<indices.length-1)  sb.append(" - ");
}
System.out.println(sb.toString() + "\n");

            //try to locate set up to member before last
            for(int i = 0 ; i < indices.length-1 ;i++){
                float x1=indices[i];
                float x2 = indices[i+1];
System.out.println("\tlocating " + xOrY + " between " + x1 + " and " + x2);

                if(x1==xOrY || (x1<xOrY && xOrY<x2)){
                    theIndex = i;   break;
                }
            }

            //if not located, place at last set
            if(theIndex==-1)    theIndex = indices.length-1;

            theOne = floor.cardSets.get(theIndex);
System.out.println("resultant index " + theIndex + " - set " + theOne.getContentString());

        }

        return theOne;
    }


    private void debugUserPlay(String code){
        StringBuffer sb = new StringBuffer();

        System.out.println("*******************************************\n");
        for(int i=0 ; i < deck.cards.size() ; i++){
            Card cX = deck.cards.get(i);    Card cY = null;
            if(deck.layout.getChildAt(i)!=null) cY=((CardLayout)deck.layout.getChildAt(i)).card;

            sb.append(i + ": ");

            if(cX.equals(cY))   sb.append("OK: " + cX.getContentString() + "\n");
            else{
                sb.append("NOK: hand (" + cX.getContentString() + ") ; ");
                if(cY!=null)    sb.append("layout (" + cY.getContentString() + ")" + "\n");
                else sb.append("layout null\n");
            }

        }

        System.out.println(code);
        System.out.println(sb.toString());
        System.out.println("*******************************************\n");
    }

    private void doUserPlayerSetAssessment(int position){
System.out.println("@GA.doUserPlayerSetAssessment()::position=" + position);

        //create set
        CardSet set = detectUserPlayerSet(position);

        //only accept set if it has 3 or more members
        //if 6 members, split into 2 sets
        if(set!=null && set.getNonNullMembers().size() > 2){
System.out.println("set detected " + set.getContentString());

            //first remove set cards from any potentialSets they might be in
            for(Iterator<CardSet> i =currentRound.userPlayer.potentialSets.iterator() ; i.hasNext() ; ){
                CardSet setX = i.next();

    for(Iterator<Card> j = set.iterator() ; j.hasNext() ;){
        Card c = j.next();
        if(setX.contains(c)){
            System.out.println(c.getContentString() + " found in " + setX.getContentString());
            setX.remove(c);
        }
    }
    //put back?
    //setX.removeAll(set);

                //if other set from which card is removed is highlighted, remove highlighting...
                if(setX.getNonNullMembers().size()<3){
                    //unhighlight and remove from potentialSets
                    deck.unhighlightSet(setX);
                    currentRound.userPlayer.potentialSets.remove(setX);
                    System.out.println("removing & unhighlighting potential set " + setX.getContentString() + " for nr members");
                }
                //else, re-"qualify" set and if found NOT floor-ready  unhighlight and remove from potentialSets
                else{
                    if(!setX.prepareForFloor()){
                        deck.unhighlightSet(setX);
                        currentRound.userPlayer.potentialSets.remove(setX);
                        System.out.println("removing & unhighlighting potential set " + setX.getContentString() + " for incompleteness");
                    }
                }
            }

            //highlight new set if floor ready
            if( set.prepareForFloor() ){
                //add set to potentialSets and highlight on hand
                currentRound.userPlayer.potentialSets.add(set);
                deck.highlightSet(set);
System.out.println("adding & highlighting new set " + set.getContentString());
            }
        }

    }


    /**
    1. Form:
        - 1.2 move up:
            Repeat
                next card associateable?
                    Y:  Create set / add card
                    N: terminate
        - 1.2 move down:
            Repeat
                next card associateable?
                    Y:  Create set / add card
                    N: terminate

    2. Assess
        - complete set?
            - calc floor values
            - add to RoundPlayer.potentialSets
            - highlight
        -
     */
    CardSet detectUserPlayerSet(int cardPosition){


        Card cX = currentRound.userPlayer.hand.get(cardPosition);
        CardSet set = null;

System.out.println("detecting set in position " + cardPosition);

System.out.println("@" + cX.getContentString() + " backward");

        //back
        for(int i=cardPosition-1 ; i > -1 ; i--){
            Card cY = currentRound.userPlayer.hand.get(i);
System.out.println("\t with " + cY.getContentString());


            if(cX.isAce()){
                cX.debug = true;
                cX.setAceModeInRelationToCard(cY);
            }
            if(cY.isAce()){
                cY.debug = true;
                cY.setAceModeInRelationToCard(cX);
            }

System.out.println("detectUserPlayerSet:: 1/" + cX.getContentString() + " - " + cY.getContentString());

            if(set==null && CardSet.associateable(cX,cY)!=-1){
System.out.println("detectUserPlayerSet:: 2/" + cX.getContentString() + " - " + cY.getContentString());
                set = CardSet.createSet(currentRound.userPlayer , cX,cY);
System.out.println("\tdetectUserPlayerSet:: created " + set.getContentString() + " - " + Card.getDenominationString(set.lowerBound) + " to " + Card.getDenominationString(set.upperBound) );
            }
            else if(set!=null && set.addable(cY)){
System.out.println("\t\t2");
System.out.println("\tdetectUserPlayerSet:: adding " + cY.getContentString() + " to " + set.getContentString() );
                set.addCard(cY);
                cX=cY;
System.out.println("\tdetectUserPlayerSet:: result "  + set.getContentString() );
            }
            else break;
        }

        //reset start position to original card
        cX = currentRound.userPlayer.hand.get(cardPosition);

System.out.println("@" + cX.getContentString() + " forward");

        //forward
        for(int i = cardPosition+1 ; i < currentRound.userPlayer.hand.size() ; i++){
            Card cY = currentRound.userPlayer.hand.get(i);

            if(cX.isAce())  cX.setAceModeInRelationToCard(cY);
            if(cY.isAce()) cY.setAceModeInRelationToCard(cX);

System.out.println("\t with " + cY.getContentString());
            if(set==null && CardSet.associateable(cX,cY)!=-1){
                set = CardSet.createSet(currentRound.userPlayer , cX,cY);
System.out.println("detectUserPlayerSet:: created " + set.getContentString() + " - " + Card.getDenominationString(set.lowerBound) + " to " + Card.getDenominationString(set.upperBound) );
System.out.println("\t\t1");
            }
            else if(set!=null && set.addable(cY)){
System.out.println("\t\t2");
                set.addCard(cY);
                cX=cY;
            }
            else break;
        }

if(set!=null)   System.out.println("result: " + set.getContentString());

        return set;
    }

    //******************  START GAME SCENE *************************






    //******************  END GAME SCENE *************************






    //******************  START GAME OBJECTS *************************

    /**
     * NOT IMPLEMENTED
    Each player draws a card, players who draw cards with equal denominations must draw again.
    Player are seated in a counter-clockwise fashion, highest score first. Highest score picks seat.
    Game.getPlayers() is sorted accordingly.
     */
    private void drawToDecideSeating(){
        game.setUserPlayer(game.getPlayers().get(0));
    }

    private void startRound(){
        currentRound = new Round(game);
        currentRound.initRound();
        currentRound.deal();
        currentRound.setRoundListener(this);
        currentRound.startPlay();
    }





    //******************  END GAME OBJECTS *************************



    //***************** END RoundInterface implementation ******************************

    /**
    - Human player: this method enables bankButton for the user to draw
    - CPU player: calls RoundPlayer.cpuPlayTurn()

     //TODO highlight player whose turn it is
     */
    @Override
    public void onNextTurn() {
System.out.println("++++++++++++++++++++++ @GameActivity.onNextTurn() --- " + currentRound.turnPlayer + " ---- user turn " + currentRound.userTurn());

        //todo: improve this if round deck (bank) has no more cards: end round - commonly last card is not drawn, game parameter?
        if(currentRound.deck.size()==1){
            System.out.println("One card left in bank - ending round without a winner");
            System.exit(0);
        }


        //if cpuPlayer, play
        if(currentRound.turnPlayer.isCpu()){
            currentRound.turnPlayer.cpuPlayTurn();
        }



    }



    /**
     * NOT IMPLEMENTED
        TODO When it's the user's turn,  highlights rejectLayout to remind user to throw reject

     */
    @Override
    public void playerHasDrawn() {


System.out.println("++++++++++++++++++++++ @GameActivity.playerHasDrawn() --- " + currentRound.turnPlayer + " ---- user turn " + currentRound.userTurn() );

    }


    /**
 *      Redraws rejectLayout when CPU player throws reject
        - TODO When it's the user's turn, un-highlights rejectLayout
     */
    @Override
    public void playerThrewReject(Card card) {
System.out.println("++++++++++++++++++++++ @GameActivity.playerThrewReject() --- " + currentRound.turnPlayer + " ---- user turn " + currentRound.userTurn());

        //if not user player, display rejected card
        if(!currentRound.userTurn()){
            PlayerLayoutWrapper pl = playerAvatars.get(currentRound.turnPlayer);
            pl.textView.setText(currentRound.turnPlayer.getStatusText());


            //animateCpuPlayerReject(card);
            reject.layoutContainer();
        }

    }



    //todo 50
    @Override
    public void endRound(RoundPlayer roundWinner){
        //decrement winner score
        roundWinner.setPoints(roundWinner.getPoints()-1);

        //increment each losers' score by num cards
        for(Iterator<RoundPlayer> i = currentRound.getPlayers().iterator() ; i.hasNext() ; ){
            RoundPlayer rp = i.next();
            if(rp.equals(roundWinner))  continue;
            rp.setPoints(rp.getPoints() + rp.getRemainingCards());
        }


        //todo declare round end
        System.out.println("#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$# ROUND END $#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$");
        for(Iterator<RoundPlayer> i = currentRound.getPlayers().iterator() ; i.hasNext() ; ) {
            RoundPlayer rp = i.next();
            System.out.print(rp + ": " + rp.getPoints() + " points");
            if (rp.equals(roundWinner)) System.out.print(" -- WINNER");
            if(i.hasNext()) System.out.println();
        }
        System.out.println("#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$#$");

        //end game or start new round



    }

    /*

        Used for CPU player floor moves.

        Todo... rotating players' array
        Based on rotating players' array
     */
    @Override
    public void moveToFloor(CardSet set, RoundPlayer player) {
System.out.println("@GameActivity.moveToFloor() - " + set.getContentString() + " for player " + player);
        PlayerLayoutWrapper playerLayout = playerAvatars.get(player);
        playerLayout.getFloor().addCardSet(set);
    }

    /*

     */
    private void redrawPlayerFloor(RoundPlayer player) {
        playerAvatars.get(player).floor.layoutContainer();
    }



    /*

     */
    @Override
    public void updateFloor(RoundPlayer player){
        redrawPlayerFloor(player);
    }
    //***************** END RoundInterface implementation ******************************


    //***************** START USER ACTIONS ******************************

    public void drawFromBank(){
        if(currentRound.userTurn() && currentRound.turnStage==Round.DRAW_TURN_STAGE && !currentRound.userPlayer.hasDrawn()) {
            final Card drawnCard = currentRound.getDeck().get(0);
            bankBuffer.addCard(drawnCard);
            currentRound.userPlayer.setDrawn(true);
        }
    }



    //***************** END USER ACTIONS ********************************

}
