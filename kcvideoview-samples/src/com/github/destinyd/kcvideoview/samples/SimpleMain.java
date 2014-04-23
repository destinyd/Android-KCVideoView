package com.github.destinyd.kcvideoview.samples;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import roboguice.activity.RoboActivity;

/**
 * Created by dd on 14-4-21.
 */
public class SimpleMain extends RoboActivity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_main);
    }

    public void onClick(View view){
        switch (view.getId()){
            case R.id.btn_single_video:
                startActivity(new Intent(this, SampleSingleVideo.class));
                break;
            case R.id.btn_multiple_videos:
                startActivity(new Intent(this, SampleMultipleVideos.class));
                break;
            case R.id.btn_set_cover:
                startActivity(new Intent(this, SampleSetCover.class));
                break;
            case R.id.btn_without_set_cover:
                startActivity(new Intent(this, SampleWithoutSetCover.class));
                break;
            case R.id.btn_with_something_other:
                startActivity(new Intent(this, SampleWithSomethingOther.class));
                break;
        }
    }
}