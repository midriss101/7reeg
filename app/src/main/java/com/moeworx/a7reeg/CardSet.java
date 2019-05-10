package com.moeworx.a7reeg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;

public class CardSet extends ArrayList<Card> {

    static Random ID_GEN = new Random();

    RoundPlayer player;


    int id;
    public int getId(){
        return id;
    }



    public CardSet(RoundPlayer player){
        super();
        this.player = player;
        id = ID_GEN.nextInt();
    }



    int cumulativeValue = 0;
    public int getCumulativeValue(){
        return cumulativeValue;
    }



    public static int TYPE_SET = 1;
    public static int SEQUENCE_SET = 2;

    int setKind = -1;

    public boolean isTypeSet(){
        if(setKind==TYPE_SET)   return true;
        return false;
    }

    public boolean isSequenceSet(){
        if(setKind==SEQUENCE_SET)   return true;
        return false;
    }

    public String getTypeString(){
        if(isTypeSet())     return "TYPE_SET";
        else                return "SEQUENCE_SET";
    }


    //for typeSet only,
    int typeSetDenomination = -1;
    int typeSetValue = -1;


    //for sequenceSet only
    int sequenceSetType = -1;
    int lowerBound = -1;
    int upperBound = -1;

    /* whether set is ready for floor, or missing cards*/
    boolean complete = false;
    public boolean isComplete(){
        return complete;
    }

    /*
    TODO consider flag isOpportunity() to indicate whether set (or player?) is in "planning phase" or "production phase"; i.e.
        - planning: no sets on the floor yet
        - production: some sets already on floor
    */


