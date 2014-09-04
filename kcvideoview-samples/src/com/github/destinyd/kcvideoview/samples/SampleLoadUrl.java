package com.github.destinyd.kcvideoview.samples;

import android.os.Bundle;
import com.github.destinyd.kcvideoview.widget.KCVideoView;
import com.github.kevinsawicki.http.HttpRequest;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import static com.github.kevinsawicki.http.HttpRequest.get;

public class SampleLoadUrl extends RoboActivity {
    @InjectView(R.id.kcvv)
    KCVideoView kcvv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_kcvideoview);
        kcvv = (KCVideoView) findViewById(R.id.kcvv);
        kcvv.load_url("http://42.120.41.92/video/jihuang.mp4");
    }
}

