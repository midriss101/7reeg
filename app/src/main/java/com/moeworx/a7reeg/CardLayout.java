package com.moeworx.a7reeg;


import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


class CardLayout extends LinearLayout {

    static int CARD_WIDTH = 60;

    Card card;
    String source;
    int rotation;

    GameActivity gameActivity;

    int stdBgColor = Color.WHITE;
    int hltBgColor = Color.LTGRAY;

    /**
    Must call layoutCard() before using
     */
    CardLayout(GameActivity gameActivity , Card card,String source, int rotation){
        super(gameActivity);
        this.card = card;
        this.source = source;
        this.rotation = rotation;
        this.gameActivity = gameActivity;
    }

    void layoutCard() {
        if(rotation==0 || rotation==180) {
            setOrientation(LinearLayout.VERTICAL);
        }
        else if(rotation==90 || rotation==270){
            setOrientation(LinearLayout.HORIZONTAL);
        }

        LinearLayout.LayoutParams layoutParams = null;

        if(rotation==0 || rotation==180) {
            layoutParams = new LinearLayout.LayoutParams(CARD_WIDTH, LinearLayout.LayoutParams.MATCH_PARENT);
        }
        else if(rotation==90 || rotation==270){
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT , CARD_WIDTH);
        }

        layoutParams.weight = 0;
        setLayoutParams(layoutParams);

        Drawable myDrawable = null;
        if (card.getTypeString().equalsIgnoreCase("club") && rotation==90) {
            myDrawable = getResources().getDrawable(R.drawable.club1);
        }
        else if (card.getTypeString().equalsIgnoreCase("club") && rotation==270) {
            myDrawable = getResources().getDrawable(R.drawable.club3);
        }
        else if (card.getTypeString().equalsIgnoreCase("club")) {
            myDrawable = getResources().getDrawable(R.drawable.club);
        }
        else if (card.getTypeString().equalsIgnoreCase("diamond") && (rotation==90 || rotation==270) ) {
            myDrawable = getResources().getDrawable(R.drawable.diamond_land);
        }
        else if (card.getTypeString().equalsIgnoreCase("diamond")) {
            myDrawable = getResources().getDrawable(R.drawable.diamond);
        }
        else if (card.getTypeString().equalsIgnoreCase("heart") && rotation==90) {
            myDrawable = getResources().getDrawable(R.drawable.heart1);
        }
        else if (card.getTypeString().equalsIgnoreCase("heart") && rotation==270) {
            myDrawable = getResources().getDrawable(R.drawable.heart3);
        }
        else if (card.getTypeString().equalsIgnoreCase("heart")) {
            myDrawable = getResources().getDrawable(R.drawable.heart);
        }
        else if (card.getTypeString().equalsIgnoreCase("spade") && rotation==90) {
            myDrawable = getResources().getDrawable(R.drawable.spade1);
        }
        else if (card.getTypeString().equalsIgnoreCase("spade") && rotation==270) {
            myDrawable = getResources().getDrawable(R.drawable.spade3);
        }
        else if (card.getTypeString().equalsIgnoreCase("spade")) {
            myDrawable = getResources().getDrawable(R.drawable.spade);
        }


        TextView tV = new TextView(gameActivity);
        if(rotation==90 || rotation==270)   tV.setRotation(rotation);
        tV.setText(card.getDenominationString());

        if(rotation==0 || rotation==180) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        }
        else if(rotation==90 || rotation==270){
            layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        }

        layoutParams.weight = 1;
        tV.setLayoutParams(layoutParams);
        addView(tV);

        ImageView iV = new ImageView(gameActivity);
        iV.setImageDrawable(myDrawable);

        if(rotation==0 || rotation==180) {
            layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
        }
        else if(rotation==90 || rotation==270){
            layoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT);
        }

        layoutParams.weight = 1;
        iV.setLayoutParams(layoutParams);
        addView(iV);


        GradientDrawable border = new GradientDrawable();
        border.setColor(stdBgColor); //white background
        border.setStroke(1, Color.BLACK); //black border with full opacity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(border);
        } else {
            setBackground(border);
        }

        //add drag listener so card can be dragged to reject
        setOnLongClickListener(new View.OnLongClickListener() {
            // Defines the one method for the interface, which is called when the View is long-clicked
            public boolean onLongClick(View v) {
                dragCard(v);
                return true;
            }
        });
    }

    public void highlight(){
        GradientDrawable border = new GradientDrawable();
        border.setColor(hltBgColor); //white background
        border.setStroke(1, Color.BLACK); //black border with full opacity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(border);
        } else {
            setBackground(border);
        }
    }

    public void unhighlight(){
        GradientDrawable border = new GradientDrawable();
        border.setColor(stdBgColor); //white background
        border.setStroke(1, Color.BLACK); //black border with full opacity
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            setBackgroundDrawable(border);
        } else {
            setBackground(border);
        }
    }



    private void dragCard(View view){
        System.out.println("starting drag of " + card.getContentString() + " from " + source);
        ClipData data = ClipData.newPlainText("", "");
        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);

        view.startDragAndDrop(data,  // the data to be dragged
                shadowBuilder,  // the drag shadow builder
                this,      // as local data
                0          // flags
        );
    }

    @Override
    public String toString(){
        return "CardLayout for " + card.getContentString();
    }
}//EO Class CardLayout


