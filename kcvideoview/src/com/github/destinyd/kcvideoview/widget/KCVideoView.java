package com.github.destinyd.kcvideoview.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.widget.*;
import com.actionbarsherlock.app.SherlockActivity;
import com.github.destinyd.kcvideoview.R;
import com.github.destinyd.kcvideoview.core.Utilities;
import com.github.destinyd.kcvideoview.error.UnsupportedLayoutError;
import com.github.destinyd.kcvideoview.model.PlayListObj;
import com.github.kevinsawicki.http.HttpRequest;
import roboguice.util.RoboAsyncTask;

import java.util.*;

import static com.github.kevinsawicki.http.HttpRequest.get;

public class KCVideoView extends RelativeLayout implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     * previously being shut down then this Bundle contains the data it most
     * recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */

    //views
    KCInternalVideoView kc_vv;
    ImageView iv_cover;
    SeekBar seekBar;
    TextView tv_current_position;
    ImageButton ib_big, ib_play, ib_volume, ib_fullscreen, ib_pause;
    RelativeLayout rl_controllers_panel, rl_volume_panel, rl_title_panel, rl_main;
    TextView tv_message, tv_title, tv_buffing;
    VerticalSeekBar vsb_volume;
    //
    List<View> views;

    //    int mStopPosition = 0;
    int handlePostDelay = 500;
    int requestedOrientation;

    boolean isFullscreen = false;
    boolean isControllersShow = true;
    boolean isPlaying = false;
    private Utilities utils;
    String mCoverUrl;
    Drawable mCover;
    ViewGroup.LayoutParams mLayoutParams = null;
    SherlockActivity sherlockActivity = null;
    Activity activity = null;
    Date dateLastClick = null;
    int old_duration = 0;


    private final Handler handlerSeekProgress = new Handler();
    private final Runnable rSeekProgress = new Runnable() {
        @Override
        public void run() {
            updateViews();
        }
    };

    public KCVideoView(Context context) {
        this(context, null);
    }

    public KCVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public KCVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.kc_videoview, this, true);

        resetDateLastClick();

        views = new ArrayList<View>();

        kc_vv = (KCInternalVideoView) findViewById(R.id.kc_vv);
        iv_cover = (ImageView) findViewById(R.id.iv_cover);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        tv_current_position = (TextView) findViewById(R.id.tv_current_position);
        tv_title = (TextView) findViewById(R.id.tv_title);
        tv_buffing = (TextView) findViewById(R.id.tv_buffing);

        ib_big = (ImageButton) findViewById(R.id.ib_big);
        ib_play = (ImageButton) findViewById(R.id.ib_play);
        ib_pause = (ImageButton) findViewById(R.id.ib_pause);
        ib_volume = (ImageButton) findViewById(R.id.ib_volume);
        ib_fullscreen = (ImageButton) findViewById(R.id.ib_fullscreen);

        vsb_volume = (VerticalSeekBar) findViewById(R.id.vsb_volume);

        rl_main = (RelativeLayout) findViewById(R.id.rl_main);
        rl_controllers_panel = (RelativeLayout) findViewById(R.id.rl_controllers_panel);
        rl_volume_panel = (RelativeLayout) findViewById(R.id.rl_volume_panel);
        rl_title_panel = (RelativeLayout) findViewById(R.id.rl_title_panel);
        tv_message = (TextView) findViewById(R.id.tv_message);

        views.add(rl_controllers_panel);
        views.add(rl_title_panel);
        views.add(ib_big);

        kc_vv.setOnCompletionListener(this);
        kc_vv.setOnErrorListener(this);


        ib_big.setOnClickListener(onClick);
        ib_play.setOnClickListener(onClick);
        ib_pause.setOnClickListener(onClick);
        ib_volume.setOnClickListener(onClick);
        ib_fullscreen.setOnClickListener(onClick);
        kc_vv.setOnClickListener(onClick);
        rl_main.setOnClickListener(onClick);

        utils = new Utilities();

