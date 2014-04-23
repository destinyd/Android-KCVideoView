package com.github.destinyd.kcvideoview.samples;

import android.os.Bundle;
import com.github.destinyd.kcvideoview.widget.KCVideoView;
import com.github.kevinsawicki.http.HttpRequest;
import roboguice.activity.RoboActivity;
import roboguice.util.RoboAsyncTask;

import static com.github.kevinsawicki.http.HttpRequest.get;

public class SampleSingleVideo extends RoboActivity {

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

        getJson("http://www.mocky.io/v2/535624c1196824b710c6b9ca");
    }

    private void getJson(String url) {
        mUrl = url;
        new RoboAsyncTask<Boolean>(this) {
            public Boolean call() throws Exception {
                HttpRequest request = get(mUrl);
                mJsonString = request.body();

                return true;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(Boolean relationship) {
                kcvv.load(mJsonString); // part 1
            }

        }.execute();
    }
}

