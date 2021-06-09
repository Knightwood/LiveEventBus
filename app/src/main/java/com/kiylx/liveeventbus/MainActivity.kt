package com.kiylx.liveeventbus;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kiylx.bus.eventbus.LiveEventBus;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
   LiveEventBus.<String>with("one").post("ooo");
   LiveEventBus.with("one").post(new Tr());
    }
}
