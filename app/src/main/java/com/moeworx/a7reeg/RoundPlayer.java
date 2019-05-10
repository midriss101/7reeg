package com.moeworx.a7reeg;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class RoundPlayer  implements  java.io.Serializable{

    GamePlayer gamePlayer;
    public int getPoints(){
        return gamePlayer.getPoints();
    }
    public void setPoints(int points){
        gamePlayer.setPoints(points);
    }

    Round round;


    /*
        all cards user has. as the game progresses, these will also be members of the following (must, as in double-entry accounting):
        - potentialSets:
        - unmatchedCards:
        - doubles:
        - floorSets:
        - addedToOtherPlayersFloorSets:
        hand is also influenced by the acts of drawing from bankBuffer and throwing reject: card in, card out
     */
    List<Card> hand = new ArrayList<Card>();
    public List<Card> getHand(){
        return hand;
    }


    /*
        Container containing all possible sets for the player.
        In the case of the user player, this holds ONLY floor ready sets, i.e. those highlighted on user player's deck.
     */
    ArrayList<CardSet> potentialSets = new ArrayList();

    /*
    Returns a subset of potentialSets, those marked complete.
    In the case of the user player, this should return all potentialSets
     */
    public ArrayList<CardSet> getFloorReadyPotentialSets(){
        ArrayList<CardSet>  sets = new ArrayList<>();
        for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
            CardSet x = i.next();
            if(x.isComplete())  sets.add(x);
        }
        return  sets;
    }



    public CardSet getPotentialCardSet(Card card){
        for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
            CardSet x = i.next();
            if(x.contains(card))    return x;
        }
        return null;
    }

    UngroupedCardsTreeSet unmatchedCards = null;


    List<CardSet> floorSets = new ArrayList<CardSet>();

    List<Card> doubles = new ArrayList<>();

    List<Card> addedToOtherPlayersFloorSets = new ArrayList<>();

    /*
    These cards will be members of floorSets sets
     */
    List<Card> addedFromOtherPlayersToFloorSets = new ArrayList<>();


    /*
    returns the count of a subset of "hand" - only the ones still on hand (!)
        - hand (original hand) less whatever is on floor while taking into account cards added by other players to this player's floor sets
     */
    public int getRemainingCards(){
        int s = 0;
        for(Iterator<CardSet> i = floorSets.iterator() ; i.hasNext() ; ){
            s = s + i.next().size();
        }

        s = s + addedToOtherPlayersFloorSets.size() - addedFromOtherPlayersToFloorSets.size();

        return 14 - s;
    }



    void debugCpuPlay() {

        //hand
        System.out.println("******************* DUMPING HAND FOR PLAYER " +  this + " (" +hand.size()  + " cards) ***********************");
        for(int i=0 ; i<hand.size() ; i++ ){
            Card c = hand.get(i);
            System.out.println("\t" + c.getTypeString() + "s: " + c.getDenominationString() + " at position " + i);
        }
        System.out.println("*******************************************************");


            //debug potentialSets
            System.out.println("******************* POTENTIAL SETS FOR PLAYER " +  this + " (" + potentialSets.size() + " sets) ***********************");
            for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
                System.out.println("\t" + i.next().getContentString());
            }
            System.out.println("*******************************************************");


                //debug floor ready
                List x = getFloorReadyPotentialSets();
                System.out.println("******************* READY SETS FOR PLAYER " +  this + " (" + x.size() + " sets) ***********************");
                for(Iterator<CardSet> i = x.iterator() ; i.hasNext() ; ){
                    System.out.println("\t" + i.next().getContentString());
                }
                System.out.println("*******************************************************");


            //debug unmatchedCards
            if(unmatchedCards!=null) {
                System.out.println("******************* DUMPING UNGROUPED CARDS FOR PLAYER " + this + " (" + unmatchedCards.size() + " cards) ***********************");
                for (int i = 0; i < unmatchedCards.size(); i++) {
                    Card c = unmatchedCards.get(i);
                    System.out.println("\t" + c.getTypeString() + "s: " + c.getDenominationString() + " at position " + i);
                }
                System.out.println("*******************************************************");
            }
            else{
                System.out.println("******************* NULL UNGROUPED CARDS FOR PLAYER " + this + " ***********************");
            }

            //doubles
            System.out.println("******************* DUMPING DOUBLES FOR PLAYER " +  this + " (" +doubles.size()  + " cards) ***********************");
            for(Iterator<Card>  i= doubles.iterator() ; i.hasNext() ; ){
                System.out.print(i.next().getContentString());
                System.out.println();
            }

            //floor
            System.out.println("******************* FLOOR SETS FOR PLAYER " +  this + "(" + floorSets.size() + " sets) ***********************");
            for(Iterator<CardSet> i = floorSets.iterator() ; i.hasNext() ; ){
                System.out.println("\t" + i.next().getContentString());
            }
            System.out.println("*******************************************************");

    }




    public RoundPlayer(GamePlayer gamePlayer, Round round){
        this.gamePlayer = gamePlayer;
        this.round = round;
    }



    public String toString(){
        return gamePlayer.toString();
    }



    boolean handMatched = false;

    boolean myTurn = false;
    public boolean isMyTurn(){
        return myTurn;
    }
    public void setMyTurn(boolean myTurn){
        this.myTurn = myTurn;
    }


    /* only used for userPlayer */
    boolean drawn = false;
    public boolean hasDrawn(){
        return drawn;
    }
    public void setDrawn(boolean drawn){
        this.drawn = drawn;
    }


    boolean onFloor = false;
    public boolean isOnFloor(){
        return onFloor;
    }
    public void setOnFloor(boolean onFloor){
        this.onFloor = onFloor;
    }




    //******************************* START STATS **************************
    int playsMade = 0;
    int score = 0;
    //******************************* END STATS ****************************

    /*
    TODO....
    Returns text to be displayed next to player's avatar
     */
    public String getStatusText(){
        return  gamePlayer.user.getUserName() + "\n" +
                gamePlayer.getPoints() + " points";
    }





    //********************************** START CPU PLAYER ********************************************

    public boolean isCpu(){
        return gamePlayer.isCpu();
    }



    /*
        0. assess rejectCard of round.previousTurnPlayer to decide 1.1 OR 1.2
        1. draw
            1.1. draw from Bank
            1.2. draw from reject
        2. process deck
            hand, less cards on floor if any


            Keep list of "observedReject" - all cards thrown to reject. Create int inObservedRejct(Card card) to test
            whether sought card (to complete a sequence) has been thrown in reject once (return 1), twice(2) or not(0);

        3. decide next move
            2.1. move sets to floor
            2.2. move card(s) to floor
        4. decide reject
        5. throw reject
     */
    public  void cpuPlayTurn(){

System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! START " + this + " PLAY !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
debugCpuPlay();

        //0,1
        if(playsMade>0 && cpuAssessPreviousPlayerReject()){
            cpuDrawFromReject();
        }
        else{
            cpuDrawFromBank();
        }

        //2
        if(!handMatched){
            cpuMatchHand(hand);

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! POST-MATCH HAND DUMP FOR " + this + " - 1st !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            debugCpuPlay();
        }
        else{
            //clear potentialSets and doubles
            potentialSets.clear();      doubles.clear();

            //replicate hand
            List<Card> handReplica = new ArrayList<>(hand);

            //remove from replica what's already on floor
            List<Card> floorCards = new ArrayList<>();
            for(Iterator<CardSet> i = floorSets.iterator() ; i.hasNext() ; ){
                floorCards.addAll(i.next());
            }

            handReplica.removeAll(floorCards);
            cpuMatchHand(handReplica);

            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! POST-MATCH HAND DUMP FOR " + this + " - 2nd..nth !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            debugCpuPlay();
        }

        //3
        cpuAssessFloorAction();

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! POST-cpuAssessFloorAction DUMP FOR " + this + "  !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        debugCpuPlay();

        //cover others' sets
        if(onFloor){
            cpuCoverOtherPlayerSets();
        }

System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! POST-PLAY DUMP FOR " + this + " !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
debugCpuPlay();

        //4
        cpuPredictReject();

        //5
        cpuThrowReject();

System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! END " + this + " PLAY !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");

    }



    /*
    Returns true:
        -   if the card rejected by previous player fits a potential (ready) set; and
        -   player can go to floor, or is already there
    TODO card fits more than one potential set
     */
    private boolean cpuAssessPreviousPlayerReject(){
        CardSet fitSet = null;
        Card rejectCard = round.reject.get(round.reject.size()-1);
System.out.println("@RP.assessReject() - " + rejectCard.getContentString());

        //if no fit with potential sets, return false
        for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
            CardSet x  = i.next();
System.out.println("\t\tassessing addability of " + rejectCard.getContentString() + " to " + x.getContentString());
            if(x.addable(rejectCard)){
                fitSet = x; break;
            }
        }

        if(fitSet==null)    return false;
