package com.six.ui_anim.sample;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.six.ui_anim.R;
import com.six.ui_anim.progressWheel.ProgressWheel;

/**
 * @author hzxuxiaolin
 * @date 2015/9/1
 * Copyright 2015 six.com. All rights reserved.
 */

public class ProgressWheelActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private Button btnFinish;
    private ProgressWheel mProgressWheel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_prog_wheel);
        init();
    }

    private void init(){
        mToolbar = (Toolbar) findViewById(R.id.tl_header);
        mToolbar.setNavigationIcon(R.drawable.ic_launcher);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setTitle("ProgressWheel Demo");
        setSupportActionBar(mToolbar);

        mProgressWheel = (ProgressWheel) findViewById(R.id.progress);

        btnFinish = (Button) findViewById(R.id.btn_finish);
        btnFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressWheel.setLoadingFinishedTag(true);
            }
        });
    }
}