    private Card getFirstNonNullCard(){
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c!=null) return c;
        }
        return null;
    }


    private Card getSmallestCardOfSequenceSet(){
        if(!isSequenceSet())    return null;

        Card theOne=null;
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c==null) continue;
            if(theOne==null)    theOne=c;
            else if(c.getDenomination() < theOne.getDenomination()) theOne=c;
        }

        return theOne;
    }


    private Card getBiggestCardOfSequenceSet(){
        if(!isSequenceSet())    return null;

        Card theOne=null;
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c==null) continue;
            if(theOne==null)    theOne=c;
            else if(c.getDenomination() > theOne.getDenomination()) theOne=c;
        }

        return theOne;
    }



    /*
    Can the set accomodate supplied card?
     */
    public boolean addable(Card c) {
if(isSequenceSet()) {
System.out.println("@CardSet.addable(): assessing if " + c.getContentString() + " can be added to a " + getTypeString() + " of " + size() +
        " possible positions starting with " + lowerBound + " of " + Card.getTypeString(sequenceSetType) + " and ending with " +
        upperBound + " of " + Card.getTypeString(sequenceSetType)
);
}
else if(isTypeSet()){
System.out.println("@CardSet.addable(): assessing if " + c.getContentString() + " can be added to a " + getTypeString() + " of " + size() +
        " possible positions denominated at " + Card.getDenominationString(typeSetDenomination));
}

        //skip if same card has been added (from another deck)
        for(Iterator<Card> i = iterator(); i .hasNext() ; ){
            Card card = i.next();
            if(card==null)  continue;
            if(card.equals(c)) {
System.out.println(c.getContentString() + " is identical to a card on set: " + card.getContentString());
                return false;
            }
            if(c.sameCardDifferentDeck(card)){
System.out.println(c.getContentString() + " is sameFromDifferentDeck as a card on set: " + card.getContentString());
                return false;
            }
        }



        boolean b = false;

        //typeset: same denomination, different (non-added) type
        if(isTypeSet()){
System.out.println(c.getContentString() + "  addable type set of " + typeSetDenomination + "?");
            if(c.getDenomination()==typeSetDenomination){
                b = true;
System.out.println("\tYes");
            }
        }

        //seq
        else if(isSequenceSet()){
System.out.println(c.getContentString() + " is addable to the sequence set of type" + sequenceSetType + "?");
            if(c.getType()==sequenceSetType){

                //new set
                if(!isComplete()) {
                    if (c.getDenomination() == lowerBound || c.getDenomination() == upperBound ||
                            (c.getDenomination() > lowerBound && c.getDenomination() < upperBound)
                    ) {
                        b = true;
System.out.println("\tYes - incomplete set");
                    }
                }

                //complete set; i.e. floor set, an addable card would be 1+ greatest card or 1- least card
                //exception: A in 2,3,4.. sets: value of A Higher will not conform to the condition above
                else{
                    List<Card> nonNulls = getNonNullMembers();
                    if( (c.getValue() == nonNulls.get(0).getValue()-1) ||
                        (c.getValue() == nonNulls.get(nonNulls.size()-1).getValue()+1) ){
                        b = true;
System.out.println("\tYes - complete set");
                    }
                    else if(c.isAce() && nonNulls.get(0).getValue()==Card.DENOMINATION_VALUE_2){
                        b = true;
                    }
                }
            }
        }

System.out.println("@CardSet.addable(): " + c.getContentString() + " to " + getContentString() + ": " + b);
        return b;
    }




    /*
    Can a set be formed? If yes, is it typeSet or sequenceSet?
     */
    public static int associateable(Card c1 , Card c2){
        int setKind = -1;

        //escape identical cards here since Card.comparable() will not. Card.comparable() is only overriden to honor SEQUENCE_TYPE sets'
        //comparison and cannot correctly process comparing identical cards (from different decks).
        if(c1.sameCardDifferentDeck(c2))    return -1;


        //type set
        if(c1.getDenomination()==c2.getDenomination() && c1.getType()!=c2.getType()){
            setKind = TYPE_SET;
        }

        //sequence set, up to 5 cards
        else {
            if (c1.getType() == c2.getType()) {
                int diff = Math.abs(c1.getDenomination()-c2.getDenomination());
                if(diff == 4 || diff < 4){
                    setKind = SEQUENCE_SET;
                }
                //if any is ace and other is 10+, consider ace as  'pic' or if the intended set is A,2,3
                else if ( c1.isAce() &&  (c2.getDenomination()==10 || c2.getDenomination()>10 || c2.getDenomination()==2 || c2.getDenomination()==3 ) ) {
                    int d1 = 10;    int d2 = c2.getDenomination();
                    diff = Math.abs(d1-d2);
                    if(diff == 4 || diff < 4){
                        setKind = SEQUENCE_SET;
                    }
                }
                else if ( c2.isAce() && (c1.getDenomination()==10 || c1.getDenomination()>10 || c1.getDenomination()==2 || c2.getDenomination()==3) ) {
                    int d1 = 10;    int d2 = c1.getDenomination();
                    diff = Math.abs(d1-d2);
                    if(diff == 4 || diff < 4){
                        setKind = SEQUENCE_SET;
                    }
                }
            }
        }


        return setKind;
    }



    public static CardSet createSet(RoundPlayer player , Card c1 , Card c2){

//if(!player.isCpu())  System.out.println("@CardSet.createSet(" + c1.getDenominationString() +" of " + c1.getTypeString() + "," + c2.getDenominationString() +" of " + c2.getTypeString() + ")");


        if(associateable(c1,c2) == -1)   return null; //ace mode set & reset (possibly)


//if(!player.isCpu())  System.out.println("associateable");
        CardSet set = new CardSet(player);
        
        //typeSet
        if(c1.getDenomination() == c2.getDenomination() && c1.getType() != c2.getType()){
//System.out.println("@CardSet.createSet(" + c1.getDenominationString() +" of " + c1.getTypeString() + "," + c2.getDenominationString() +" of " + c2.getTypeString() + ")");
//System.out.println("typeSet");
//System.out.println();
            set.setKind = TYPE_SET;
            set.typeSetDenomination = c1.getDenomination();
            set.typeSetValue = c1.getValue();

            //insert cards ordered by type
            set.ensureSize(4);
            set.set(c1.getType()-1,c1);
            set.set(c2.getType()-1,c2);
        }

        //sequenceSet
        else if(c1.getType() == c2.getType()){
            set.setKind = SEQUENCE_SET;
            set.sequenceSetType = c1.getType();

if(!player.isCpu()) System.out.println("\t\t@CardSet.createSet() - sequenceSet detected for " + c1.getContentString() + " and " + c2.getContentString());


            //determine diff between both
            int diff = Math.abs(c1.getDenomination() - c2.getDenomination());

if(!player.isCpu()) System.out.println("\t\t\tdiff="+diff);


                //if diff==4, each card is at continuum extreme (e.g. Ace & 5, K&9)
            if(diff==4) {
                set.ensureSize(5);

if(!player.isCpu()) System.out.println("\t\t\tcase 4");

                if (c2.getDenomination() < c1.getDenomination()) {
                    set.lowerBound = c2.getDenomination();
                    set.upperBound = c1.getDenomination();
                    set.set(0,c2);
                    set.set(4,c1);
                } else if (c1.getDenomination() < c2.getDenomination()) {
                    set.lowerBound = c1.getDenomination();
                    set.upperBound = c2.getDenomination();
                    set.set(0,c1);
                    set.set(4,c2);
                }
            }


            //diff 3, potential 6  (e.g. 2&5, K&10) (1)
            // except if
            //one is ace high mode and the other card is J (potential 5: A..10) (2)
            //one is ace low mode and the other card is  4 (potential 5: A..5)  (3)
            else if(diff==3) {

if(!player.isCpu()) System.out.println("\t\t\tcase 3");

                //(3)
                if ((c1.getAceMode() == Card.ACE_MODE_LOW && c2.getDenomination() == 4)
                        ||
                        (c2.getAceMode() == Card.ACE_MODE_LOW && c1.getDenomination() == 4)
                        ) {
                    set.lowerBound = Card.DENOMINATION_VALUE_ACE_LOWER;
                    set.upperBound = Card.DENOMINATION_VALUE_5;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(0,c1);
                        set.set(3,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(0,c2);
                        set.set(3,c1);
                    }

if(!player.isCpu()) System.out.println("\t\t\t\t\tsw3 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }
                //(2)
                else if ((c1.getAceMode() == Card.ACE_MODE_HIGH && c2.getDenomination() == Card.DENOMINATION_VALUE_JACK)
                        ||
                        (c2.getAceMode() == Card.ACE_MODE_HIGH && c1.getDenomination() == Card.DENOMINATION_VALUE_JACK)
                        ) {
                    set.lowerBound = Card.DENOMINATION_VALUE_10;
                    set.upperBound = Card.DENOMINATION_VALUE_ACE_HIGHER;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(1,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(1,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw2");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw2 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(1) diff 3, potential 6  (e.g. 5&8) 4..9 5->1 8-> 4
                else {
                    set.ensureSize(6);

                    if (c2.getDenomination() < c1.getDenomination()) {
                        set.lowerBound = c2.getDenomination() - 1;
                        set.upperBound = c1.getDenomination() + 1;
                        set.set(1,c2);
                        set.set(4,c1);
                    } else if (c1.getDenomination() < c2.getDenomination()) {
                        set.lowerBound = c1.getDenomination() - 1;
                        set.upperBound = c2.getDenomination() + 1;
                        set.set(1,c1);
                        set.set(4,c2);
                    }
//System.out.println("\t\t\t\t\tsw1");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw1 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }
            }

            //diff 2, potential 7  (e.g. 5&7) (1)
            //except if
            //one is ace low mode and other is 3 (potential 5), (2)
            //one is ace high mode and the other is Q (potential 5) (3)
            //one is 2 and other is 4 (potential 6) ,  (4)
            //one is K and other is J (potential 6)     (5)
            else if(diff==2){

if(!player.isCpu()) System.out.println("\t\t\tcase 2");
                //(2)
                if ((c1.getAceMode() == Card.ACE_MODE_LOW && c2.getDenomination() == 3)
                        ||
                        (c2.getAceMode() == Card.ACE_MODE_LOW && c1.getDenomination() == 3)
                ) {
                    set.lowerBound = Card.ACE_MODE_LOW;
                    set.upperBound = Card.DENOMINATION_VALUE_5;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(0,c1);
                        set.set(2,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(0,c2);
                        set.set(2,c1);
                    }
//System.out.println("\t\t\t\t\tsw2");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw2 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(3)
                else if ((c1.getAceMode() == Card.ACE_MODE_HIGH && c2.getDenomination() == Card.DENOMINATION_VALUE_QUEEN)
                        ||
                        (c2.getAceMode() == Card.ACE_MODE_HIGH && c1.getDenomination() == Card.DENOMINATION_VALUE_QUEEN)
                ) {
                    set.lowerBound = Card.DENOMINATION_VALUE_10;
                    set.upperBound = Card.ACE_MODE_HIGH;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(2,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(2,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw3");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw3 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(4) P6 1..6 2/4 2->1 4->3
                else if ((c1.getDenomination() == 2 && c2.getDenomination() == 4)
                        ||
                        (c2.getDenomination() == 2 && c1.getDenomination() == 4)
                        ) {
                    set.lowerBound = Card.DENOMINATION_VALUE_ACE_LOWER;
                    set.upperBound = Card.DENOMINATION_VALUE_6;

                    set.ensureSize(6);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(1,c1);
                        set.set(3,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(1,c2);
                        set.set(3,c1);
                    }
//System.out.println("\t\t\t\t\tsw4");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw4 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(5)KJ P6 9..A  J->2  K->4
                else if ((c1.getDenomination() == Card.DENOMINATION_VALUE_KING && c2.getDenomination() == Card.DENOMINATION_VALUE_JACK)
                        ||
                        (c2.getDenomination() == Card.DENOMINATION_VALUE_KING && c1.getDenomination() == Card.DENOMINATION_VALUE_JACK)

                        ) {
                    set.lowerBound = Card.DENOMINATION_VALUE_9;
                    set.upperBound = Card.DENOMINATION_VALUE_ACE_HIGHER;

                    set.ensureSize(6);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(2,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(2,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw5");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw5 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(1) diff 2, potential 7  (e.g. 5&7) 3..9 5->2 7->4
                else {
                    set.ensureSize(7);

                    if (c2.getDenomination() < c1.getDenomination()) {
                        set.lowerBound = c2.getDenomination() - 2;
                        set.upperBound = c1.getDenomination() + 2;
                        set.set(2,c2);
                        set.set(4,c1);
                    } else if (c1.getDenomination() < c2.getDenomination()) {
                        set.lowerBound = c1.getDenomination() - 2;
                        set.upperBound = c2.getDenomination() + 2;
                        set.set(2,c1);
                        set.set(4,c2);
                    }
//System.out.println("\t\t\t\t\tsw1");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw1 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

            }

            //diff 1 , potential 8 (e.g. 4&5 , 8&9)  (1)
            //except if
            //ace high mode & K (potential 5),    (2)
            //ace low mode & 2 (potential 5),     (3)
            //2 & 3 (potential 6 A..6),             (4)
            //3 & 4 (potential 7 A..7),             (5)
            //Q & J  (potential 7 A..8),            (6)
            //K & Q (potential 6 A..9)              (7)
            else if (diff==1){
if(!player.isCpu()) System.out.println("\t\t\tcase 1");
                //(2)
                if( (c1.getDenomination()==Card.DENOMINATION_VALUE_ACE_HIGHER && c2.getDenomination()==Card.DENOMINATION_VALUE_KING)
                    ||
                    (c2.getDenomination()==Card.DENOMINATION_VALUE_ACE_HIGHER && c1.getDenomination()==Card.DENOMINATION_VALUE_KING)
                ){
                    set.lowerBound = Card.DENOMINATION_VALUE_10;
                    set.upperBound = Card.DENOMINATION_VALUE_ACE_HIGHER;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(3,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(3,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw2");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw2 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(3) ace low mode & 2 (potential 5),
                else if( (c1.getDenomination()==Card.DENOMINATION_VALUE_ACE_LOWER && c2.getDenomination()==Card.DENOMINATION_VALUE_2)
                        ||
                        (c2.getDenomination()==Card.DENOMINATION_VALUE_ACE_LOWER && c1.getDenomination()==Card.DENOMINATION_VALUE_2)
                        ){
                    set.lowerBound = Card.DENOMINATION_VALUE_ACE_LOWER;
                    set.upperBound = Card.DENOMINATION_VALUE_5;

                    set.ensureSize(5);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(0,c1);
                        set.set(1,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(0,c2);
                        set.set(1,c1);
                    }
//System.out.println("\t\t\t\t\tsw3");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw3 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(4) 2 & 3 (potential 6 A..6),
                else if( (c1.getDenomination()==Card.DENOMINATION_VALUE_2 && c2.getDenomination()==Card.DENOMINATION_VALUE_3)
                        ||
                        (c2.getDenomination()==Card.DENOMINATION_VALUE_2 && c1.getDenomination()==Card.DENOMINATION_VALUE_3)
                        ){
                    set.lowerBound = Card.DENOMINATION_VALUE_ACE_LOWER;
                    set.upperBound = Card.DENOMINATION_VALUE_6;

                    set.ensureSize(6);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(1,c1);
                        set.set(2,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(1,c2);
                        set.set(2,c1);
                    }
//System.out.println("\t\t\t\t\tsw4");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw4 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(5) 3 & 4 (potential 7 A..7)
                else if( (c1.getDenomination()==Card.DENOMINATION_VALUE_3 && c2.getDenomination()==Card.DENOMINATION_VALUE_4)
                        ||
                    (c2.getDenomination()==Card.DENOMINATION_VALUE_3 && c1.getDenomination()==Card.DENOMINATION_VALUE_4)
                ){
                    set.lowerBound = Card.DENOMINATION_VALUE_ACE_LOWER;
                    set.upperBound = Card.DENOMINATION_VALUE_7;

                    set.ensureSize(7);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(2,c1);
                        set.set(3,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(2,c2);
                        set.set(3,c1);
                    }
//System.out.println("\t\t\t\t\tsw5");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw5 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(6) Q & J  (potential 7 8..A)
                else if( (c1.getDenomination()==Card.DENOMINATION_VALUE_QUEEN && c2.getDenomination()==Card.DENOMINATION_VALUE_JACK)
                        ||
                        (c2.getDenomination()==Card.DENOMINATION_VALUE_QUEEN && c1.getDenomination()==Card.DENOMINATION_VALUE_JACK)
                        ){
                    set.lowerBound = Card.DENOMINATION_VALUE_8;
                    set.upperBound = Card.DENOMINATION_VALUE_ACE_HIGHER;

                    set.ensureSize(7);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(3,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(3,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw6");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw6 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(7) Q & K (potential 6 9..A) Q->3 K->4
                else if( (c1.getDenomination()==Card.DENOMINATION_VALUE_KING && c2.getDenomination()==Card.DENOMINATION_VALUE_QUEEN)
                        ||
                        (c2.getDenomination()==Card.DENOMINATION_VALUE_KING && c1.getDenomination()==Card.DENOMINATION_VALUE_QUEEN)
                    ){
                    set.lowerBound = Card.DENOMINATION_VALUE_9;
                    set.upperBound = Card.DENOMINATION_VALUE_ACE_HIGHER;

                    set.ensureSize(6);

                    //position cards within array
                    if(c1.getDenomination() < c2.getDenomination() ){
                        set.set(3,c1);
                        set.set(4,c2);
                    }
                    else if(c2.getDenomination() < c1.getDenomination() ){
                        set.set(3,c2);
                        set.set(4,c1);
                    }
//System.out.println("\t\t\t\t\tsw7");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw7 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }

                //(1) diff 1 , potential 8 (e.g. 4&5 1..8 , 8&9 5..12Q) 4->3 5->4
                else {
                    set.ensureSize(8);

                    if (c2.getDenomination() < c1.getDenomination()) {
                        set.lowerBound = c2.getDenomination() - 3;
                        set.upperBound = c1.getDenomination() + 3;
                        set.set(3,c2);
                        set.set(4,c1);
                    } else if (c1.getDenomination() < c2.getDenomination()) {
                        set.lowerBound = c1.getDenomination() - 3;
                        set.upperBound = c2.getDenomination() + 3;
                        set.set(3,c1);
                        set.set(4,c2);
                    }
//System.out.println("\t\t\t\t\tsw 1");
if(!player.isCpu()) System.out.println("\t\t\t\t\tsw1 - lB=" + set.lowerBound + " - uB=" + set.upperBound);
                }
            }


        }//end sequenceSet block



        return set;
    }



    public String getContentString(){
        StringBuffer sb = new StringBuffer("CardSet (");
        for(int i=0 ; i<size() ; i++ ){
            Card c = get(i);
            if(c==null)     sb.append("NULL@" + i);
            else    sb.append(c.getContentString() + "@" + i);
            if(i<size()-1) sb.append(", ");
        }
        sb.append(")");
        return sb.toString();
    }





    public boolean addCard(Card c){
System.out.println("@CardSet.addCard().. adding (" + c.getContentString() + ") to " + getContentString() + " set size = " + size() + " - complete " + isComplete());
        //if(!addable(c)) return false;

        //ensure space
        if(size() == getNonNullMembers().size()){
System.out.println("all members non null, adding slot");
            add(null);
System.out.println("resulting set::" + getContentString() );
        }

        if(isTypeSet()){
//System.out.println("@CardSet.addCard().. adding (" + c.getContentString() + ") to " + getContentString());
//System.out.println("typeSet");
            //insert the card in the position reserved for its type
            if(!isComplete()) {
                set(c.getType() - 1, c);
            }
            else {
                add(c.getType() - 1, c);
            }
System.out.println("inserted " + c.getContentString() + " in position " + (c.getType()-1));
System.out.println("resulting set::" + getContentString() );
        }

        else if(isSequenceSet()){

//System.out.println("sequenceSet");
            //card denomination in relation to upper and lower bounds
            //lowerBound ... c.denom .... upperBound
            //    d2           .              d1
            //int d1 = upperBound - c.getDenomination();

            if(!isComplete()) {
                int d2 = c.getDenomination() - lowerBound;
                System.out.println("index =  c.getDenomination() - lowerBound = " + c.getDenomination() + "-" + lowerBound + "=" + d2);

                //add space at the end if all memebers non null OR last member non null
                if (d2 == size() || get(size()-1)!=null) {
                    System.out.println("index=size, adding slot");
                    add(null);//allocate space for new member if needed
                    System.out.println("resulting set::" + getContentString());
                }

                set(d2, c);//insert new member
                System.out.println("inserted " + c.getContentString() + " in position " + d2);
                System.out.println("resulting set::" + getContentString());

                System.out.println("denom=" + c.getDenomination() + " ; lb=" + lowerBound + " ; ub=" + upperBound + " ; d2=denom-lb=" + d2 + " ");
//System.out.println("\t\tinserted " + c.getContentString() + " into position " + d2 + " (d2); d2=c.getDenomination() - lowerBound="+c.getDenomination()+" - " + lowerBound);
            }

            //a complete SEQ set can only be extended by the next card in either direction
            //since this passed addable(), compare addable card to low/high ends
            else{
                List<Card> nonNulls = getNonNullMembers();

                //first exception A(Higher) in 2,3,4...
                if(c.isAce() && nonNulls.get(0).getValue()==Card.DENOMINATION_VALUE_2){
                    add(0,c);
                }
                else if(c.getDenomination() < nonNulls.get(0).getDenomination()){
                    add(0,c);
                }
                else{
                    add(nonNulls.size() , c);
                }
            }


            return true;
        }


        return false;
    }


    /*
    Pre-requisite: addable(c)==true
    Returns an array made up of
    - cumValue of set after addition
    - number of cards in set after addition
     */
    public int[] simulateAddCard(Card c){

        int[] result = new int[] {-1,-1};

        //if type set and card passed addable(), should be the 3rd or 4th in set
        if(isTypeSet()){
            int simCV = cumulativeValue + typeSetDenomination;
            result[0] = simCV;
            result[1] = size()+1;
        }

        else{
            //duplicate set and add card c
            ArrayList<Card> replica = new ArrayList<>(this);

            int d2 = c.getDenomination() - lowerBound;
            replica.set(d2,c);

            //calc floor readiness e.g. prepareForFloor
            int seriesCounter = 0;
            int lastIndex = -1;
            int firstIndex = -1;
            boolean ready = false;

            ListIterator<Card> i = listIterator();
            while(i.hasNext())  i.next();

            while(i.hasPrevious() ) {
                Card card = i.previous();

                //if null
                if (card == null) {
                    //either this null member is the breakage after set completion... terminate at last non-null (previous) member
                    if (seriesCounter > 2) break;

                        //or this is the continuation of a gap in the series, or start of one
                        //continue and reset series until next non null member
                    else {
                        seriesCounter = 0;
                        continue;
                    }
                }

                seriesCounter++;

                if (seriesCounter > 2) {
                    //ensure that none of the cards that make the set ready is used in another complete set
                    ready = true;
                }


                //set first and last indices
                //prepare for possible breakage at 3rd,4th,5th member
                if (seriesCounter == 1) {
                    lastIndex = indexOf(card);
                }
                else if (seriesCounter > 2) {
                    firstIndex = indexOf(card);
                }

            }//end walk through

            if (ready) {
                List<Card> toRetain = replica.subList(firstIndex, lastIndex+1);
                replica.retainAll(toRetain);
                System.out.println("\t\t@CardSet.simulateAddCard() - retaining from "+firstIndex + " to " + (lastIndex+1) );
            }

            System.out.println("\t\t@CardSet.simulateAddCard() - result: " + getContentString());

            if(ready) {
                int simCV = 0;

                for (Iterator<Card> j = replica.iterator(); j.hasNext(); ) {
                    int x = j.next().getValue();
                    System.out.println("\t\t\t\t@CardSet.simulateAddCard() - incrementing set value by " + x);
                    simCV = simCV + x;
                }

                result[0] = simCV;
                result[1] = replica.size();

                System.out.println("\t\t@CardSet.simulateAddCard() - cumulativeValue: " + cumulativeValue);
            }
        }//end seq set

        return result;
    }



    public void ensureSize(int size) {
        // Prevent excessive copying while we're adding
        ensureCapacity(size);
        while (size() < size) {
            add(null);
        }
    }




    /*
        A weighted score taking into account:
            -   25% - How many cards are actually collected to make this a 3 card set                          (1)
            -   25% - planned cumulativeValue of the 3 card set                                                            (2)
            -   15% - How many cards are actually collected to make this a 4 card set                          (3)
            -   15% - planned cumulativeValue of the 4 card set                                                            (4)
            -   10% - How many cards are actually collected to make this a 5 card set                          (5)
            -   10% - planned cumulativeValue of the 5 card set                                                            (6)
            -   TODO The likeliness of getting cards needed to complete the set, e.g. if 2Hearts is required to complete the set and both have been
     */
    public double getSexiness(){

        //set ACE mode
        setAceMode();


//System.out.println("\t@getSexiness: " + getContentString() + " ------------------------------------------- ");
        Object[] s3 = null;
        Card[] c3 = null;
        int score3 = 0;
        int maxScore3 = -1;
        int count3 = 0;
        float countRatio3 = 0;
        float valueRatio3 = 0;

        s3 = getSetPossibility(3);
//System.out.println("\t\t\ts3 = " + s3);

        if(s3!=null) {
            c3 = (Card[]) s3[0];
            score3 = (int) s3[1];
            maxScore3 = (int) s3[2];
            count3 = 0;

            for (int i = 0; i < c3.length; i++) {
                if (c3[i] != null) {
                    count3++;
                }
            }

            countRatio3 = (count3 * 100)/ 3;//15% of final score
            valueRatio3 = (score3 * 100)/ maxScore3 ;//25% ...
        }
/*if(c3!=null)    System.out.println("\t\t\tc3 count:"+c3.length);
else    System.out.println("\t\t\tc3 is NULL");
System.out.println("\t\t\tcount3 = " + count3);
System.out.println("\t\t\tscore3 = " + score3);
System.out.println("\t\t\tcountRatio3 = " + countRatio3);
System.out.println("\t\t\tvalueRatio3 = " + valueRatio3);
*/


        Object[] s4 = null;
        Card[] c4 = null;
        int score4 = 0;
        int count4=0;
        int maxScore4 =0;
        float countRatio4 = 0;
        float valueRatio4 = 0;

        s4 = getSetPossibility(4);
//System.out.println("\t\t\ts4 = " + s4);

        if(s4!=null) {
            c4 = (Card[]) s4[0];
            score4 = (int) s4[1];
            count4 = 0;
            maxScore4 = (int) s4[2];


            for (int i = 0; i < c4.length; i++) {
                if (c4[i] != null) count4++;
            }

            countRatio4 = (count4 * 100)/4;//15% of final score
            valueRatio4 = (score4  * 100)/ maxScore4;//15% ...
        }
/*if(c4!=null)    System.out.println("\t\t\tc4 count:"+c4.length);
else    System.out.println("\t\t\tc4 is NULL");
System.out.println("\t\t\tcount4 = " + count4);
System.out.println("\t\t\tscore4 = " + score4);
System.out.println("\t\t\tcountRatio4 = " + countRatio4);
System.out.println("\t\t\tvalueRatio4 = " + valueRatio4);
*/


        Object[] s5 = null;
        Card[] c5 = null;
        int score5 = 0;
        int count5=0;
        int maxScore5 = 0;
        float countRatio5 = 0;
        float valueRatio5 = 0;

        s5 = getSetPossibility(5);
//System.out.println("\t\t\ts5 = " + s5);

        if(s5!=null){
            c5 = (Card[])s5[0];
            score5 = (int)s5[1];
            count5=0;
            maxScore5 = (int)s5[2];

            for(int i=0 ; i<c5.length ; i++){
                if(c5[i]!=null)     count5++;
            }

            countRatio5 = (count5 * 100) / 5;//10% of final score
            valueRatio5 = (score5 * 100) / maxScore5;//10% ...

        }
/*
if(c5!=null)    System.out.println("\t\t\tc5 count:"+c5.length);
else    System.out.println("\t\t\tc5 is NULL");
System.out.println("\t\t\tcount5 = " + count5);
System.out.println("\t\t\tscore5 = " + score5);
System.out.println("\t\t\tcountRatio5 = " + countRatio5);
System.out.println("\t\t\tvalueRatio5 = " + valueRatio5);
*/


        double sexiness = 0.25*(countRatio3 + valueRatio3) + 0.15*(countRatio4 + valueRatio4) + 0.10*(countRatio5 + valueRatio5);
//System.out.println("\t\t@GetSexiness() for "+getContentString() +
//"\t\t = 0.25*("+countRatio3+" + "+valueRatio3+") + 0.15*("+countRatio4+" + "+valueRatio4+") + 0.10*("+countRatio5+" + "+valueRatio5+")" +
//"\t\t= "+sexiness);


        //            -   25% - How many cards are actually collected to make this a 3 card set                          (1)
        //                possible scores: 100% (3 cards) 67%(2 cards) 33%(1 card)

        //            -   25% - planned cumulativeValue of the 3 card set                                                (2)
        //                max score (100%) is 30

        //            -   15% - How many cards are actually collected to make this a 4 card set                          (3)

        //            -   15% - planned cumulativeValue of the 4 card set                                                (4)
        //                max score (100%) is 40

        //            -   10% - How many cards are actually collected to make this a 5 card set                          (5)
        //            -   10% - planned cumulativeValue of the 5 card set                                                (6)
        //                max score (100%) is 50

//System.out.println("\t\t\tfinal score = " + sexiness);
//System.out.println(" ------------------------------------------------------------ ");


        //reset ACE mode
        resetAceMode();


        return sexiness;
    }


    public void setAceMode(){
        ArrayList<Card> list = getNonNullMembers();
        for(int i=0 ; i<list.size() ; i++){
            Card c = list.get(i);
            if(c.isAce()){

                System.out.println("@CardSet.setAceMode() - isSequenceSet(): " + isSequenceSet());
                System.out.println("@CardSet.setAceMode() - list.size(): " + list.size());
                System.out.println("@CardSet.setAceMode() - i: " + +i);
                if(list.size()>1 && list.get(1)!=null)   System.out.println("@CardSet.setAceMode() - list.get(1): " + list.get(1).getContentString());
                if(list.size() > 2 && list.get(2)!=null)   System.out.println("@CardSet.setAceMode() - list.get(2): " + list.get(2).getContentString());

                //make an exception for A,2,3 sets:
                if(isSequenceSet() && list.size()==3 && i==0 && list.get(1).getDenomination()==Card.DENOMINATION_VALUE_2 && list.get(2).getDenomination()==Card.DENOMINATION_VALUE_3){
                    c.setAceMode(Card.ACE_MODE_HIGH);
                }
                else {
                    //find antother card for Card.setAceModeInRelationToCard()
                    Card anotherCard = null;
                    for(Iterator<Card> it = list.iterator() ; it.hasNext() ; ){
                        Card x = it.next();
                        if(x==null) continue;
                        if(x.equals(c)) continue;
                        anotherCard = x;
                        break;
                    }
                    c.setAceModeInRelationToCard(anotherCard);
                }
//System.out.println("@CardSet.setAceMode() - " +getContentString() + ":: " + c.getContentString() + " denom set to " + c.getAceModeString());
            }
        }
    }

    public void resetAceMode(){
        ArrayList<Card> list = getNonNullMembers();
        for(int i=0 ; i<list.size() ; i++) {
            Card c = list.get(i);
            if (c.isAce()) {
                c.resetAceMode();
            }
        }
    }

    public ArrayList<Card> getNonNullMembers(){
        ArrayList<Card> list = new ArrayList();
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c!=null) list.add(c);
        }
        return list;
    }

    public static String dumpCardArray(Card[] array){
        StringBuffer sb = new StringBuffer("[");
        for(int i=0 ; i < array.length ; i++){
            if(array[i]!=null){
                sb.append(array[i].getContentString());
            }
            else{
                sb.append("NULL");
            }
            if(i < array.length-1)  sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    /*
    returns array [nr cards collected to make up intended set , planned cumulative value of completed intended set]
    */
    /**
                intendedSetSize = 3
                GET 1
                SEEK 2
                    FORK
                        SEEK 3
                            FORK
                                CALL 3
                            CALL 2
                    CALL 1
                REPEAT UNTIL index:size-intendedSetSize

                intendedSetSize = 4
                GET 1
                SEEK 2
                    FORK
                        SEEK 3
                            FORK
                                SEEK 4
                                    FORK
                                        CALL 4
                                    CALL 3
                            CALL 2
                    CALL 1
                REPEAT UNTIL index:size-intendedSetSize

                intendedSetSize = 5
                GET 1
                SEEK 2
                    FORK
                        SEEK 3
                            FORK
                                SEEK 4
                                    FORK
                                        SEEK 5
                                            FORK
                                                CALL 5
                                        CALL 4
                                    CALL 3
                            CALL 2
                    CALL 1
                REPEAT UNTIL  index:size-intendedSetSize

            return best from floorSetList

     */
    public Object[] getSetPossibility(int intendedSetSize){


        if(isSequenceSet()) {
            //foreach card until size-intendedSetSize
            int lastPossibleIndexForSeriesBeginning = size()-intendedSetSize;
//System.out.println("\t\t\t@getSetPossiblity(" + intendedSetSize + ") - for SEQUENCE type set size=" + size() + " ::: cards in set: " + this.getContentString() + " - lastPossibleIndexForSeriesBeginning="+lastPossibleIndexForSeriesBeginning);
            for (int i = 0; i < lastPossibleIndexForSeriesBeginning; i++) {
                Card x = get(i);
                Card[] buffer = new Card[intendedSetSize];
                buffer[0] = x;
                int lastIndexToTraverse = i + intendedSetSize-1;

//System.out.println("i=" + i + " - x=" + x + " :: now traversing set from position 1 to position " + lastPossibleIndexForSeriesBeginning);
//set:8 intededSize=3 set:{n,n,n,n,4,5,n,n,n} // set:8 intededSize=5 set:{n,4,5,n,n,n,n,n} //
                //j is the step for finding next in sequence: possible values: 1,2,3 / 1,2,3,4 / 1,2,3,4,5
                for (int j = i + 1; j < lastIndexToTraverse+1; j++) {
                    Card y = get(j);
                    buffer[j] = y;

//System.out.println("\ti=" + i + " - x=" + x + " - j=" + j + " - y=" + y + " - buffer now has " + buffer.length + "members:: " + dumpCardArray(buffer));

                    //if y is null, continue at this point to move to next in seq
                    /*if(y==null){
System.out.println("\tnothing at position " + j);
                        continue;
                    }*/

                    //end not reached? continue to traverse neighbours
                    if(j < lastIndexToTraverse){
//System.out.println("\t\tfork 1: end not reached - did NOT reach position " + (intendedSetSize-1));
                        buffer =  (Card[]) evaluateSequenceSetPossibility(buffer)[0];
//System.out.println("\t\t\t\tcalled evaluateSequenceSetPossibility() and result has " + buffer.length + " members");
                    }
                    // (1) TERMINATE
                    else {
//System.out.println("\t\tfork 2: terminating - reached position " + (intendedSetSize-1));
                        Object[] res = evaluateSequenceSetPossibility(buffer);
                        Card[] array = (Card[])res[0];
//System.out.println("@getSetPossiblity(" + intendedSetSize + ") SEQ - set size=" + size() + " ::: cards in set: " + this.getContentString());
/*System.out.println("\t\t\t\t\t\tresult: cValue="+res[1] + " - maxValue="+res[2] + " - cards:");
for(int iQ=0; iQ < array.length ; iQ++){
    if( array[iQ]!=null) System.out.println("\t\t\t\t\t\t\t" + array[iQ].getContentString());
    else    System.out.println("\t\t\t\t\t\t\tNULL");
}*/
//System.out.println("=================================================");
                        return evaluateSequenceSetPossibility(buffer);
                    }
                }
            }
        }//end sequenceSet

        //typeSet
        else if(isTypeSet()){
//System.out.println("\t\t\t@getSetPossiblity(" + intendedSetSize + ") TYPE - set size=" + size() + " ::: cards in set: " + this.getContentString());
//System.out.println("isTypeSet()");
//System.out.println();
//System.out.println();
            int n = getNumCardsCollected();
            int maxValue = typeSetValue * intendedSetSize;

//System.out.println("n="+n + " - maxValue="+maxValue);

            int cValue = 0;
            for(int i=0 ; i < size() ; i++ ){
                Card c = get(i);
                if(c!=null){
                    cValue +=typeSetValue;
//System.out.println("\t" + i + ": available: " + c.getContentString() + " - cValue="+cValue );
                }
/*else{
System.out.println("\t" + i + ": NULL");
}*/
                if(cValue-maxValue==0){
//System.out.println("max value reached, breaking: " );
                    break;
                }
                else{
//System.out.println("max value NOT reached, continuing " );
                }
            }

//System.out.println("max value reached OR end of cards in set " );
            Card[] array = new Card[size()];
            for(int i=0; i < array.length ; i++){
                array[i] = get(i);
            }
//System.out.println("available cards="+n + "/" + intendedSetSize + " - cValue="+cValue + " - maxValue="+maxValue );
            Object[] res = new Object[] {array , cValue , maxValue};
//System.out.println("@getSetPossiblity(" + intendedSetSize + ") TYPE - set size=" + size() + " ::: cards in set: " + this.getContentString());
/*System.out.println("\t\t\t\t\t\tresult: cValue="+cValue + " - maxValue="+maxValue + " - cards:");
for(int i=0; i < array.length ; i++){
    if( array[i]!=null) System.out.println("\t\t\t\t\t\t\t" + array[i].getContentString());
    else    System.out.println("\t\t\t\t\t\t\tNULL");
}*/
//System.out.println("=================================================");
            return res;
        }

        return null;
    }


    /*
    Returns a set comprising available cards of a set (unavailable are null) , and the cumulativeValue of available cards
     */
    private Object[] evaluateSequenceSetPossibility(Card[] availableCards){


        //init cumulative value with value of first card
        int cumulativeValue=0;
        int maxValue = 0;
        int lastCardValue = 0;//if next card is null, holds value to be added to maxValue (planned value)

        for(int i=0 ; i < availableCards.length ; i++ ){
            Card c = availableCards[i];

            //card not null? increment cum. and max values
            if(c!=null){
                cumulativeValue =+ c.getValue();
                maxValue = maxValue + c.getValue();
                lastCardValue = c.getValue();
            }

            //null card, increment max val only if seq
            else{
                maxValue = maxValue + lastCardValue;
                if(isSequenceSet()){
                    lastCardValue = lastCardValue + 1;
                }
            }

        }

        return new Object[] {availableCards , cumulativeValue , maxValue};
    }



    private int getNumCardsCollected(){
        int counter=0;
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            Card c = i.next();
            if(c!=null)  counter++;
        }
        return counter;
    }


    @Override
    public boolean contains(Object o){
        if(o==null) return false;

        try{
            Card anotherCard = (Card) o;

            for(Iterator<Card> i = iterator() ; i.hasNext() ; ) {
                Card card = i.next();
                if(card==null)  continue;
                if(anotherCard.equals(card))    return true;
                if(anotherCard.sameCardDifferentDeck(card)) return true;
            }

            return false;
        }catch(ClassCastException cce){
            return false;
        }
    }



    public boolean allMembersNull(){
        for(Iterator<Card> i = iterator() ; i.hasNext() ; ){
            if(i.next()!=null)  return false;
        }
        return true;
    }


    /*
    TODO
    - APPROACH I:       2 common cards will make 2 sets duplicates.
                        LIMITATION: 2 SETS WITH 2 CARDS EACH - ONE CARD IS COMMON - WILL TEST -VE.
    - APPROACH II:      1 common card will make 2 sets duplicates.
                        INCORRECT.
     */

    public boolean overlaps(CardSet anotherSet){
        setAceMode();
        anotherSet.setAceMode();

//System.out.println("----------------- BEGIN @CardSet.overlaps() -------------------");
        boolean match = false;
        boolean hit1 = false;

        //2 type set overlapping : should be identical as members are sorted by type when creating set or adding to it
        if(isTypeSet() && anotherSet.isTypeSet()){
            match = equals(anotherSet);
        }

        else {
            for (Iterator<Card> i = iterator(); i.hasNext(); ) {
                Card c = i.next();

                if (c == null) continue;

                for (Iterator<Card> j = anotherSet.iterator(); j.hasNext(); ) {
                    Card another = j.next();

                    if (another == null) continue;

                    if (c.equals(another)) {
                        //match = true;break;//approach 2

                        // Approach 1
                        if (!hit1) {
                            hit1 = true;
                        } else {
                            match = true;
                            break;
                        }


                    }
                }

            /*
            an exception: hit1 true
                            &&
                          overlapping card is the smallest available card of one set and the biggest available card in the other
                            && both sets are of the same type as hit1 will also be true if same

            */
                if (isSequenceSet() && anotherSet.isSequenceSet() && hit1) {
                    if (getSmallestCardOfSequenceSet().equals(anotherSet.getBiggestCardOfSequenceSet())
                            ||
                            getBiggestCardOfSequenceSet().equals(anotherSet.getSmallestCardOfSequenceSet())
                            ) {
                        match = true;
                    }
                }
            }
        }//end seq


        resetAceMode();
        anotherSet.resetAceMode();

//System.out.println("\t\t@CardSet.overlaps():" + getContentString() + " OVERLAP " + anotherSet.getContentString() + " is " + match);
        return match;
    }


    ArrayList<Card> cardsToDiscardPostCompletion = new ArrayList<>();

    /*
    - chose highest complete series and "trims" the set accordingly
    - compute value
    - return true if set can be moved to floor

    The members trimmed out are to be placed in a container for RoundPlayer to pick up and decide what to do with them (form (an)other set(s) OR place in unmatchedCards)
     */
    public boolean prepareForFloor() {
        boolean ready = false;
        cumulativeValue = 0;

        setAceMode();

        List<Card> cardsUsedInOtherCompleteSets = new ArrayList<>();

        //type set: 3+ non null members make the set ready
        if (isTypeSet()) {
            if (getNonNullMembers().size() > 2) {

                //remove nulls
                for (Iterator i = iterator(); i.hasNext(); ) {
                    if (i.next() == null) i.remove();
                }

                //ensure that none of the cards that make the set ready is used in another complete set
                    //2 cards used in complete sets
                    //OR
                    //1 card used in complete set while size==3
                for(Iterator<Card> z = iterator() ; z.hasNext() ; ) {
                    Card c = z.next();

                    for (Iterator<CardSet> j = player.potentialSets.iterator(); j.hasNext(); ) {
                        CardSet otherSet = j.next();
                        if(equals(otherSet))    continue;//skip self

                        if (otherSet.isComplete() && otherSet.contains(c)){
                            cardsUsedInOtherCompleteSets.add(c);
                            break;
                        }
                    }
                }

                if(size()-cardsUsedInOtherCompleteSets.size()>2)   ready = true;

System.out.println("@CardSet.prepareForFloor()::" + getContentString() + "- type set ready="+ready);
            }
        }

        //seq type: start with last member down
        //if a series can be formed with 3 non null members
        else if(isSequenceSet()){
System.out.println("@CardSet.prepareForFloor()::" + getContentString() + "- SEQ set ");
            int seriesCounter = 0;
            int lastIndex = -1;
            int firstIndex = -1;

            ListIterator<Card> i = listIterator();
            while(i.hasNext())  i.next();

            while(i.hasPrevious() ) {
                Card c = i.previous();

                //if null
                if (c == null) {
                    //either this null member is the breakage after set completion... terminate at last non-null (previous) member
                    if(seriesCounter>2) break;

                    //or this is the continuation of a gap in the series, or start of one
                    //continue and reset series until next non null member
                    else {
                        seriesCounter = 0;
                        continue;
                    }
                }


System.out.println("\t now looking at " + c.getContentString());

                //ensure that none of the cards that make the set ready is used in another complete set
                //if card is used in another complete set
                    //1. series is broken, reset
                    //2. add to cardsUsedInOtherCompleteSets
                boolean cardUsedInAnotherSet = false;
                for (Iterator<CardSet> j = player.potentialSets.iterator(); j.hasNext(); ) {
                    CardSet otherSet = j.next();
                    if(equals(otherSet))    continue;

                    if (otherSet.isComplete() && otherSet.contains(c)){
                        cardsUsedInOtherCompleteSets.add(c);
                        cardUsedInAnotherSet = true;
                        break;
                    }
                }

                if(cardUsedInAnotherSet){
System.out.println("\t\t cardUsedInAnotherSet ... cont");
                    continue;//no need to place this in cardsToDiscardPostCompletion... already used in a set
                }

                //increment counter and mark set as ready
                    //counter is 0, this is the first in a new series
                    //if counter is 1 or 2, build up the series
                    //if counter is 3, set is ready
                    //if counter is 4, build up the series
                    //if counter is 5, break the series
                seriesCounter++;

System.out.println("\t\t seriesCounter=" + seriesCounter + " ");
                if (seriesCounter > 2) {
                    ready = true;
                }
System.out.println("\t\t ready=" + ready + " ");


                //set first and last indices
                    //prepare for possible breakage at 3rd,4th,5th member
                if (seriesCounter == 1) {
                    lastIndex = indexOf(c);
System.out.println("\t\t setting lastIndex to " + lastIndex + " ");
                }
                else if (seriesCounter > 2) {
                    firstIndex = indexOf(c);
System.out.println("\t\t setting firstIndex to " + firstIndex + " ");
                }


System.out.println("@CardSet.prepareForFloor()::" + getContentString() + "- seq set looking at " + c.getContentString() +
        " seriesCounter after increment = " + seriesCounter +
        " - firstIndex = " + firstIndex + " - lastIndex = " + lastIndex) ;

                //cap at 5
                if(seriesCounter>4){
System.out.println("\t\t 5 members reached, breaking ");
                    break;
                }

            }//end walk-through


System.out.println("\t\tready="+ready);

            //if set ready, trim to ready size
            //any remaining cards must be placed in cardsToDiscardPostCompletion for RoundPlayer
            //also update lowerBound/upperBound
            if (ready) {
                List<Card> toRetain = subList(firstIndex, lastIndex+1);
System.out.println("@CardSet.prepareForFloor()::" + getContentString() + " retaining from "+firstIndex + " to " + (lastIndex+1) );


                //if a card is not retained, place in cardsUsedInOtherCompleteSets
                for(Iterator<Card>  j = iterator() ; j.hasNext() ; ){
                    Card c = j.next();

                    if(!toRetain.contains(c))   cardsToDiscardPostCompletion.add(c);
                }


                retainAll(toRetain);
                lowerBound = get(0).getDenomination();
                upperBound = get(size()-1).getDenomination();
            }

System.out.println("@CardSet.prepareForFloor()::" + getContentString() + " result: " + getContentString());
        }//end seq set



        //if set is ready, compute cumulativeValue and mark set as complete
        if(ready) {
            for (Iterator<Card> i = iterator(); i.hasNext(); ) {
                int x = i.next().getValue();
System.out.println("\t@CardSet.prepareForFloor()::" + getContentString() + " incrementing set value by " + x);
                cumulativeValue = cumulativeValue + x;
            }
            complete = true;
System.out.println("@CardSet.prepareForFloor()::" + getContentString() +" cumulativeValue: " + cumulativeValue);
        }

        resetAceMode();
        return ready;
    }


}
