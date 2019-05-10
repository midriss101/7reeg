package com.moeworx.a7reeg;


import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerLayoutWrapper {

    GameActivity gameActivity;
    LinearLayout layout;
    RoundPlayer player;
    Drawable avatar;
    int orientation;
    int colorCode;

    PlayerLayoutWrapper(GameActivity gameActivity, LinearLayout layout, RoundPlayer player, int orientation, int colorCode, Drawable avatar) {
        this.gameActivity = gameActivity;
        this.avatar = avatar;
        this.layout = layout;
        this.player = player;
        this.orientation = orientation;
        this.colorCode = colorCode;
    }



    ImageView imageView = null;
    TextView textView = null;
    LinearLayout rejectCardBuffer;

    public void init(){
        layout.setOrientation(orientation);

        imageView = new ImageView(gameActivity);
        textView = new TextView(gameActivity);
        rejectCardBuffer = new LinearLayout(gameActivity);

        if(orientation==LinearLayout.VERTICAL) {
            layout.addView(textView);
            layout.addView(imageView);
            layout.addView(rejectCardBuffer);
        }
        else if(orientation==LinearLayout.HORIZONTAL){
            layout.addView(rejectCardBuffer);
            layout.addView(imageView);
            layout.addView(textView);
        }

        imageView.setImageDrawable(avatar);
        textView.setText(player.getStatusText());

        if(orientation==LinearLayout.VERTICAL){
            textView.setTextAlignment(layout.TEXT_ALIGNMENT_CENTER);
        }
        else if(orientation==LinearLayout.HORIZONTAL){
            textView.setTextAlignment(layout.TEXT_ALIGNMENT_VIEW_START);
        }

        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.WHITE); //white background
        border.setStroke(1, colorCode);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
            layout.setBackgroundDrawable(border);
        } else {
            layout.setBackground(border);
        }
    }


    CardContainerLayoutWrapper floor;
    public void setFloor(CardContainerLayoutWrapper floor) {
        this.floor = floor;
    }
    public CardContainerLayoutWrapper getFloor() {
        return floor;
    }



}

