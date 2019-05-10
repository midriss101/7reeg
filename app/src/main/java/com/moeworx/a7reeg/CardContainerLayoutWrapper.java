package com.moeworx.a7reeg;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Space;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import java.util.jar.Attributes;

public class CardContainerLayoutWrapper implements View.OnDragListener {



    LinearLayout layout;
    String containerName;
    int containerType = -1;
    int rotation;
    int colorCode = -1;



    public static int CARD_CONTAINER_TYPE = 1;
    public static int CARD_SET_CONTAINER_TYPE = 2;





    List<Card> cards = new ArrayList<>();
    public void setCards(List<Card> cards){
        this.cards = cards;
    }



    List<CardSet> cardSets = new ArrayList<>();
    public void setCardSets(List<CardSet> cardSets){
        this.cardSets = cardSets;
    }

    public void addCardSet(CardSet set){
        cardSets.add(set);
        displayCardSet(set);
    }


    GameActivity gameActivity;

    CardContainerLayoutWrapper(GameActivity gameActivity , LinearLayout layout , String containerName , int containerType , int rotation, int colorCode){
        this.gameActivity = gameActivity;
        this.layout = layout;
        this.colorCode = colorCode;
        this.containerName = containerName;
        this.containerType = containerType;
        this.rotation = rotation;

    }


