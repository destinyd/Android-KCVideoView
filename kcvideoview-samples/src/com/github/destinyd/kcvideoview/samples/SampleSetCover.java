package com.github.destinyd.kcvideoview.samples;

import android.os.Bundle;
import com.github.destinyd.kcvideoview.widget.KCVideoView;
import roboguice.activity.RoboActivity;

import static com.github.kevinsawicki.http.HttpRequest.get;

public class SampleSetCover extends RoboActivity {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    //views
    KCVideoView kcvv;

    String mUrl;
    String mJsonString;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_kcvideoview);
        kcvv = (KCVideoView) findViewById(R.id.kcvv);

        kcvv.set_cover("http://ww4.sinaimg.cn/large/996e0d92jw1efebaefht7j20ac0c1751.jpg");
    }
}

