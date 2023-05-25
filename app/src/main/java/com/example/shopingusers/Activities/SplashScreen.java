package com.example.shopingusers.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.example.shopingusers.R;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        SharedPreferences sharedPreferences = getSharedPreferences("ShoppingRef",MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId","");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(userId!="")
                {
                    startActivity(new Intent(SplashScreen.this,MainActivity.class));
                    finish();
                }
                else
                {
                    startActivity(new Intent(SplashScreen.this,Login.class));
                    finish();
                }
            }
        },2000);
    }
}