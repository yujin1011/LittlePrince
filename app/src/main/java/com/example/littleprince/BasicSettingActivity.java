package com.example.littleprince;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class BasicSettingActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_man, btn_woman, btn_light, btn_dark, btn_save;
    boolean man, woman, light, dark;

    String colorB = "#000000", colorW = "#ffffff";
    public static final String PREFERENCES_NAME = "rebuild_preference";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_setting);
        SharedPreferences prefs = getPreferences(this);

        if (prefs.getString("isSuccess", "").equals("SUCCESS")) {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }

        btn_man = findViewById(R.id.btn_man);
        btn_woman = findViewById(R.id.btn_woman);
        btn_light = findViewById(R.id.btn_Light);
        btn_dark = findViewById(R.id.btn_dark);
        btn_save = findViewById(R.id.btn_save);

        btn_man.setOnClickListener(this);
        btn_woman.setOnClickListener(this);
        btn_light.setOnClickListener(this);
        btn_dark.setOnClickListener(this);
        btn_save.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        SharedPreferences prefs = getPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        if (v == btn_man) {
            btn_man.setBackgroundResource(R.drawable.btn_select);
            btn_woman.setBackgroundResource(R.drawable.btn_noselect);
            btn_man.setTextColor(Color.parseColor(colorW));
            btn_woman.setTextColor(Color.parseColor(colorB));
            man = true;
            woman = false;
            editor.putBoolean("man", man);
            editor.putBoolean("woman", woman);
            editor.apply();
        }
        if (v == btn_woman) {
            btn_man.setBackgroundResource(R.drawable.btn_noselect);
            btn_woman.setBackgroundResource(R.drawable.btn_select);
            btn_man.setTextColor(Color.parseColor(colorB));
            btn_woman.setTextColor(Color.parseColor(colorW));
            man = false;
            woman = true;
            editor.putBoolean("man", man);
            editor.putBoolean("woman", woman);
            editor.apply();
        }
        if (v == btn_light) {
            btn_light.setBackgroundResource(R.drawable.btn_select);
            btn_dark.setBackgroundResource(R.drawable.btn_noselect);
            btn_light.setTextColor(Color.parseColor(colorW));
            btn_dark.setTextColor(Color.parseColor(colorB));
            light = true;
            dark = false;
            setTheme(R.style.LightTheme);
            editor.putBoolean("bright", light);
            editor.putBoolean("dark", dark);
            editor.apply();
        }
        if (v == btn_dark) {
            btn_light.setBackgroundResource(R.drawable.btn_noselect);
            btn_dark.setBackgroundResource(R.drawable.btn_select);
            btn_light.setTextColor(Color.parseColor(colorB));
            btn_dark.setTextColor(Color.parseColor(colorW));
            light = false;
            dark = true;
            setTheme(R.style.DarkTheme);
            editor.putBoolean("bright", light);
            editor.putBoolean("dark", dark);
            editor.apply();
        }

        if (v == btn_save) {
            if (man == false && woman == false) {
                if (light == false && dark == false) {
                    Toast.makeText(this, "테마를 선택해주세요.", Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(this, "성별을 선택해주세요", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
    }
}
