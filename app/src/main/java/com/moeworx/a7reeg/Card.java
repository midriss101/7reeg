package com.moeworx.a7reeg;

public class Card implements  java.io.Serializable,Comparable{

    boolean debug=false;

    public static final int DENOMINATION_VALUE_ACE_LOWER = 1;
    public static final int DENOMINATION_VALUE_2 = 2;
    public static final int DENOMINATION_VALUE_3 = 3;
    public static final int DENOMINATION_VALUE_4 = 4;
    public static final int DENOMINATION_VALUE_5 = 5;
    public static final int DENOMINATION_VALUE_6 = 6;
    public static final int DENOMINATION_VALUE_7 = 7;
    public static final int DENOMINATION_VALUE_8 = 8;
    public static final int DENOMINATION_VALUE_9 = 9;
    public static final int DENOMINATION_VALUE_10 = 10;
    public static final int DENOMINATION_VALUE_JACK = 11;
    public static final int DENOMINATION_VALUE_QUEEN = 12;
    public static final int DENOMINATION_VALUE_KING = 13;
    public static final int DENOMINATION_VALUE_ACE_HIGHER = 14;



    public static final int TYPE_VALUE_CLUB = 1;
    public static final int TYPE_VALUE_DIAMOND = 2;
    public static final int TYPE_VALUE_SPADE = 3;
    public static final int TYPE_VALUE_HEART = 4;

    /* an integer id for each card denomination, different than card value used to calculate floor "sum"*/
    int denomination;
    int getDenomination(){
        return denomination;
    }

    private int value = -1;
    public int getValue(){
        if(value==-1){
            if(denomination==DENOMINATION_VALUE_10 || denomination < DENOMINATION_VALUE_10){
                value = denomination;
            }
            else{
                value = DENOMINATION_VALUE_10;
            }
        }
        return value;
    }


    int type;
    int getType(){
        return type;
    }


    Deck deck;
    public Deck getDeck(){
        return deck;
    }



    public String getDenominationString(){
        switch (denomination){
            case DENOMINATION_VALUE_ACE_LOWER:{
                return "A";
            }
            case DENOMINATION_VALUE_2:{
                return "2";
            }
            case DENOMINATION_VALUE_3:{
                return "3";
            }
            case DENOMINATION_VALUE_4:{
                return "4";
            }
            case DENOMINATION_VALUE_5:{
                return "5";
            }
            case DENOMINATION_VALUE_6:{
                return "6";
            }
            case DENOMINATION_VALUE_7:{
                return "7";
            }
            case DENOMINATION_VALUE_8:{
                return "8";
            }
            case DENOMINATION_VALUE_9:{
                return "9";
            }
            case DENOMINATION_VALUE_10:{
                return "10";
            }
            case DENOMINATION_VALUE_JACK:{
                return "J";
            }
            case DENOMINATION_VALUE_QUEEN:{
                return "Q";
            }
            case DENOMINATION_VALUE_KING:{
                return "K";
            }
            case DENOMINATION_VALUE_ACE_HIGHER:{
                return "A";
            }
        }

        return null;
    }


    public static String getDenominationString(int denomination){
        switch (denomination){
            case DENOMINATION_VALUE_ACE_LOWER:{
                return "A";
            }
            case DENOMINATION_VALUE_2:{
                return "2";
            }
            case DENOMINATION_VALUE_3:{
                return "3";
            }
            case DENOMINATION_VALUE_4:{
                return "4";
            }
            case DENOMINATION_VALUE_5:{
                return "5";
            }
            case DENOMINATION_VALUE_6:{
                return "6";
            }
            case DENOMINATION_VALUE_7:{
                return "7";
            }
            case DENOMINATION_VALUE_8:{
                return "8";
            }
            case DENOMINATION_VALUE_9:{
                return "9";
            }
            case DENOMINATION_VALUE_10:{
                return "10";
            }
            case DENOMINATION_VALUE_JACK:{
                return "J";
            }
            case DENOMINATION_VALUE_QUEEN:{
                return "Q";
            }
            case DENOMINATION_VALUE_KING:{
                return "K";
            }
            case DENOMINATION_VALUE_ACE_HIGHER:{
                return "A";
            }
        }

        return "-1";
    }



    public String getTypeString(){
        switch (type){
            case TYPE_VALUE_CLUB:   return "club";
            case TYPE_VALUE_DIAMOND:    return "diamond";
            case TYPE_VALUE_HEART: return "heart";
            case TYPE_VALUE_SPADE:  return "spade";
        }
        return null;
    }

    public static String getTypeString(int type){
        switch (type){
            case TYPE_VALUE_CLUB:   return "club";
            case TYPE_VALUE_DIAMOND:    return "diamond";
            case TYPE_VALUE_HEART: return "heart";
            case TYPE_VALUE_SPADE:  return "spade";
        }
        return null;
    }

    //for ace only
    public static int ACE_MODE_HIGH = 1;
    public static int ACE_MODE_LOW = 2;
    //public static int ACE_MODE_NONE = -1;

    private int aceMode = ACE_MODE_LOW;
    public int getAceMode(){
        return aceMode;
    }