//        seekBar.setMax(100);
        seekBar.setOnSeekBarChangeListener(current_position_change);
        vsb_volume.setOnSeekBarChangeListener(volume_change);

        listITimePointListener = new ArrayList<CTimePoint>();

        activity = (Activity) getContext();

        try {
            sherlockActivity = (SherlockActivity) getContext();
        } catch (Exception ex) {
            Log.d("KCVideoView", "context is not SherlockActivity");
        }
        requestedOrientation = getRequestedOrientation();

        init_volume();
    }

    private void resetDateLastClick() {
        dateLastClick = Calendar.getInstance().getTime();
    }

    private void init_volume() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        vsb_volume.setProgress(current);
        vsb_volume.setMax(max);
        ib_volume.setImageResource(current > 0 ? R.drawable.icon_volume : R.drawable.icon_volume_disabled);
        toggle_rl_volume_to(isFullscreen);
    }

    private void toggle_rl_volume_to(boolean is_fullscreen) {
        rl_volume_panel.setVisibility(is_fullscreen ? VISIBLE : GONE);
    }

    private void updateViews() {
        watchProgress();
        watchControllers();
        watchBuffing();
        handlerSeekProgress.postDelayed(rSeekProgress, handlePostDelay);
    }

    private void watchBuffing() {
        int duration = kc_vv.getCurrentPosition();
        if (old_duration == duration && isPlaying) {
                change_buffing_layout_params();
                tv_buffing.setVisibility(View.VISIBLE);
        } else {
            tv_buffing.setVisibility(View.GONE);
            old_duration = duration;
        }
    }

    private void watchProgress() {
        if (isPlaying) {
            int totalDuration = kc_vv.getDuration();
            int currentPosition = kc_vv.getCurrentPosition();
            set_current_position(currentPosition);
            set_progress(totalDuration, currentPosition);
            set_buffer_progress(totalDuration);

            call_i_time_point_listener(currentPosition, handlePostDelay);
        }
    }


    private void watchControllers() {
        if (isPlaying) {
            if (isControllersShow) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.SECOND, -5);
                if (dateLastClick.before(cal.getTime())) {
                    set_controllers_visiable(false);
                }
            }
        } else {
            if (!isControllersShow)
                set_controllers_visiable(true);
        }
    }

    private void change_buffing_layout_params() {
        LayoutParams buffingParams = (LayoutParams) tv_buffing.getLayoutParams();
        buffingParams.addRule(RelativeLayout.ABOVE, isControllersShow ? R.id.rl_controllers_panel : 0);
        buffingParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, isControllersShow ? 0 : RelativeLayout.TRUE);
        tv_buffing.setLayoutParams(buffingParams);
    }

    private void call_i_time_point_listener(int currentPosition, int handlePostDelay) {
        Iterator<CTimePoint> iterator = listITimePointListener.iterator();
        while (iterator.hasNext()) {
            CTimePoint cTimePoint = iterator.next();
            if (
                    cTimePoint.getTime_point() >= currentPosition - handlePostDelay / 2
                            &&
                            cTimePoint.getTime_point() < currentPosition + handlePostDelay / 2
                    )
                cTimePoint.getL().call(this);
        }

    }

    private void set_buffer_progress(int totalDuration) {
        int bufferPercentage = kc_vv.getBufferPercentage();
        PlayListObj prev = kc_vv.getPrevPlayListObj();
        int prevCount = prev != null ? prev.getSecondsCount() * 1000 : 0;
        PlayListObj current = kc_vv.getCurrentPlayListObj();
        int currentBufferCount = current != null ? (int) (current.getSeconds() * 1000 * bufferPercentage / 100.0) : 0;
        int bufferProgress = prevCount + currentBufferCount;

        int progressBuffer = utils.getProgressPercentage(bufferProgress,
                totalDuration);

        seekBar.setSecondaryProgress(progressBuffer);
    }

    private void set_progress(int totalDuration, int currentDuration) {
        int progress = utils.getProgressPercentage(currentDuration,
                totalDuration);
        seekBar.setProgress(progress);
    }

    private void set_current_position(long currentDuration) {
        tv_current_position.setText("" + utils.milliSecondsToTimer(currentDuration));
    }

    public OnClickListener onClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            resetDateLastClick();
            int id = view.getId();
            if (id == R.id.ib_big) {
                if (isPlaying)
                    pause();
                else
                    play();
            } else if (id == R.id.ib_play) {
                play();
            } else if (id == R.id.ib_pause) {
                pause();
            } else if (id == R.id.ib_fullscreen) {
                isFullscreen = !isFullscreen;//改变全屏/窗口的标记
                set_fullwindow();
            } else if (id == R.id.ib_volume) {
                toggle_volume();
            } else if (id == R.id.rl_main) {
                set_controllers_visiable(true);
            }
        }
    };

    private void set_fullwindow() {
        if (this.mLayoutParams == null)
            this.mLayoutParams = //(RelativeLayout.LayoutParams)  //对于父级的LayoutParams
                    getLayoutParams();
        if (isFullscreen) {//设置RelativeLayout的全屏模式
            if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);//横屏
            }
            hideStatusBar();
            set_fullscreen_layout_params();
            hide_parent_other_views();
            change_fullscreen_view_to(isFullscreen);

        } else {//设置RelativeLayout的窗口模式
            activity.setRequestedOrientation(requestedOrientation); // 恢复
            showStatusBar();
            setLayoutParams(mLayoutParams);
            resume_parent_other_views();
            change_fullscreen_view_to(isFullscreen);
        }
        toggle_rl_volume_to(isFullscreen);
    }

    private void change_fullscreen_view_to(boolean is_fullscreen) {
        LayoutParams seekbarParams = (LayoutParams) seekBar.getLayoutParams();
        seekbarParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, is_fullscreen ? 0 : RelativeLayout.TRUE);
        seekbarParams.addRule(RelativeLayout.LEFT_OF, is_fullscreen ? R.id.tv_current_position : R.id.ib_fullscreen);
        seekBar.setLayoutParams(seekbarParams);

        if (is_fullscreen) {
            tv_current_position.setVisibility(VISIBLE);
            if (isPlaying) {
                ib_play.setVisibility(INVISIBLE);
                ib_pause.setVisibility(VISIBLE);
            } else {
                ib_play.setVisibility(VISIBLE);
                ib_pause.setVisibility(INVISIBLE);
            }
        } else {
            ib_play.setVisibility(GONE);
            ib_pause.setVisibility(GONE);
            tv_current_position.setVisibility(GONE);
        }
    }

    private void resume_parent_other_views() {
        ViewGroup viewGroup = ((ViewGroup) getParent());
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view != this)
                view.setVisibility(VISIBLE);
        }
    }

    private void set_fullscreen_layout_params() {
        ViewGroup.LayoutParams viewGrouplayoutParams = getLayoutParams();
        String className = viewGrouplayoutParams.getClass().getName();
        if ("android.widget.RelativeLayout$LayoutParams".equals(className)) {
            LayoutParams layoutParams =
                    new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
            setLayoutParams(layoutParams);
        } else if ("android.widget.LinearLayout$LayoutParams".equals(className)) {
            LinearLayout.LayoutParams layoutParams =
                    new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, LinearLayout.LayoutParams.FILL_PARENT);
            setLayoutParams(layoutParams);
        } else if ("android.widget.FrameLayout$LayoutParams".equals(className)) {
            FrameLayout.LayoutParams layoutParams =
                    new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
            setLayoutParams(layoutParams);
        } else {
//                ViewGroup.LayoutParams layoutParams =
//                        new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
//                setLayoutParams(layoutParams);
            throw new UnsupportedLayoutError(viewGrouplayoutParams.getClass().getName());
        }
    }

    private void hide_parent_other_views() {
        ViewParent viewParent = getParent();
//            Class<?> c = viewParent.getClass();
        ViewGroup viewGroup = ((ViewGroup) viewParent);
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View view = viewGroup.getChildAt(i);
            if (view != this)
                view.setVisibility(INVISIBLE);
        }
    }

    private void toggle_volume() {
        AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int current = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (current <= 0) {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, max, AudioManager.MODE_NORMAL);
            ib_volume.setImageResource(R.drawable.icon_volume);
            vsb_volume.setProgress(max);
        } else {
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.MODE_NORMAL);
            ib_volume.setImageResource(R.drawable.icon_volume_disabled);
            vsb_volume.setProgress(0);
        }

    }

    public void play() {
        change_view_to_play();
        isPlaying = true;
        kc_vv.start();

        updateViews();
//        set_controllers_visiable(false);
    }

    private void change_view_to_play() {
        iv_cover.setVisibility(View.GONE);
        tv_message.setVisibility(View.GONE);
        if (isFullscreen) {
            ib_play.setVisibility(INVISIBLE);
            ib_pause.setVisibility(VISIBLE);
        } else {
            ib_play.setVisibility(GONE);
            ib_pause.setVisibility(GONE);
        }
        ib_big.setImageResource(R.drawable.big_pause);
//        ib_big.setVisibility(GONE);
    }

    public void pause() {
//        if (kc_vv.canPause()) {
        if (isPlaying) {
            change_view_to_pause();
            kc_vv.pause();
            isPlaying = false;
        }
    }

    private void change_view_to_pause() {
        if (isFullscreen) {
            ib_play.setVisibility(VISIBLE);
            ib_pause.setVisibility(INVISIBLE);
        } else {
            ib_play.setVisibility(GONE);
            ib_pause.setVisibility(GONE);
        }
//            mStopPosition = kc_vv.getCurrentPosition();
        ib_big.setImageResource(R.drawable.big_play);
        set_controllers_visiable(true);
    }

    public void set_cover(String img_url) {
        getCovers(img_url);
    }

    public void set_cover(Drawable img_drawable) {
        iv_cover.setImageDrawable(img_drawable);
    }

    public void load(String json_string) {
        kc_vv.load(json_string);
    }

    public void load_url(String url) {
        kc_vv.setVideoURI(Uri.parse(url));
    }

    private SeekBar.OnSeekBarChangeListener current_position_change = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // 当进度条停止修改的时候触发
            // 取得当前进度条的刻度
            int progress = seekBar.getProgress();
            int totalDuration = kc_vv.getDuration();
            int p_progress = utils.progressToTimer(progress,
                    totalDuration);
            set_current_position(p_progress);
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            if (fromUser) {
                int totalDuration = kc_vv.getDuration();
                int p_progress = utils.progressToTimer(progress,
                        totalDuration);
//                kc_vv.pause();
                set_current_position(p_progress);
                kc_vv.seekTo(p_progress);
            }
        }
    };

    private void set_controllers_visiable(boolean b) {
        isControllersShow = b;
        int v = b ? VISIBLE : GONE;
        Iterator<View> it = views.iterator();
        while (it.hasNext()) {
            View view = it.next();
            view.setVisibility(v);
        }
        if (isFullscreen) {
            rl_volume_panel.setVisibility(v);
        }
    }

    private void getCovers(String url) {
        mCoverUrl = url;
        new RoboAsyncTask<Boolean>((Activity) getContext()) {
            public Boolean call() throws Exception {

                HttpRequest request = get(mCoverUrl);
                mCover = new BitmapDrawable(BitmapFactory.decodeStream(request.stream()));

                return true;
            }

            @Override
            protected void onException(Exception e) throws RuntimeException {
                e.printStackTrace();
            }

            @Override
            public void onSuccess(Boolean relationship) {
                set_cover(mCover);
            }

        }.execute();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        if (kc_vv.couldPlayNext()) {
            if (isPlaying) {
                kc_vv.playNext();
            }
        } else {
            isPlaying = false;
            change_view_to_pause();
            seekBar.setProgress(0);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        tv_message.setVisibility(VISIBLE);
        Log.e("KCVideoView", "视频无法播放");
        return true;
    }

    class CTimePoint {
        int time_point;
        ITimePointListener l;

        CTimePoint(int time_point, ITimePointListener l) {
            this.time_point = time_point;
            this.l = l;
        }

        public int getTime_point() {
            return time_point;
        }

        public ITimePointListener getL() {
            return l;
        }
    }

    List<CTimePoint> listITimePointListener;

    // int time_point_millisecond 是一个毫秒值。表示播放到这个位置时触发该回调。
    public void add_time_point_listener(int time_point_millisecond, ITimePointListener l) {
        CTimePoint c = new CTimePoint(time_point_millisecond, l);
        listITimePointListener.add(c);
    }

    ;

    public interface ITimePointListener {
        public void call(KCVideoView view);
    }
    // ITimePointListener 接口中实现一个 call 方法。触发回调时，调用此方法。

    private void hideStatusBar() {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
        try {
            sherlockActivity.getSupportActionBar().hide();
        } catch (Exception ex) {
            Log.d("KCVideoView", "context is not SherlockActivity");
        }
    }

    private void showStatusBar() {
        WindowManager.LayoutParams attrs = activity.getWindow().getAttributes();
        attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
        activity.getWindow().setAttributes(attrs);
        try {
            sherlockActivity.getSupportActionBar().show();
        } catch (Exception ex) {
            Log.d("KCVideoView", "context is not SherlockActivity");
        }
    }

    private int getRequestedOrientation() {
        int h = getDisplayHeight();
        int w = getDisplayWidth();

        if (w >= h) {
            return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        } else {
            return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
        }
    }

    private int getDisplayHeight() {
        final Display defaultDisplay =
                activity.getWindow().getWindowManager().getDefaultDisplay();

        return defaultDisplay.getHeight();
    }

    private int getDisplayWidth() {
        final Display defaultDisplay =
                activity.getWindow().getWindowManager().getDefaultDisplay();

        return defaultDisplay.getWidth();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        try {
//        super.onConfigurationChanged(newConfig); // 注释掉则不会切屏
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // land
            } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                // port
            }
        } catch (Exception ex) {
        }
    }

    public void set_title(String title) {
        tv_title.setText(title);
    }


    private VerticalSeekBar.OnSeekBarChangeListener volume_change = new VerticalSeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(VerticalSeekBar VerticalSeekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(VerticalSeekBar VerticalSeekBar) {

        }

        @Override
        public void onStopTrackingTouch(VerticalSeekBar VerticalSeekBar) {
            AudioManager mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, VerticalSeekBar.getProgress(), AudioManager.MODE_NORMAL);
        }
    };

    public int getCurrentPosition(){
        return kc_vv.getCurrentPosition();
    }

    public void seekTo(int msec){
        kc_vv.seekTo(msec);
    }
}

