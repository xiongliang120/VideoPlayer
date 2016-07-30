package com.example.user.videoplayer.volume;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.user.videoplayer.R;
import com.example.user.videoplayer.player.AbstractProgressShow;
import com.example.user.videoplayer.view.VolumeBar;

/**
 * Copyright  : 2015-2033 Beijing Startimes Communication & Network Technology Co.Ltd
 * Created by Administrator on 2015/10/23.
 * ClassName    :VolumeFragment.java
 * Description  :
 */
@SuppressLint("ValidFragment")
public class VolumeFragment extends AbstractProgressShow {

    private Handler tiemsHandler = new Handler();

    private AudioManager mAudioManager;
    private View mVolumeBarLayout;
    OnPlayStatusIconListener onPlayStatusIconListener;
    private int value;
    private Context mContext;

    public VolumeFragment(Context context) {
        mContext = context;
        mVolumeBarLayout = LayoutInflater.from(context).inflate(R.layout.volume_bar_layout, null, false);
        ((VolumeBar) mVolumeBarLayout).setOnSilenceListener(onSilenceListener);
        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return mVolumeBarLayout;
    }

    public void setSlience() {
        ((VolumeBar) mVolumeBarLayout).setSlience(mContext);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onOkKeyEvent() {
    }


    @Override
    public boolean onKeyEvent(KeyEvent event) {

        super.show(2000);
        Log.i("mVolumeBarLayout", " onKeyEvent=" + event.getKeyCode());
        if (event.getAction() == KeyEvent.ACTION_UP) {
            return false;
        }

        if (mVolumeBarLayout != null && mVolumeBarLayout.isShown()) {

            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                hide();
                return true;
            } else {
                return mVolumeBarLayout.dispatchKeyEvent(event);
            }
        }

        return true;
    }


    private VolumeBar.OnSilenceListener onSilenceListener = new VolumeBar.OnSilenceListener() {
        @Override
        public void onSilence(boolean isSilence) {
            if (isSilence) {
                if (onPlayStatusIconListener != null) {
                    onPlayStatusIconListener.onPlayStatusIcon(true);
                }
            } else {
                if (onPlayStatusIconListener != null) {
                    onPlayStatusIconListener.onPlayStatusIcon(false);
                }
            }
        }
    };


    public void setOnPlayStatusIconListener(OnPlayStatusIconListener onPlayStatusIconListener) {
        this.onPlayStatusIconListener = onPlayStatusIconListener;
    }

    public interface OnPlayStatusIconListener {
        void onPlayStatusIcon(boolean isPlayIcon);
    }

    /**
     * //设置在vod播放中相应上下键调正音量
     */
    public void setResponseUpDown() {
        ((VolumeBar) mVolumeBarLayout).setChangeMode();

    }

}