    public boolean isAceHigherMode(){
        if(aceMode==ACE_MODE_HIGH)  return true;
        return false;
    }

    public boolean isAceLowerMode(){
        if(aceMode==ACE_MODE_LOW)   return true;
        return false;
    }


    public void setAceMode(int aceMode){
        this.aceMode = aceMode;
        if(isAceHigherMode())   denomination = DENOMINATION_VALUE_ACE_HIGHER;
        else if(isAceLowerMode())   denomination = DENOMINATION_VALUE_ACE_LOWER;
    }


    public String getAceModeString(){
        if(!isAce())    return null;
        if(aceMode==ACE_MODE_HIGH)  return "ACE_MODE_HIGH";
        else if(aceMode==ACE_MODE_LOW) return "ACE_MODE_LOW";
        return null;
    }


    /**
     * Applies only if the card is an ace.
     * Adjusts denomination to 1 or 10 in relation to the card supplied.
     * Must be reset using resetAceMode() once intended usage is done in order to prepare card for other ops. e.g. being matched against another card.
     * @param c
     */
    public void setAceModeInRelationToCard(Card c){


        //null card
        if(c==null) return;

        //none_ace
        if(!isAce())    return;


        //if other card has same denomination (TYPE SET OP), high mode
        if(c.isAce()){
            setAceMode(ACE_MODE_HIGH);
            if(debug) System.out.println("sw1");
        }

        //if other card c is 2,3:high mode
        else if(c.getDenomination() < 4){
            setAceMode(ACE_MODE_HIGH);
            if(debug) System.out.println("sw2");
        }

        //if other card c is 4,5 (SEQ SET OP): low mode
        else if(c.getDenomination() < 6){
            setAceMode(ACE_MODE_LOW);
            if(debug) System.out.println("sw3");
        }


        //if other card c is K,Q,J,10 (SEQ SET OP): high mode
        else if(c.getDenomination() > 9){
            setAceMode(ACE_MODE_HIGH);
            if(debug) System.out.println("sw4");
        }

if(debug) System.out.println("--------------- Card.setAceMode() for :" + getContentString() + " in relation to " + c.getContentString() + " to " + getAceModeString() + " ---------------------");


    }

    public void resetAceMode(){
        if(!isAce())    return;
        setAceMode(ACE_MODE_LOW);
//System.out.println("--------------- Card.resetAceMode() for :" + getContentString() + " to " + getAceModeString());
    }

    public boolean isAce(){
        if(denomination==DENOMINATION_VALUE_ACE_LOWER || denomination==DENOMINATION_VALUE_ACE_HIGHER)   return true;
        return false;
    }



    public Card(int denomination,int type, Deck deck){
        this.denomination = denomination;
        this.type = type;
        this.deck = deck;
    }




    public String toString(){
        return getDenominationString();
    }


    public String getContentString(){
        String x="";
        if(denomination==DENOMINATION_VALUE_ACE_HIGHER) x="(Higher)";
        else if(denomination==DENOMINATION_VALUE_ACE_LOWER) x="(Lower)";
        return getDenominationString() + " of " + getTypeString() + "s" + " " + x;
    }



    public String getExtendedContentString(){
        String x="";
        if(denomination==DENOMINATION_VALUE_ACE_HIGHER) x="(Higher)";
        else if(denomination==DENOMINATION_VALUE_ACE_LOWER) x="(Lower)";
        return getDenominationString() + " of " + getTypeString() + "s" + " " + x + " of deck " + deck;
    }



    @Override
    public boolean equals(Object obj) {
        if(obj==null)   return false;
        try {
            Card otherCard = (Card) obj;
//System.out.println(getExtendedContentString() + ".equals(" + otherCard.getExtendedContentString() + ")= " + (otherCard.denomination==denomination) + " && " +  (otherCard.type==type) +"&&" + (deck.equals(otherCard.deck)) + "=" + ((otherCard.denomination==denomination) && (otherCard.type==type) && (deck.equals(otherCard.deck))) );
            return ((otherCard.denomination==denomination) && (otherCard.type==type) && (deck.equals(otherCard.deck)) );
        }catch(ClassCastException cce){
            return false;
        }
    }

    public boolean sameCardDifferentDeck(Card otherCard){
        return ((otherCard.denomination==denomination) && (otherCard.type==type) && (!deck.equals(otherCard.deck)) );
    }


    /*
    Used only by RoundPlayer.UngroupedCardsTreeSet for SEQUENCE_TYPE set creation and qualification
    Used for ordering cards...
     */

    @Override
    public int compareTo(Object o) {
        try {
            Card another = (Card) o;
//System.out.println("------- Card.compareTo() called --------------------");
            //denom
            if (another.getDenomination() < getDenomination()) return 1;
            else if (another.getDenomination() > getDenomination()) return -1;
            else {
                //type
                if (another.getType() < getType()) return 1;
                else if (another.getType() > getType()) return -1;
                else {
                    //deck
                    if (another.getDeck().hashCode() < getDeck().hashCode()) return 1;
                    else if (another.getDeck().hashCode() > getDeck().hashCode()) return -1;
                    else{
                        return 0;
                    }
                }
            }
        }catch (ClassCastException cce){
            return -1;
        }
    }



}
