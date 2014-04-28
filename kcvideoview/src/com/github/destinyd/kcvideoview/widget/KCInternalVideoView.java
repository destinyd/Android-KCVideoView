package com.github.destinyd.kcvideoview.widget;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.VideoView;
import com.github.destinyd.kcvideoview.model.PlayListObj;
import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;

/**
 * Created by dd on 14-4-16.
 */
public class KCInternalVideoView extends VideoView implements MediaPlayer.OnPreparedListener, MediaPlayer.OnVideoSizeChangedListener {
    List<PlayListObj> mListPlay = null;
    int mHours, mMinutes, mSeconds, mSecondsCount;
    int mSize;
    int part = 0;
    int mPlayCount;
    boolean isChangePart = false, isPlaying = false;
    int mChangeSeekTo;
    MediaPlayer mediaPlayer = null;
    int mVideoWidth, mVideoHeight;

    public KCInternalVideoView(Context context) {
        super(context);
        setOnPreparedListener(this);
    }


    public KCInternalVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnPreparedListener(this);
    }

    public KCInternalVideoView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setOnPreparedListener(this);
    }

    @Override
    public void seekTo(int msec) {

        if (mListPlay != null) {
            if (msec > 0) {
                for (int i = mListPlay.size() - 1; i >= 1; i--) {
                    PlayListObj tmp = mListPlay.get(i);
                    PlayListObj tmp_prev = mListPlay.get(i - 1);
                    if (msec < tmp.getSecondsCount() * 1000 && msec >= tmp_prev.getSecondsCount() * 1000) {
                        setPosition(i + 1, msec - tmp_prev.getSecondsCount() * 1000); // this, 逻辑有问题 还是他本身有问题
                        if (isPlaying)
                            start();
                        return;
                    }
                }
                setPosition(1, msec);
                if (isPlaying)
                    start();
                return;
            }
        }
        super.seekTo(msec);
    }

    @Override
    public int getDuration() {
        return mSecondsCount * 1000;
    }

    @Override
    public int getCurrentPosition() {
        int currentPosition = super.getCurrentPosition();
        if (mListPlay != null) {

            if (part >= 2)
                return (mListPlay.get(part - 2).getSecondsCount() * 1000) + currentPosition;

        }
        return currentPosition;
    }

    //
    public void playNext() {
        play(part + 1, 0);
    }

    private void play(int i, int seekTo) {
        setPosition(i, seekTo);
        start();
    }

    @Override
    public void pause() {
        super.pause();
        isPlaying = false;
    }

    @Override
    public void start() {
        super.start();
        isPlaying = true;
    }

    private void setPosition(int i, int seekTo) {
        if (part != i) {
            part = i;
            setVideoURI(Uri.parse(mListPlay.get(i - 1).getUrl()));
            mChangeSeekTo = seekTo;
            isChangePart = true;
        } else {
            super.seekTo(seekTo);
        }
    }

    public void load(String json_string) {
        init();
        PlayListObj tmp_plo;
        mListPlay = Arrays.asList(new Gson().fromJson(json_string, PlayListObj[].class));
        for (int i = 0; i < mListPlay.size(); i++) {
//            tmp_plo = (PlayListObj) it.next();
            tmp_plo = mListPlay.get(i);
            mSecondsCount += tmp_plo.getSeconds();
            mSize = tmp_plo.getSize();
            tmp_plo.setSecondsCount(mSecondsCount);
        }
        setPosition(1, 0);
    }

    ;

    void init() {
        mSecondsCount = 0;
        mSize = 0;
    }


    public boolean couldPlayNext() {
        return mListPlay != null && part < mListPlay.size();
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        if (this.mediaPlayer != mediaPlayer) {
            this.mediaPlayer = mediaPlayer;
            this.mediaPlayer.setOnVideoSizeChangedListener(this);
        }
        Log.d("KCInternalVideoView", "prepared!!!!!!!!!!");
        if (isChangePart) {
            mediaPlayer.seekTo(mChangeSeekTo);
            isChangePart = false;
            if (isPlaying)
                mediaPlayer.start();
        }
    }

    public PlayListObj getCurrentPlayListObj() {
        if (mListPlay != null && part > 0 && part <= mListPlay.size()) {
            return mListPlay.get(part - 1);
        }
        return null;
    }

    public PlayListObj getPrevPlayListObj() {
        if (mListPlay != null && part > 1 && part <= mListPlay.size()) {
            return mListPlay.get(part - 2);
        }
        return null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = getDefaultSize(mVideoWidth, widthMeasureSpec);
        int height = getDefaultSize(mVideoHeight, heightMeasureSpec);
        if (mVideoWidth > 0 && mVideoHeight > 0) {

            int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
            int widthSpecSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
            int heightSpecSize = MeasureSpec.getSize(heightMeasureSpec);

            if (widthSpecMode == MeasureSpec.EXACTLY && heightSpecMode == MeasureSpec.EXACTLY) {
                // the size is fixed
                width = widthSpecSize;
                height = heightSpecSize;

                // for compatibility, we adjust size based on aspect ratio
                if (mVideoWidth * height < width * mVideoHeight) {
                    //Log.i("@@@", "image too wide, correcting");
                    width = height * mVideoWidth / mVideoHeight;
                } else if (mVideoWidth * height > width * mVideoHeight) {
                    //Log.i("@@@", "image too tall, correcting");
                    height = width * mVideoHeight / mVideoWidth;
                }
            } else if (widthSpecMode == MeasureSpec.EXACTLY) {
                // only the width is fixed, adjust the height to match aspect ratio if possible
                width = widthSpecSize;
                height = width * mVideoHeight / mVideoWidth;
                if (heightSpecMode == MeasureSpec.AT_MOST && height > heightSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    height = heightSpecSize;
                }
            } else if (heightSpecMode == MeasureSpec.EXACTLY) {
                // only the height is fixed, adjust the width to match aspect ratio if possible
                height = heightSpecSize;
                width = height * mVideoWidth / mVideoHeight;
                if (widthSpecMode == MeasureSpec.AT_MOST && width > widthSpecSize) {
                    // couldn't match aspect ratio within the constraints
                    width = widthSpecSize;
                }
            } else {
                // neither the width nor the height are fixed, try to use actual video size
                width = mVideoWidth;
                height = mVideoHeight;
                if (
//                        heightSpecMode == MeasureSpec.AT_MOST &&
                        height > heightSpecSize) {
                    // too tall, decrease both width and height
                    height = heightSpecSize;
                    width = height * mVideoWidth / mVideoHeight;
                }
                if (
//                        widthSpecMode == MeasureSpec.AT_MOST &&
                        width > widthSpecSize) {
                    // too wide, decrease both width and height
                    width = widthSpecSize;
                    height = width * mVideoHeight / mVideoWidth;
                }
            }
            getHolder().setFixedSize(width, height);
        } else {
            // no size yet, just adopt the given spec sizes
        }
        setMeasuredDimension(width, height);
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        mVideoWidth = mp.getVideoWidth();
        mVideoHeight = mp.getVideoHeight();
        if (mVideoWidth != 0 && mVideoHeight != 0) {
            getHolder().setFixedSize(mVideoWidth, mVideoHeight);
            requestLayout();
        }
    }
}