System.out.println("\t\taddable");

        //if player is already on floor, card can be taken
        if(isOnFloor()) return true;

        //not on floor::

        //compute current sum floor ready cumulative value
        int cumV = 0;
        int numCards = 0;
        for(Iterator<CardSet> i = getFloorReadyPotentialSets().iterator() ; i.hasNext() ; ){
            CardSet x = i.next();
            cumV = cumV + x.getCumulativeValue();
            numCards = numCards + x.getNonNullMembers().size();
        }


        //if fit with a complete set, add card value to cumV and assess floor readiness
        //todo revise this for ACE_HIGHER:: setAceMode/resetAceMode
        if(fitSet.isComplete()){
            cumV = cumV + rejectCard.getValue();
            numCards++;
        }

        //if completes a non-complete set, add set value to cumV and assess floor readiness
        else{
            int[] sim = fitSet.simulateAddCard(rejectCard);
            cumV = cumV + sim[0];
            numCards = numCards + sim[1];
        }

        //if floor conditions met
        //the fixed situation
        if(round.getGame().getMoveToFloorValueCardsMethod() == Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                &&
                round.getGame().moveToFloorNumberCardsMethod == Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                &&
                (
                        cumV == Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                                ||
                        cumV > Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                )
                &&
                (
                        numCards == Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                                ||
                        numCards > Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                )
        ){
            return true;
        }

        //todo the floating case

        return false;
    }


    private void cpuDrawFromReject(){
        Card rejectCard = round.reject.get(round.reject.size()-1);

        hand.add(rejectCard);

        round.reject.remove(rejectCard);

System.out.println("@RoundPlayer.cpuDrawFromBank() - " + this + " - drew REJECT " + rejectCard.getContentString());

        //notify
        round.drawComplete();
    }

    private void cpuDrawFromBank(){


        //get first card from bankBuffer
        Card card = round.getDeck().get(0);

        //add to deck
        hand.add(card);

        //remove from bankBuffer
        round.getDeck().remove(card);

System.out.println("@RoundPlayer.cpuDrawFromBank() - " + this + " - drew " + card.getContentString());

        //notify
        round.drawComplete();
    }




    class UngroupedCardsTreeSet extends TreeSet<Card>{

        UngroupedCardsTreeSet(Collection c){
            super(c);
        }

        public Card get(int index){
            int c=0;
            for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
                Card y = i.next();
                if(index==c)    return y;
                c++;
            }
            return null;
        }

        public String getContentString(){
            StringBuffer sb = new StringBuffer("{");
            for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
                Card y = i.next();
                if(y==null)    sb.append("null");
                else sb.append(y.getContentString());
                if(i.hasNext()) sb.append(",");
            }
            sb.append("}");
            return sb.toString();
        }

        @Override
        public boolean add(Card o) {
            if(o==null) return false;
System.out.println("RP.UngroupedCardsTreeSet.add() - " + o.getContentString());
            return super.add(o);
        }
    }



    /* to match
        - drawn card post 1st draw
        - TODO previous player's reject
      */
    private void cpuMatchCard(Card card){
        List<CardSet> possibleToAddTo = new ArrayList<>();

        for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
            CardSet set = i.next();
            //if card is addable, a
            if(set.addable(card)){
                possibleToAddTo.add(set);
System.out.println("@RoundPlayer.cpuMatchCard - " + this + " - " + card.getContentString() + " is addable to " + set.getContentString());
            }
        }

        //TODO assess best set to add to
        if(!possibleToAddTo.isEmpty()){
            possibleToAddTo.get(0).addCard(card);
System.out.println("@RoundPlayer.cpuMatchCard - " + this +  card.getContentString() + " is added to " + possibleToAddTo.get(0).getContentString());
        }
    }

    /**
     * Approach II
     * First time:
     *     	unload deck into unprocessedCards   (1)
 * 	        create unprocessableCards
     *
     * for each card x in unprocessedCards     (2)
     *     	Create potentialSets                (3)
     * 	    locate and create possible typeSets (4)
     * 	    sequenceSets:
         * 		try to find card y that can form a sequence with x, while abs(x-y)=1                                    (5)
         * 			exists?
         * 				create group                                                                                    (6)
         * 				try to increase the group from remaining cards in unprocessedCards                              (7)
         * 			does not exist?
         * 				try to find card y that can form a sequence with x, while abs(x-y)=2                            (8)
         * 					exists?
         * 						create group                                                                            (9)
         * 						try to increase the group from remaining cards in unprocessedCards                      (10)
         * 					does not exist?
         * 						try to find card y that can form a sequence with x, while abs(x-y)=3                    (11)
         * 							exists?
         * 								create group                                                                    (12)
         * 								try to increase the group from remaining cards in unprocessedCards              (13)
         * 							does not exist?
         * 								try to find card y that can form a sequence with x, while abs(x-y)=4            (14)
         * 									exists?
         * 										create group                                                            (15)
         * 										try to increase the group from remaining cards in unprocessedCards      (16)
         * 									does not exist?
         * 										move x to unprocessableCards                                            (17)
     */
    private void cpuMatchHand(List<Card> cardsToMatch){

        unmatchedCards = new UngroupedCardsTreeSet(cardsToMatch);
        List<CardSet> finalCandidateSets = identifySets(cardsToMatch , unmatchedCards);



        //System.out.println("--------------- PRE ----------------------");
        //debugCpuPlay();

        //18 from finalCandidateSets
        //  find set with max sexiness and place into potentialSets
        //  find set with next best sexiness AND no card conflict, place into potentialSets
        //  continue until all cards in deck are either:
        //      -   members of a CardSet in potential sets
        //OR
        //          members of unprocessableCards

int completionCounter=1;


//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
//System.out.println("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");

        //in the rare case that all cards are processed without having to qualify sexiest...
        if(terminateSelectionOfPotentialSets()){
            potentialSets.addAll(finalCandidateSets);
            finalCandidateSets.clear();
        }


        //the normal situation
        else {
            while (!terminateSelectionOfPotentialSets()) {
                System.out.println("\tcompletion loop#" + completionCounter);
                completionCounter++;

System.out.println("\t\tcalling @RoundPlayer.getSexiestSet() for " + finalCandidateSets.size() + " sets");
                CardSet x = getSexiestSet(finalCandidateSets);
                System.out.println("\t\tsexiest set this loop:" + x.getContentString());
                if (!conflictWithPotentialSets(x)) {
                    System.out.println("\t\ttno conflict... set added to potentialSets");
                    potentialSets.add(x);
                    finalCandidateSets.remove(x);

                } else {
                    finalCandidateSets.remove(x);
                    System.out.println("\t\tconflict... set removed from allPossibleSets and remaining members return to unmatchedCards");
                    moveSetMembersToUngroupedCards(x);
                }

System.out.println("dumping CPU PLAYER " + this + " cards at completion loop #" + completionCounter);
debugCpuPlay();
//System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$ remaining finalCandidateSets (" + finalCandidateSets.size() + ")  this loop $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
//for(Iterator<CardSet> i = finalCandidateSets.iterator() ; i.hasNext() ; ){
                //  System.out.println("\t" + i.next().getContentString());
//}
            }
        }

        handMatched = true;
System.out.println("======================================================================================================");
System.out.println("======================================================================================================");
    }







    /*This will be called
        - case 1. to match hand at each turn:
                here junk will be unmatchedCards
                doubles possible
        - case 2. to match leftovers from completed sets:
                junk is to be offloaded to unmatchedCards
                no doubles
     */
    private List<CardSet>  identifySets(List<Card> cards , UngroupedCardsTreeSet junk){




        //************************ HANDLING DOUBLES ************************
        //todo doubles?
        //TODO improve this: if both doubles are to be utilized, one will most likely be in a SEQ set AND the other in a TYPE set (2 type sets also happens)
        //for now, if y is identical to x (a double), place y in unprocessable cards
        //this will miss some opportunities
        for(int i = 0; i< junk.size()-2 ; i++ ) {
            Card x = junk.get(i);
            for(int j = i+1; j< junk.size()-1; j++){
                Card y= junk.get(j);
                if(x.sameCardDifferentDeck(y) && (!doubles.contains(x) || !doubles.contains(y)))  doubles.add(y);
            }
        }
        junk.removeAll(doubles);



        List<CardSet> allPossibleSets = new ArrayList<>();
        List<Card> unprocessableCards = new ArrayList<>();
        List<Card> processedCards = new ArrayList<>();


        //2
        for(Iterator<Card> i = junk.iterator(); i.hasNext() ; ) {

            Card x = i.next();
            List<CardSet> xPossibleSets = new ArrayList<>();//3




            UngroupedCardsTreeSet replica1 = new UngroupedCardsTreeSet(junk);
            replica1.remove(x);





            //4 locate and create possible typeSets
            for(Iterator<Card> j=replica1.iterator() ; j.hasNext() ; ) {
                Card y = j.next();

                //if y was processed against x previously, skip
                if(processedCards.contains(y))  continue;



                // set ace mode x,y
                if(x.isAce())   x.setAceModeInRelationToCard(y);
                if(y.isAce())   y.setAceModeInRelationToCard(x);

                int possibility = CardSet.associateable(x, y);

                //skip not possible
                if (possibility == -1) continue;

/*String pT = "Not possible";
if(possibility==CardSet.TYPE_SET)   pT = "Possible";
System.out.println("@RoundPlayer.cpuMatchHand() - Opportunity to form TYPE SET between " +
"i(" + x.getContentString() + ") and j(" + y.getContentString() + ") " + pT);
*/
                //4
                if (possibility == CardSet.TYPE_SET) {
                    CardSet setX = CardSet.createSet(this,x, y);
//System.out.println("\t4. Created and added to " + x.getContentString() + " sets: " + setX.getContentString());
                    xPossibleSets.add(setX);

                    UngroupedCardsTreeSet replica2 = new UngroupedCardsTreeSet(junk);
                    replica2.remove(x);
                    replica2.remove(y);
//System.out.println("\t4. now trying to increase members of the set formed from " + replica2.size() + " other cards in unmatchedCards after removing " + x.getContentString() + " & " + y.getContentString());
                    for(Iterator<Card> k = replica2.iterator() ; k.hasNext();){
                        Card z = k.next();

                        // set ace mode z
                        if(z.isAce())   z.setAceModeInRelationToCard(x);

//System.out.println("\tnow looking at " + z.getContentString());
                        if(setX.addable(z)){
                            setX.addCard(z);
//System.out.println("\t\t4. "+z.getContentString() + " is added, resultant set:" + setX.getContentString());
                        }

                        // reset ace mode z
                        //if(z.isAce())   z.resetAceMode();
                    }
                }

                // reset ace mode x,y
                /*if(x.isAce())   x.resetAceMode();
                if(y.isAce())   y.resetAceMode();*/
            }

            //5 try to find card y that can form a sequence with x, while abs(x-y)=1
            for(Iterator<Card> j=replica1.iterator() ; j.hasNext() ; ) {

                Card y = j.next();

                // set ace  mode x,y
                if(x.isAce())   x.setAceModeInRelationToCard(y);
                if(y.isAce())   y.setAceModeInRelationToCard(x);

                int possibility = CardSet.associateable(x, y);

/*String pT = "Not possible";
if(possibility==CardSet.SEQUENCE_SET && Math.abs(x.getDenomination() - y.getDenomination())==1)   pT = "Possible";
System.out.println("@RoundPlayer.cpuMatchHand() - 5 - Opportunity to form SEQUENCE SET (1st neighbor) between " +
"i(" + x.getContentString() + ") and j(" + y.getContentString() + ") " + pT);
*/
                if (possibility == -1){
//System.out.println("\t5. Skipping " + y.getContentString());
                    continue;
                }

                if(possibility==CardSet.SEQUENCE_SET && Math.abs(x.getDenomination() - y.getDenomination())==1){
//System.out.println("\t5. " + y.getContentString() + " is next in seq to " + x.getContentString() + " ... drilling in");
                    CardSet setX = CardSet.createSet(this , x,y);
                    xPossibleSets.add(setX);//6
//System.out.println("\t5. Created and added to " + x.getContentString() + " sets: " + setX.getContentString());

                    //7

                    UngroupedCardsTreeSet replica2 = new UngroupedCardsTreeSet(junk);
                    replica2.remove(x);
                    replica2.remove(y);
//System.out.println("\t5. now trying to increase members of " + setX.getContentString() + " from " + replica2.getContentString());
                    for(Iterator<Card> k = replica2.iterator() ; k.hasNext();){
                        Card z = k.next();

                        // set ace mode z
                        if(z.isAce())   z.setAceModeInRelationToCard(x);

//System.out.println("\t\t5. now looking at " + z.getContentString());
                        if(setX.addable(z)){
                            setX.addCard(z);
//System.out.println("\t\t5. "+z.getContentString() + " is added, resultant set:" + setX.getContentString());
                        }

                        // reset ace mode z
                        //if(z.isAce())   z.resetAceMode();
                    }
                }

                // reset ace mode x,y
                //if(x.isAce())   x.resetAceMode();
                //if(y.isAce())   y.resetAceMode();
            }

            //8 try to find card y that can form a sequence with x, while abs(x-y)=2
            for(Iterator<Card> j=replica1.iterator() ; j.hasNext() ; ) {
                Card y = j.next();

                int possibility = CardSet.associateable(x, y);

                // set ace mode x,y
                if(x.isAce())   x.setAceModeInRelationToCard(y);
                if(y.isAce())   y.setAceModeInRelationToCard(x);

/*String pT = "Not possible";
if(possibility==CardSet.SEQUENCE_SET && Math.abs(x.getDenomination() - y.getDenomination())==2)   pT = "Possible";
System.out.println("@RoundPlayer.cpuMatchHand() - 8 - Opportunity to form SEQUENCE SET (2nd neighbor) between " +
"i(" + x.getContentString() + ") and j(" + y.getContentString() + ") " + pT);
*/
                if (possibility == -1) {
//System.out.println("\t8. Skipping " + y.getContentString());
                    continue;
                }

                if(possibility==CardSet.SEQUENCE_SET && Math.abs(x.getDenomination() - y.getDenomination())==2){
//System.out.println("\t8. " + y.getContentString() + " is 2nd next in seq to " + x.getContentString() + " ... drilling in");
                    CardSet setX = CardSet.createSet(this , x,y);
                    xPossibleSets.add(setX);//9
//System.out.println("\t8. Created and added to " + x.getContentString() + " sets: " + setX.getContentString());

                    //10
                    UngroupedCardsTreeSet replica2 = new UngroupedCardsTreeSet(junk);
                    replica2.remove(x);
                    replica2.remove(y);
//System.out.println("\t8. now trying to increase members of " + setX.getContentString() + " from " + replica2.getContentString());
                    for(Iterator<Card> k = replica2.iterator() ; k.hasNext();){
                        Card z = k.next();

                        // set ace mode z
                        if(z.isAce())   z.setAceModeInRelationToCard(x);

//System.out.println("\t\t8. now looking at " + z.getContentString());
                        if(setX.addable(z)){
                            setX.addCard(z);
//System.out.println("\t\t8. "+z.getContentString() + " is added, resultant set:" + setX.getContentString());
                        }

                        // reset ace mode z
                        //if(z.isAce())   z.resetAceMode();
                    }
                }

                // rest ace mode x,y
                //if(x.isAce())   x.resetAceMode();
                //if(y.isAce())   y.resetAceMode();
            }



            if(! xPossibleSets.isEmpty()) {
                allPossibleSets.addAll(xPossibleSets);
            }
            //TODO 17
            else{
                unprocessableCards.add(x);
            }

            //add x to processed cards so future cards won;t be processed against x
            processedCards.add(x);

        }//end processing of each card

        //done with all cards, retain only  unprocessableCards members in unmatchedCards
        junk.retainAll(unprocessableCards);

        //remove null sets
        for(Iterator<CardSet> i = allPossibleSets.iterator() ; i.hasNext() ; ){
            if(i.next().allMembersNull())   i.remove();
        }




        System.out.println("--------------- IDENTIFY SETS - " + this + " ----------------------");

        System.out.println("\n\n");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$ ALL POSSIBLE SETS (" + allPossibleSets.size() + ") before duplicates removal $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        for(Iterator<CardSet> i = allPossibleSets.iterator() ; i.hasNext() ; ){
            System.out.println("\t" + i.next().getContentString());
        }
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("\n\n");







        List <CardSet> finalCandidateSets = new ArrayList<>();
        if(!allPossibleSets.isEmpty()){
            finalCandidateSets = removeDuplicates(allPossibleSets);
        }


        //FROM HEREON, allPossibleSets IS NOT USED


        System.out.println("\n\n");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$ finalCandidateSets(" + finalCandidateSets.size() + ")  $$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        for(Iterator<CardSet> i = finalCandidateSets.iterator() ; i.hasNext() ; ){
            System.out.println("\t" + i.next().getContentString());
        }
        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        System.out.println("--------------- END IDENTIFY SETS - " + this + " ----------------------");
        System.out.println("\n\n");

        return finalCandidateSets;
    }






    /*
    Returns the "widest" CardSet from each group of duplicate sets found within provided list.
     */
    private List<CardSet> removeDuplicates(List<CardSet> allSets){
        //detect (!) and remove "duplicate" sets: overlapping
        //map overlapping sets and chose the widest
        //if 2 common members: overlap
        //put each group of overlapping sets in a category
        //select widest category member
        ArrayList<ArrayList<CardSet>> duplicateSetGroups = new ArrayList<>();

        List<CardSet> result = new ArrayList<>();


        //1. place sets in groups
//System.out.println("%%%%%%%%%%%%%%%%%%%%%% DETECTING DUPLICATES from " + allSets.size() +" possible sets %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        for(int i=0 ; i<allSets.size()-1 ;i++){
            CardSet setX = allSets.get(i);
            boolean xOverlap = false;//will be flagged if a duplicate was found for x, and processed in block 1.1

//System.out.println("\tnow processing " + setX.getContentString() + " @ allSets index " + i);


            for(int j=i+1 ; j<allSets.size() ; j++){
                CardSet setY = allSets.get(j);
//System.out.println("\t\tagainst " + setY.getContentString() + " @ allSets index " + j);

                //1.1
                if(setX.overlaps(setY)){
                    if(!xOverlap)   xOverlap = true;
                    boolean addedToDuplicateGroup = false;

//System.out.println("\t\t\t\tIS overlap");
                    //if x,y fit within a 'similar' group of sets, locate it and add x AND/OR y
                    //if no such group of sets exist, create it
                    for(Iterator it=duplicateSetGroups.iterator() ; it.hasNext() ; ) {
                        List setGroup = (List)it.next();
//String x="nothing";if(!setGroup.isEmpty())  x=((CardSet)setGroup.get(0)).getContentString();
//System.out.println("\t\t\t\t\t\ttrying to fit in a group of duplicates containing " + x);

                        //similar group exists? add
                        if (setsFitDuplicates(setGroup, setX, setY)){
//System.out.println("\t\t\t\t\t\t\t\ta fit is found.... adding to group containing " + x);
                            if(!setGroup.contains(setX)){
//System.out.println("\t\t\t\t\t\t\t\t" + setX.getContentString() + " added to duplicates group");
                                setGroup.add(setX);
                            }
                            if(!setGroup.contains(setY)){
//System.out.println("\t\t\t\t\t\t\t\t" + setY.getContentString() + " added to duplicates group");
                                setGroup.add(setY);
                            }
                            addedToDuplicateGroup = true;
                            break;
                        }
                    }

                    //create set group if needed
                    if(!addedToDuplicateGroup){
//System.out.println("\t\t\t\t\t\t\t\ta fit was NOT found.... creating a group to contain " + setX.getContentString() + " & " + setY.getContentString());
                        ArrayList<CardSet> list = new ArrayList();
                        list.add(setX); list.add(setY);
                        duplicateSetGroups.add(list);
                    }

                }
                else{
//System.out.println("\t\t\tno overlap");
                }
            }//end inner walk through

            //if no duplicate was found for x, add to result
            if(!xOverlap)   result.add(setX);

//System.out.println("\tresultant allPossible sets has " + allPossibleSets.size() + " after processing " + setX.getContentString()  );
        }//end walk through

        //in the rare case that the last set in allSets does not conflict with any other set...
        // it will not be traversed in the code above since it will never be setX...
            // the line <<if(!xOverlap)   result.add(setX)>> will not be executed for it
        //so, if the last set cannot be found in result or duplicateSets... add it to result
        boolean lastSetProcessed = false;
        CardSet lastSet = allSets.get(allSets.size()-1);

        for(Iterator<ArrayList<CardSet>> i =duplicateSetGroups.iterator() ; i.hasNext() ; ){
            if(i.next().contains(lastSet)){
                lastSetProcessed = true;    break;
            }
        }

        if(!lastSetProcessed){
            lastSetProcessed = result.contains(lastSet);
        }

        if(!lastSetProcessed)   result.add(lastSet);


        //
        //2. select widest set from each group of duplicates
        //TODO...


        for(Iterator i = duplicateSetGroups.iterator() ; i.hasNext() ; ){
            List duplicateSetsList = (List)i.next();
            CardSet widestCardSet = null;

            for(Iterator<CardSet> j = duplicateSetsList.iterator() ; j.hasNext() ;){
                CardSet setX = j.next();
                if(widestCardSet==null)    widestCardSet=setX;

                else{
                    if(setX.getNonNullMembers().size() > widestCardSet.getNonNullMembers().size())  widestCardSet = setX;
                }
            }

            if(!result.contains(widestCardSet)){
                result.add(widestCardSet);
            }

            // *************************** START EXCEPTIONS *****************************
            //todo revise set creation for cause ... why are there duplicates?
/*
            //EXC 1... this is a workaround for issue#1... revise
            //if any set does not intersect widestCardSet at all... add it to result set
            for(Iterator<CardSet> j = duplicateSetsList.iterator() ; j.hasNext() ;){
                CardSet setX = j.next();
                if(widestCardSet==setX) continue;
                if(Collections.disjoint(setX.getNonNullMembers() , widestCardSet.getNonNullMembers())){
                    result.add(setX);
System.out.println("@RP.removeDuplicates() - re-instated " + setX.getContentString() + " after selecting widest set " + widestCardSet.getContentString());
                }
            }//end EXC 1
*/

/*
            //EXC 2
            //widest set selected leaves out 2 consequetive cards in the beginning or end: create set for leftouts
            //i.e. for any set in duplicate set group that is NOT selected as widest
                //any cards left out in beginning or end? more than 1?
                    //yes: create set for those
            for(Iterator<CardSet> j = duplicateSetsList.iterator() ; j.hasNext() ;){
                CardSet setX = j.next();
                if(widestCardSet==setX) continue;


                //case 1: setX series starts first... x members down widest set starts
                Card intersection = widestCardSet.getNonNullMembers().get(0);

                //index of intersection in setX
                int iSec = setX.indexOf(intersection);

                //if index>1, more than a card lay before intersection::
                if(iSec>1){
                    //isolate non null cards
                    List<Card> leftouts = new ArrayList<>();
                    for(Iterator<Card> k = setX.subList(0,iSec).iterator() ; k.hasNext() ;){
                        Card c = k.next();
                        if(c!=null) leftouts.add(c);
                    }

                    //create set
                    if(!leftouts.isEmpty()) {
                        CardSet loSet = CardSet.createSet(this, leftouts.get(0), leftouts.get(1));
                        if (leftouts.size() > 2) {
                            for (int loC = 2; loC < leftouts.size(); loC++) {
                                loSet.addCard(leftouts.get(loC));
                            }
                        }

                        //add to result
                        result.add(loSet);
                    }
                }

//System.out.println("@RP.removeDuplicates() - re-instated " + setX.getContentString() + " after selecting widest set " + widestCardSet.getContentString());

            }//end EXC 2
*/



            //EXC 3
            /*
            Duplicate set group
                CardSet (NULL@0, NULL@1, 5 of hearts @2, NULL@3, 7 of hearts @4, 8 of hearts @5, NULL@6), 		CardSet (NULL@0, 5 of hearts @1, NULL@2, 7 of hearts @3, 8 of hearts @4, NULL@5, NULL@6, J of hearts @7), 		CardSet (NULL@0, NULL@1, J of hearts @2, NULL@3, K of hearts @4, NULL@5)
            Widest set
                CardSet (NULL@0, 5 of hearts @1, NULL@2, 7 of hearts @3, 8 of hearts @4, NULL@5, NULL@6, J of hearts @7)
            Leftout
                CardSet (NULL@0, NULL@1, J of hearts @2, NULL@3, K of hearts @4, NULL@5)
            | |
             V
            K of hearts is nowhere to be found for terminateSelectionOfPotentialSets().

            Ideally, 5,null,7,8 is one set.... (null,)J,null,K(,null) is another


             */


            // *************************** END EXCEPTIONS *****************************

        }//end traversing set groups

        //at this point all duplicate sets are grouped
        //none-duplicated sets are still in allPossible


        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$ DUPLICATE SET GROUPS (" + duplicateSetGroups.size() + ")$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
        for(Iterator i = duplicateSetGroups.iterator() ; i.hasNext() ; ){
            List duplicateSets = (List)i.next();
            List<Card> flatList = new ArrayList<>();

            for(Iterator<CardSet> j = duplicateSets.iterator() ; j.hasNext() ; ){
                CardSet set = j.next();
                System.out.print("\t\t" + set.getContentString());
                //flatten sets to find min/max cards
                flatList.addAll(set.getNonNullMembers());

                if(j.hasNext()) System.out.print(", ");
            }
            System.out.println();

            Card max = Collections.max(flatList);
            Card min = Collections.min(flatList);

            int delta = max.denomination - min.denomination;
            System.out.println("max: " + max.getContentString() + " - min: " + min.getContentString() + " - delta: "+ delta);


        }



        System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");

        return result;
    }



    /*
    Both sets have to overlap with at least one set from duplicates?
     */
    private boolean setsFitDuplicates(List<CardSet> duplicateSets , CardSet setX , CardSet setY){
        boolean hit1 = duplicateSets.contains(setX);
        boolean hit2 = duplicateSets.contains(setY);
        for(Iterator<CardSet> i = duplicateSets.iterator() ; i.hasNext() ; ){
            CardSet dupSet = i.next();
            if(!hit1 && dupSet.overlaps(setX))   hit1=true;
            if(!hit2 && dupSet.overlaps(setY))   hit2=true;
        }
        return hit1&&hit2;
    }


    //for each card, check if contained in a set from potentialSets
    private void moveSetMembersToUngroupedCards(CardSet set){
//System.out.println("moveSetMembersToUngroupedCards()--" + set.getContentString());

        for(Iterator<Card> i = set.iterator() ; i.hasNext()  ; ){
            Card c = i.next();
            if(c==null) continue;

            boolean retainCard = false;
CardSet toDelete = null;

            for(Iterator<CardSet> j = potentialSets.iterator() ; j.hasNext() ; ){
                CardSet pSet = j.next();

                if(pSet.contains(c)){
                    retainCard = true;
toDelete = pSet;
                    break;
                }
            }

            if(!retainCard){
                unmatchedCards.add(c);
                i.remove();
System.out.println("@RP.moveSetMembersToUngroupedCards() - " + c.getContentString() + " moved to unmatchedCards");
            }
            else{

System.out.println("@RP.moveSetMembersToUngroupedCards() - " + c.getContentString() + " retained for use in " + toDelete.getContentString());
            }
        }
    }



    private CardSet getSexiestSet(List<CardSet> sets){
System.out.println("\t\t@RoundPlayer.getSexiestSet()... evaluating " + sets.size() + " sets");
        double maxScore = 0;
        CardSet theOne = null;
        for(Iterator<CardSet> i = sets.iterator() ; i.hasNext() ; ){
            CardSet x = i.next();
System.out.println("\t\t\tNow evaluating set " + x.getContentString());
            double xScore = x.getSexiness();
if(score>0) System.out.println("\t\t\t\tscore " + xScore + " against current maxScore (" + maxScore + ")");
            if(xScore>maxScore){
System.out.println("\t\t\t\tHit... new maxScore=" + xScore + " ... this set is currently the one");
                maxScore = xScore;
                theOne = x;
            }
        }
System.out.println("\t\t@RoundPlayer.getSexiestSet():: returning the ONE " + theOne.getContentString());
        return theOne;
    }




    private boolean conflictWithPotentialSets(CardSet x){

        for(Iterator<Card> i = x.iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c==null) continue;

            for(Iterator<CardSet> j = potentialSets.iterator() ; j.hasNext() ; ){
                CardSet otherSet = j.next();

                if(x.equals(otherSet)) continue;//skip self

                if(otherSet.contains(c)){
System.out.println("\t\t@conflictWithPotentialSets() - " + x.getContentString() + " conflicts with " + otherSet.getContentString());
                    return true;//conflict exists
                }
/*else{
    System.out.println("@conflictWithPotentialSets() - " + x.getContentString() + " DOES NOT conflict with " + otherSet.getContentString());
}*/
            }
        }

        return false;
    }


    /*
    Returns true if all cards in deck are in any of:
        - potentialSets:
        - unmatchedCards:
        - doubles:
        - floorSets:
        - addedToOtherPlayersFloorSets:
     */

    private boolean terminateSelectionOfPotentialSets(){
System.out.println("@terminateSelectionOfPotentialSets() ---------------------- ");

        for(Iterator<Card> it = hand.iterator() ; it.hasNext() ; ) {
            Card card = (Card)it.next();
            boolean cardFound = false;

System.out.println("\t\tverifying card " + card.getContentString());

            //locate card in sets first
            for (Iterator<CardSet> i = potentialSets.iterator(); i.hasNext(); ) {
                CardSet set = (CardSet)i.next();
                if(set.contains(card)){
                    cardFound = true;
System.out.println("\t\t\t\tcard " + card.getContentString() + " found in potential set " + set.getContentString());
                    break;
                }
            }

            //floor sets
            if(!cardFound) {
                for (Iterator<CardSet> i = floorSets.iterator(); i.hasNext(); ) {
                    CardSet set = (CardSet) i.next();
                    if (set.contains(card)) {
                        cardFound = true;
                        System.out.println("\t\t\t\tcard " + card.getContentString() + " found in floor set " + set.getContentString());
                        break;
                    }
                }
            }

            //not found? try unmatchedCards...
            if(!cardFound && unmatchedCards.contains(card)){
                cardFound = true;
System.out.println("\t\t\t\tcard " + card.getContentString() + " found in unmatchedCards");
                break;
            }

            //not found? try doubles...
            if(!cardFound && doubles.contains(card)){
                cardFound = true;
System.out.println("\t\t\t\tcard " + card.getContentString() + " found in doubles");
                break;
            }

            //todo "covering" others' sets
            if(!cardFound && addedToOtherPlayersFloorSets.contains(card)){
                cardFound = true;
System.out.println("\t\t\t\tcard " + card.getContentString() + " found in addedToOtherPlayersFloorSets");
                break;
            }

            //if still not found return false; card has not been processed
            if(!cardFound){
System.out.println("\t\t\t\tcard " + card.getContentString() + " not found - returning -ve");
System.out.println("+++++++++++++ terminateSelectionOfPotentialSets condition NOT met ++++++++++++++++");
                return false;
            }
        }

        //all cards processed, condition met
System.out.println("+++++++++++++ terminateSelectionOfPotentialSets condition met ++++++++++++++++");
        return true;
    }


    /*
        1 - if not onFloor, assess whether available "complete" sets can be moved to floor
            - if yes
                - move to floor
                - onFloor = true
        2 - if onFloor
            - assess whether "new" available "complete" sets can be moved to floor
                - if yes,  move to floor
            - assess whether any card can be moved to floor
                - if yes, move to floor
     */
    private void cpuAssessFloorAction(){
        /*
        1.
        prepare each set:
            chose highest complete series and "trim" the set accordingly
            compute value
        can move to floor?
            Game.getMoveToFloorNumberCardsMethod()
                and
            Game.getMoveToFloorValueCardsMethod()
            moveToFloor()
        */
        int floorCardsValue = 0;
        int numFloorCards = 0;

        if(!onFloor){

System.out.println();

            //calculate thresholds
            for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
                CardSet set = i.next();
                if(set.prepareForFloor()){
System.out.println("\t\t" + set.getContentString() + " is ready");
                    floorCardsValue = floorCardsValue + set.getCumulativeValue();
                    numFloorCards = numFloorCards + set.size();
                }
            }

System.out.println("\t@RoundPlayer.cpuAssessFloorAction() - " + this + " ready floor value="+floorCardsValue);
System.out.println("\t@RoundPlayer.cpuAssessFloorAction() - " + this + " ready floor num cards="+numFloorCards);

            //the fixed situation
            if(round.getGame().getMoveToFloorValueCardsMethod() == Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                    &&
                round.getGame().moveToFloorNumberCardsMethod == Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                    &&
                (
                    floorCardsValue == Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                            ||
                    floorCardsValue > Game.MOVE_TO_FLOOR_VALUE_CARDS_METHOD_FIXED
                )
                    &&
                (
                    numFloorCards == Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                    ||
                    numFloorCards > Game.MOVE_TO_FLOOR_NUMBER_CARDS_METHOD_FIXED
                )
            ){

System.out.println("\t@RoundPlayer.cpuAssessFloorAction() - " +toString() + " is good to go");

                List<CardSet> leftoverPotentialSets = new ArrayList<>();//buffer to hold...

                for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ;){
                    CardSet set = i.next();
                    if(set.isComplete()) {
                        round.moveToFloor(set, this);
                        if(!floorSets.contains(set))    floorSets.add(set);//add to floor sets if not added the first round
                        i.remove();//remove from potential sets

                        //remove set cards from any other potential sets they might be members of
                        for(Iterator<CardSet> j = potentialSets.iterator() ; j.hasNext() ;){
                            CardSet s = j.next();

                            if(s.equals(set))   continue;//skip self
                            s.removeAll(set);
                        }

                        //also, retrieve any leftover cards and either create set(s) or put in unmatchedCards
                        UngroupedCardsTreeSet junk = new UngroupedCardsTreeSet(set.cardsToDiscardPostCompletion);
                        List<CardSet> leftoverSets = identifySets(set.cardsToDiscardPostCompletion , junk);
System.out.println("\t@RoundPlayer.cpuAssessFloorAction() - calling identifySets() for " + set.getContentString() + " leftovers");
                        leftoverPotentialSets.addAll(leftoverSets);
                        unmatchedCards.addAll(junk);
                    }
                }//end walk through

                //process leftoverSets: move to potentialSets and prepareForFloor
                for(Iterator<CardSet> i = leftoverPotentialSets.iterator() ; i.hasNext() ; ){
                    CardSet loSet = i.next();
                    potentialSets.add(loSet);

                    if (loSet.prepareForFloor()) {
                        System.out.println("\t\tleftover set " + loSet.getContentString() + " is ready");
                        floorCardsValue = floorCardsValue + loSet.getCumulativeValue();
                        numFloorCards = numFloorCards + loSet.size();
                    }
                }

                onFloor = true;
            }

            //todo the floating situations


        }


