package com.example.user.videoplayer.player;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;


/**
* Copyright  : 2015-2033 Beijing Startimes Communication & Network Technology Co.Ltd
* Created by majh on 2015/10/25.
* ClassName    :AbstractProgressShow.java
* Description  :进度条显示和隐藏的抽象类
* 
*/
public abstract class AbstractProgressShow extends BaseFragment {
	public static final int sDefaultTimeout = 3000;
	public static final int PROGRESS_MAX = 1000;
	public static final int FADE_OUT = 1;
	public static final int SHOW_PROGRESS = 2;
	public boolean mShowing;
	private final int REFRESH_PROGRESS_RATE = 1000;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			long pos;
			switch (msg.what) {
			case FADE_OUT:
				hide();
				break;
			case SHOW_PROGRESS:
				pos = setProgress();
				if (mShowing) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, REFRESH_PROGRESS_RATE);
				}
				break;
			}
		}
	};
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		show(sDefaultTimeout);
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public void onHiddenChanged(boolean hidden) {
		if (!hidden) {
			show(sDefaultTimeout);
		}
		super.onHiddenChanged(hidden);
	}
	
	@Override
	public void onDestroyView() {
		mHandler.removeMessages(FADE_OUT);
		mHandler.removeMessages(SHOW_PROGRESS);
		super.onDestroyView();
	}
	
	protected void show() {
		show(sDefaultTimeout);
	}

	public void show(int timeout) {
		Log.i("MAJH", "Timeout=" + timeout);
		if (!mShowing) {
			setProgress();
			mShowing = true;
		}

		// cause the progress bar to be updated even if mShowing
		// was already true. This happens, for example, if we're
		// paused with the progress bar showing the user hits play.
		mHandler.removeMessages(SHOW_PROGRESS);
		mHandler.sendEmptyMessage(SHOW_PROGRESS);

		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(msg, timeout);
		}
	}
	
	protected void hide() {
		if (mShowing) {
			mHandler.removeMessages(SHOW_PROGRESS);
			if(this.isVisible()) {
				FragmentTransaction mFragmentTransaction = getFragmentManager()
						.beginTransaction();
				mFragmentTransaction.remove(this);
				mFragmentTransaction.commitAllowingStateLoss();//.commit();
			}
			mShowing = false;
		}
	}
	
	protected int setProgress(){
		return 0;
	}
}
