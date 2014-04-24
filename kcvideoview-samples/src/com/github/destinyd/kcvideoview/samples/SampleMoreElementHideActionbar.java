package com.github.destinyd.kcvideoview.samples;

import android.os.Bundle;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import com.actionbarsherlock.view.ActionMode;
import com.github.destinyd.kcvideoview.widget.KCVideoView;
import com.github.kevinsawicki.http.HttpRequest;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import roboguice.inject.InjectView;
import roboguice.util.RoboAsyncTask;

import java.util.ArrayList;
import java.util.HashMap;

import static com.github.kevinsawicki.http.HttpRequest.get;

public class SampleMoreElementHideActionbar extends RoboSherlockActivity implements KCVideoView.ITimePointListener {

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
    ActionMode mMode;

    @InjectView(R.id.myTabHost)
    TabHost tabHost;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sample_with_something_other);

        setTabs();

        kcvv = (KCVideoView) findViewById(R.id.kcvv);
        getJson("http://www.mocky.io/v2/53562554196824bf10c6b9cb");
        kcvv.add_time_point_listener(10000, this);
    }

    private void setTabs() {
        tabHost.setBackgroundResource(android.R.color.background_light);

        // 如果不是继承TabActivity，则必须在得到tabHost之后，添加标签之前调用tabHost.setup()
        tabHost.setup();

        // 这里content的设置采用了布局文件中的view
        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("tab1 s").setContent(R.id.view1));
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("tab2")
                .setContent(R.id.view2));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("tab3")
                .setContent(R.id.view3));

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        for (int i = 1; i <= 10; i++) {
            HashMap<String, String> map = new HashMap<String, String>();
            map.put("name", "listitem " + String.valueOf(i));
            list.add(map);
        }
        ListView listView = (ListView) findViewById(R.id.view1);
        BaseAdapter baseAdapter = new SimpleAdapter(this, list, android.R.layout.simple_list_item_1, new String[]{"name"}, new int[]{android.R.id.text1});
        listView.setAdapter(baseAdapter);
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
                kcvv.load(mJsonString);
            }

        }.execute();
    }

    @Override
    public void call(KCVideoView view) {
        Log.e("SampleMultipleVideos", "10s call!!!!!!!");
    }
}

