package com.github.destinyd.kcvideoview.samples;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

/**
 * Created by dd on 14-4-21.
 */
public class SimpleMain extends Activity {

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
            case R.id.btn_more_element:
                startActivity(new Intent(this, SampleMoreElement.class));
                break;
            case R.id.btn_more_element_hide_actionbar:
                startActivity(new Intent(this, SampleMoreElementHideActionbar.class));
                break;
        }
    }
}