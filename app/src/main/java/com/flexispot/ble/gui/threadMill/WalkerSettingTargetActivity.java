package com.flexispot.ble.gui.threadMill;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.flexispot.ble.R;
import com.flexispot.ble.gui.view.ActionSheetDialog;
import com.flexispot.ble.gui.view.StatusBarUtil;

import java.util.ArrayList;
import java.util.List;

public class WalkerSettingTargetActivity extends AppCompatActivity {

    private Toolbar toolbar;
    PreferencesUtility preferencesUtility;



    private RelativeLayout rel_m1;//档速m1
    private RelativeLayout rel_m2;//档速m2
    private RelativeLayout rel_m3;//档速m3
    private TextView text_m1;//显示m1速度
    private TextView text_m2;//显示m2速度
    private TextView text_m3;//显示m3速度
    private int system=0;//0公制1英制


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_taread);
        findId();
        click();
        StatusBarUtil.setStatusBarMode(this, true, R.color.bg_color);

        preferencesUtility = PreferencesUtility.getInstance(WalkerSettingTargetActivity.this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
            }
        });
        system=  preferencesUtility.getSystem();

        int a = preferencesUtility.getM1();
        if (a >= 0) {
            if (system==1){
                text_m1.setText(itemYin.get(a) + "mph");
            }else {
                text_m1.setText(item.get(a) + "km/h");
            }
        }
        int b = preferencesUtility.getM2();
        if (b >= 0) {
            if (system==1){
                text_m2.setText(itemYin.get(b)  + "mph");
            }else {
                text_m2.setText(item.get(b) + "km/h");
            }

        }
        int c = preferencesUtility.getM3();
        if (c >= 0) {
            if (system==1){
                text_m3.setText(itemYin.get(c)  + "mph");
            }else {
                text_m3.setText(item.get(c) + "km/h");
            }

        }

    }



    private void findId() {


        rel_m1 = findViewById(R.id.rel_m1);
        rel_m2 = findViewById(R.id.rel_m2);
        rel_m3 = findViewById(R.id.rel_m3);
        text_m1 = findViewById(R.id.text_m1);
        text_m2 = findViewById(R.id.text_m2);
        text_m3 = findViewById(R.id.text_m3);
    }



    private void click() {
        rel_m1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (system==1){
                    TanYing(1);
                }else {
                    Tan(1);
                }
            }
        });
        rel_m2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (system==1){
                    TanYing(2);
                }else {
                    Tan(2);
                }
            }
        });
        rel_m3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (system==1){
                    TanYing(3);
                }else {
                    Tan(3);
                }
            }
        });

        item = new ArrayList<>();
        item.add(1.0);
        item.add(1.5);
        item.add(2.0);
        item.add(2.5);
        item.add(3.0);
        item.add(3.5);
        item.add(4.0);
        item.add(4.5);
        item.add(5.0);
        item.add(5.5);
        item.add(6.0);

        itemYin=new ArrayList<>();
        itemYin.add(0.6);
        itemYin.add(0.9);
        itemYin.add(1.2);
        itemYin.add(1.5);
        itemYin.add(1.8);
        itemYin.add(2.1);
        itemYin.add(2.4);
        itemYin.add(2.7);
        itemYin.add(3.1);
        itemYin.add(3.4);
        itemYin.add(3.7);
    }

    List<Double> item;
    List<Double> itemYin;
    private void Tan(final int i) {
        new ActionSheetDialog(WalkerSettingTargetActivity.this)
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheet(item, ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        int a = which - 1;
                        if (a >= 0) {
                            double speed = item.get(a);
                            if (i == 1) {
                                text_m1.setText(speed + "km/h");
                                preferencesUtility.setM1(a);
                            } else if (i == 2) {
                                text_m2.setText(speed + "km/h");
                                preferencesUtility.setM2(a);
                            } else {
                                text_m3.setText(speed + "km/h");
                                preferencesUtility.setM3(a);
                            }
                        }

                    }
                },0).show();

    }



    private void TanYing(final int i) {
        new ActionSheetDialog(WalkerSettingTargetActivity.this)
                .builder()
                .setCancelable(false)
                .setCanceledOnTouchOutside(false)
                .addSheet(itemYin, ActionSheetDialog.SheetItemColor.Blue, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int which) {
                        int a = which - 1;
                        if (a >= 0) {
                            double speed = itemYin.get(a);
                            if (i == 1) {
                                text_m1.setText(speed + "mph");
                                preferencesUtility.setM1(a);
                            } else if (i == 2) {
                                text_m2.setText(speed + "mph");
                                preferencesUtility.setM2(a);
                            } else {
                                text_m3.setText(speed + "mph");
                                preferencesUtility.setM3(a);
                            }
                        }

                    }
                },1).show();

    }



}
