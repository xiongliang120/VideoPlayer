package com.example.user.videoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.example.user.videoplayer.R;
import com.example.user.videoplayer.util.VolumeUtil;


/**
* Copyright  : 2015-2033 Beijing Startimes Communication & Network Technology Co.Ltd
* Created by majh on 2015/10/25.
* ClassName    : VolumeBar.java
* Description  :音量条控件，实现音量图标的显示，进度值显示和动画显示。
* 
*/
public class VolumeBar extends FrameLayout {
	private int progress = -1;
	private final int VOLUME_MAX = 100;
	
	private VolumeProgressView mVolumebarProgressView;
	private View bgView;
	private Context mContext;
	private int maxVolume;
	private OnSilenceListener onSilenceListener;
	
	public VolumeBar(Context context) {
		super(context);
		mContext = context;
		maxVolume = VolumeUtil.getMaxVolume(mContext);
	}

	public VolumeBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		maxVolume = VolumeUtil.getMaxVolume(mContext);
	}
	
	@Override
	protected void onFinishInflate() {

		super.onFinishInflate();
		setFocusable(true);
		mVolumebarProgressView =(VolumeProgressView)findViewById(R.id.my_progress);
		bgView = findViewById(R.id.volum_bg);
	}
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		float current = VolumeUtil.getCurVolume(mContext);
//		if (VolumeUtil.getMuteState()) {
//			current = 0;
//		}
		int progress = (int)(VOLUME_MAX * current / maxVolume + 0.5);
		onVolumeSilence(progress);
		if(progress != this.progress){

			mVolumebarProgressView.setProgress(progress);
			mVolumebarProgressView.invalidate();
			this.progress = progress;
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	public void setSlience(Context context) {
		VolumeUtil.setSilence(context);
		float current = VolumeUtil.getCurVolume(mContext);
		int progress = (int)(VOLUME_MAX * current / maxVolume + 0.5);
//		if (VolumeUtil.getMuteState()) {
//			progress = 0;
//		}
		onVolumeSilence(progress);
		requestLayout();
	}

	private void onVolumeSilence(int progress) {
		if(onSilenceListener == null) {
			return;
		}
		if(progress == 0) {
			bgView.setBackgroundResource(R.mipmap.mute_bg);
			onSilenceListener.onSilence(true);
		} else {
			bgView.setBackgroundResource(R.mipmap.sound_bg);
			onSilenceListener.onSilence(false);
		}
	}
	@Override
	public boolean dispatchKeyEvent(KeyEvent event){
		if(event.getAction() == KeyEvent.ACTION_DOWN){
			return  keyPressToChangeVolume(event.getKeyCode(),1);
		}else if(event.getAction() == KeyEvent.ACTION_MULTIPLE){
			return keyPressToChangeVolume(event.getKeyCode(),event.getRepeatCount());
		}
		return false;
	}
	private boolean keyPressToChangeVolume(int keyCode, int repeatCount) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP){
			VolumeUtil.setVolume(mContext, keyCode == KeyEvent.KEYCODE_VOLUME_UP);
			requestLayout();
			///updateVolum();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT){
			VolumeUtil.setVolume(mContext, keyCode == KeyEvent.KEYCODE_DPAD_RIGHT);
			requestLayout();
			///updateVolum();
			return true;
		}else if(isUpDown){
			if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
				VolumeUtil.setVolume(mContext, keyCode == KeyEvent.KEYCODE_DPAD_UP);
				requestLayout();
				///updateVolum();
				return true;
			}
		}
		return false;
	}

	public void setOnSilenceListener(OnSilenceListener onSilenceListener) {
		this.onSilenceListener = onSilenceListener;
	}
	
	public interface OnSilenceListener {
		void onSilence(boolean isSilence);
	}


	//设置音量条的默认方式
	public void setChangeMode() {
		isUpDown = true;
	}

	public boolean isUpDown;
}