System.out.println("==========================================");
    }


    /*
    Covers cards from:
    - unmatchedCards
    - doubles
     */
    private void cpuCoverOtherPlayerSets(){
        List<RoundPlayer> playersToUpdate = new ArrayList<>();

        for(Iterator<RoundPlayer> i = round.getPlayers().iterator() ; i.hasNext() ; ){
            RoundPlayer rp = i.next();
            //if(rp.equals(this)) continue;//skip self

            for(Iterator<CardSet> j = rp.floorSets.iterator() ; j.hasNext() ; ){
                CardSet set = j.next();

                //unmatchedCards
                for(Iterator<Card> k = unmatchedCards.iterator() ; k.hasNext() ; ){
                    Card c = k.next();
                    if(set.addable(c)){
                        //add card to set.. round.moveToFloor
                        set.addCard(c);
System.out.println("@RP.coverOtherPlayerSets():: added " + c.getContentString() + " to " + set.getContentString());
                        //add card to coveredCards
                        addedToOtherPlayersFloorSets.add(c);
                        k.remove();
                        if(!playersToUpdate.contains(set.player))    playersToUpdate.add(set.player);
                    }
                }

                // doubles
                for(Iterator<Card> k = doubles.iterator() ; k.hasNext() ; ){
                    Card c = k.next();
                    if(set.addable(c)){
                        //add card to set.. round.moveToFloor
                        set.addCard(c);
                        //add card to coveredCards
                        addedToOtherPlayersFloorSets.add(c);
                        set.player.addedFromOtherPlayersToFloorSets.add(c);
System.out.println("@RP.coverOtherPlayerSets():: added " + c.getContentString() + " to " + set.getContentString());
                        k.remove();
                        if(!playersToUpdate.contains(set.player))    playersToUpdate.add(set.player);
                    }
                }
            }
        }

        for(Iterator<RoundPlayer> i = playersToUpdate.iterator() ; i.hasNext() ; ) {
            round.updateFloor(i.next());
        }
    }




    Card rejectPrediction;
    public Card getRejectPrediction(){
        return rejectPrediction;
    }

    /*
    .... the smallest card in unmatchedCards
     */
    private void cpuPredictReject(){


        //first discard of any doubles!!!
        if(!doubles.isEmpty()){
            rejectPrediction = doubles.get(0);
        }

        //in the presence of unmatched cards, pick smallest
        else if(!unmatchedCards.isEmpty()) {
            Card smallest = null;

            for (Iterator<Card> i = unmatchedCards.iterator(); i.hasNext(); ) {
                Card c = i.next();
//System.out.println("comparing " + c.getDenominationString() + " to smallest:" + smallest);
                if (smallest == null) smallest = c;
                else {
                    if (c.getDenomination() < smallest.getDenomination()) smallest = c;
                }
            }

            rejectPrediction = smallest;
            System.out.println("REJECT predicted  " + rejectPrediction.getContentString());
        }


        //no unmatched cards? smalles card of least sexiest potential set
        else{
            CardSet leastSet = null;
            for(Iterator<CardSet> i = potentialSets.iterator() ; i.hasNext() ; ){
                CardSet set = i.next();

                if(leastSet==null)  leastSet = set;
                else if(set.getSexiness() < leastSet.getSexiness())  leastSet = set;
            }
            //least card of least set
            rejectPrediction = leastSet.getNonNullMembers().get(0);
        }
    }




    private void cpuThrowReject(){
        //remove from deck
        hand.remove(rejectPrediction);

        //add to round reject
        round.getReject().add(rejectPrediction);

        //notify
        round.rejectComplete(rejectPrediction);

        //TODO reset rejectPrediction???
        //rejectPrediction = null;
    }
    //********************************** END CPU PLAYER **********************************************
}