    public  void setup(){
        layout.setOnDragListener(this);

        if(rotation==0 || rotation==180) {
            layout.setOrientation(LinearLayout.HORIZONTAL);
        }
        else if(rotation==90 || rotation==270){
            layout.setOrientation(LinearLayout.VERTICAL);
        }

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); //white background
        border.setStroke(5, colorCode); //black border with full opacity
        //border.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        //border.setColors(new int[]{Color.BLACK , Color.GRAY ,  Color.WHITE});
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackgroundDrawable(border);
        } else {
            layout.setBackground(border);
        }
    }





    public boolean isCardContainer(){
        if(containerType == CARD_CONTAINER_TYPE)    return true;
        return false;
    }

    public boolean isCardSetContainer(){
        if(containerType == CARD_SET_CONTAINER_TYPE)    return true;
        return false;
    }


    /*
    if -1, all cards will be displayed e.g. deck
    if set to +ve value, will display only as much e.g. reject
     */
    int numCardsToShow = -1;


    public void layoutContainer(){
        layout.removeAllViews();

        if(isCardContainer()) {
            if(numCardsToShow==-1 || (cards.size() < numCardsToShow || cards.size()==numCardsToShow) ) {
                for (Iterator<Card> i = cards.iterator(); i.hasNext(); ) {
                    displayCard(i.next());
                }
            }
            else{
                for(Iterator<Card> i = cards.subList(cards.size()-1-numCardsToShow, cards.size()).iterator() ; i.hasNext() ; ){
                    displayCard(i.next());
                }
            }
        }
        else if (isCardSetContainer()){
            for(Iterator<CardSet> i = cardSets.iterator() ; i.hasNext() ;){
                displayCardSet(i.next());
            }
        }
    }

    /**
     * Used by reject
     * @param cl
     */
    public void addCardLayout(CardLayout cl){
        layout.addView(cl);
        layout.invalidate();
        cardLayoutMap.put(cl.card,cl);
    }

    HashMap<Card,CardLayout> cardLayoutMap = new HashMap();

    //ArrayList  cardSetHashMap = new ArrayList();//x:set

    /*
    class CardSetOrderedList extends TreeSet{

        HashMap<Float,CardSet> setHashMap = new HashMap<>();

        @Override
        public boolean add(Object o){
            try{
                Object[] entry = (Object[])o;
                float x = (Float)entry[0];
                CardSet set = (CardSet) entry[1];

                super.add(x);
                setHashMap.put(x,set);

                return true;

            }catch(ClassCastException cce){
                return false;
            }
        }

        @Override
        public boolean addAll(Collection c) {
            throw new UnsupportedOperationException("Unsupported");
        }

        public Float[] dumpX(){
            Float[] x = new Float[size()];
            toArray(x);
            return x;
        }

        public CardSet getSet(float x){
            return setHashMap.get(x);
        }

        public void debug(){
            System.out.println("debugging CardSet CardContainer");
            System.out.println("dumping x TreeSet");
            StringBuffer sb = new StringBuffer();
            for(Iterator i = iterator() ; i.hasNext() ; ){
                sb.append(i.next());
                if(i.hasNext()) sb.append(",");
            }
            System.out.println(sb.toString() + "\n");

            System.out.println("dumping hashmap");
            sb = new StringBuffer();
            for(Iterator<Float> i = setHashMap.keySet().iterator() ; i.hasNext() ; ){
                Float x = i.next(); CardSet set = setHashMap.get(x);
                sb.append(x + ": " + set.getContentString());
                if(i.hasNext()) sb.append(";    ");
            }
            System.out.println(sb.toString() + "\n");

        }
    }
    */
    //CardSetOrderedList cardSetOrderedList = new CardSetOrderedList();


    public void displayCard(Card card){
        CardLayout cl = new CardLayout(gameActivity , card,containerName,rotation);
        cl.layoutCard();
        layout.addView(cl);
        layout.invalidate();
        cardLayoutMap.put(card,cl);
    }


    public void displayCard(Card card , int targetPosition){
        CardLayout cl = new CardLayout(gameActivity , card,containerName,rotation);
        cl.layoutCard();
        layout.addView(cl,targetPosition);
        layout.invalidate();
        cardLayoutMap.put(card,cl);
    }


    public void displayCardSet(CardSet set){
        LinearLayout setLayout = new LinearLayout(gameActivity);

        if(rotation==0 || rotation==180) {
            setLayout.setOrientation(LinearLayout.HORIZONTAL);
        }
        else if(rotation==90 || rotation==270){
            setLayout.setOrientation(LinearLayout.VERTICAL);
        }

        //create cards and add to setLayout
        for(Iterator<Card> i = set.iterator() ; i.hasNext() ; ){
            Card card = i.next();
            if(card==null)  continue;

System.out.println("@CardContainerWrapper.displayCardSet():: displaying " + card.getContentString() + " of " + set.getContentString());
            CardLayout cl = new CardLayout(gameActivity , card,containerName,rotation);
            cl.layoutCard();
            setLayout.addView(cl);
        }

        //append set to layout
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT , LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(8,8,15,8);
        setLayout.setLayoutParams(params);
        layout.addView(setLayout);
        layout.invalidate();

        //cardSetHashMap.put(setLayout.getX() , set);//real index will change everytime a preceding set is drawn when covered
//        cardSetOrderedList.add(new Object[]{setLayout.getX() , set});
//System.out.println("@CardContainerWrapper.displayCardSet():: added cardSetOrderedList entry " + setLayout.getX() + ":" + set.getContentString());
    }



    public float[] getSetsXs(){
        float[] x = new float[cardSets.size()];
        int newX = 8;

        for(int i = 0 ; i < cardSets.size() ; i++  ){
            x[i] = newX;

            CardSet set = cardSets.get(i);
            int increment = set.getNonNullMembers().size() * CardLayout.CARD_WIDTH;
            if(i<cardSets.size()-1) increment = increment + 15;

            newX = newX + increment;
        }

        return x;
    }




    public float[] getSetsYs(){
        float[] y = new float[cardSets.size()];
        int newY = 8;

        for(int i = 0 ; i < cardSets.size() ; i++  ){
            y[i] = newY;

            CardSet set = cardSets.get(i);
            int increment = set.getNonNullMembers().size() * CardLayout.CARD_WIDTH;
            if(i<cardSets.size()-1) increment = increment + 15;

            newY = newY + increment;
        }

        return y;
    }


    public void addCard(Card card){
        cards.add(card);
        if(numCardsToShow==-1)  displayCard(card);
        else    layoutContainer();
    }

    public void insertCard(Card card , int targetPosition){
        cards.add(targetPosition , card);
        if(numCardsToShow==-1){
            displayCard(card , targetPosition);
        }
        else    layoutContainer();
    }

    //a,b,c,d,e
    //move d before b   3->1 abdce adbce
    //move b after d    1->3
    public int changeCardPosition(CardLayout cardLayout , int targetPosition){
        int sourcePosition = layout.indexOfChild(cardLayout);

        //backward
        if(sourcePosition>targetPosition){
System.out.println("changing " + cardLayout.card.getContentString() + " from p" + sourcePosition + " to p" + targetPosition  + " replacing " + cards.get(targetPosition).getContentString());

            for(int i=sourcePosition ; i > targetPosition ; i--){
                CardLayout clX = (CardLayout)layout.getChildAt(i-1);
                layout.removeView(clX);
                layout.addView(clX , i);
System.out.println("    swapped " + cards.get(i).getContentString() + " and " + cards.get(i-1).getContentString());
                Collections.swap(cards, i, i-1);
            }
        }

        //forward
        else if(sourcePosition<targetPosition) {
            targetPosition = targetPosition-1;
System.out.println("changing " + cardLayout.card.getContentString() + " from p" + sourcePosition + " to p" + targetPosition  + " replacing " + cards.get(targetPosition).getContentString() + " after decrementing targetPosition by 1");


            /*if (targetPosition == layout.getChildCount() - 1) {
                System.out.println("detecting drop at last position");
            }*/

            //else{
                for (int i = sourcePosition; i < targetPosition; i++) {
                    if(i==targetPosition-1 && targetPosition==layout.getChildCount()-1){
                        System.out.println("skipping swap of (" + i + ") " + cards.get(i).getContentString() + " and (" + (i+1) + ") " + cards.get(i+1).getContentString());
                        continue;//
                    }

                    CardLayout clX = (CardLayout) layout.getChildAt(i);
                    layout.removeView(clX);
                    layout.addView(clX, i + 1);

                    System.out.println("    swapped ("+i+")" + cards.get(i).getContentString() + " and ("+(i+1)+")" + cards.get(i + 1).getContentString());
                    Collections.swap(cards, i, i + 1);
                }
            //}
        }
        layout.invalidate();
        return targetPosition;
    }

    public void removeCard(CardLayout cardLayout){
        cards.remove(cardLayout.card);
        cardLayout.setVisibility(LinearLayout.INVISIBLE);
        layout.removeView(cardLayout);
        layout.invalidate();
        cardLayoutMap.remove(cardLayout.card);
    }


    public void removeAll(List<Card> cards){
        for(Iterator<Card> i = cards.iterator() ; i.hasNext() ; ){
            removeCard(cardLayoutMap.get(i.next()));
        }
    }

    public void removeCardRetainModel(CardLayout cardLayout){
        //todo move card to end of array
        for(int i = cards.indexOf(cardLayout.card) ; i < cards.size()-1 ; i++){
            Collections.swap(cards,i,i+1);
        }

        cardLayout.setVisibility(LinearLayout.INVISIBLE);
        layout.removeView(cardLayout);
        layout.invalidate();
    }


    public void removeAllRetainModel(List<Card> cards){
        for(Iterator<Card> i = cards.iterator() ; i.hasNext() ; ){
            removeCardRetainModel(cardLayoutMap.get(i.next()));
        }
    }

    public String toString(){
        return "CardContainerLayout: name:" + containerName;
    }



    public void highlightSet(CardSet set){
        for(Iterator<Card> i = set.iterator() ; i.hasNext() ; ){
            cardLayoutMap.get(i.next()).highlight();
        }
        layout.invalidate();
    }


    public void unhighlightSet(CardSet set){
        for(Iterator<Card> i = set.iterator() ; i.hasNext() ; ){
            cardLayoutMap.get(i.next()).unhighlight();
        }
        layout.invalidate();
    }




    @Override
    public boolean onDrag(View v, DragEvent event) {

        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                //no action necessary
                return true;
            case DragEvent.ACTION_DRAG_ENTERED:
                //no action necessary
                return true;
            case DragEvent.ACTION_DRAG_EXITED:
                //no action necessary
                return true;
            case DragEvent.ACTION_DROP: {
                CardLayout cardLayout = (CardLayout) event.getLocalState();
                String sourceName = cardLayout.source;

                System.out.println("Container.onDrag(): destination:" + containerName + " - source:" + sourceName);

                //destination: deck - accept card from bankBuffer, reject
                if (containerName.equals("deck")) {
                    if (sourceName.equals("reject")) {
                        float x = event.getX();
                        int p = Math.round(x / CardLayout.CARD_WIDTH);
                        gameActivity.reject2deck(cardLayout,p);
                        return true;
                    } else if (sourceName.equals("bank_buffer")) {
                        float x = event.getX();
                        int p = Math.round(x / CardLayout.CARD_WIDTH);
                        gameActivity.bank2deck(cardLayout,p);
                        return true;
                    }

                    //3 deck2deck
                    else if (sourceName.equals("deck")) {
                        float x = event.getX();
                        int p = Math.round(x / CardLayout.CARD_WIDTH);
                        gameActivity.deck2deck(cardLayout, p);
                        return true;
                    }

                }


                //destination: reject - accept card from deck
                else if (containerName.equals("reject")) {
                    if (sourceName.equals("deck")) {
                        gameActivity.deck2reject(cardLayout);
                        return true;
                    }
                    else if(sourceName.equals("bank_buffer")){
                        gameActivity.bankToReject(cardLayout);
                        return true;
                    }
                }

                //destination: user player floor - accept card from deck
                /*else if (containerName.equals("floor0")) {
                    if (sourceName.equals("deck")) {
                        gameActivity.deck2floor(cardLayout);
                        return true;
                    }
                }*/
                //all floors ... accept sets to floor0 and covering cards on all floors
                else if(containerName.contains("floor")){
                    if (sourceName.equals("deck")) {
                        float x = event.getX();
                        float y = event.getY();
                        return gameActivity.deck2floor(cardLayout,containerName,x,y);
                    }
                }

            }
            case DragEvent.ACTION_DRAG_ENDED:
                //no action necessary
                return true;
        }
        return false;
    }


/*
    public void debugOrder(){
        System.out.println("---------------- " + containerName + " ------------------ ");
        System.out.println("\t *********** cards order ***********");
        for(int i = 0 ;  i < cards.size() ; i++){
            System.out.println("\t" + i + ": " + cards.get(i).getContentString());
        }

        System.out.println("\t *********** layout order ***********");
        for(int i=0 ; i < layout.getChildCount() ; i++){
            System.out.println("\t" + i + ": " + ((CardLayout)layout.getChildAt(i)).card.getContentString());
        }
        System.out.println("------------------------------------------------------------------ ");
    }
*/


}//EO ContainerClass

