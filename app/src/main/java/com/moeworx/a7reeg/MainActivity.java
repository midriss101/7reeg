package com.moeworx.a7reeg;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_USER = "com.moeworx.a7reeg.MainActivity.USER";

    String userName = "User Player";//TODO change

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

    }


    public void newGame(View view){
        //handover to GameActivity
        Intent intent = new Intent(this , GameActivity.class);
        intent.putExtra(EXTRA_USER , userName);
        startActivity(intent);
    }


}
