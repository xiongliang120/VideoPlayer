package  com.example.user.videoplayer.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * Copyright  : 2015-2033 Beijing Startimes Communication & Network Technology Co.Ltd
 * Created by majh on 2015/10/25.
 * ClassName    :VolumeUtil.java
 * Description  :系统音量调节工具类。
 *
 */
public class VolumeUtil {

	private static AudioManager mAudioManager;

	/**
	 * 设置静音和恢复的方法
	 * @param mContext
	 */
	public static void setSilence(Context mContext) {
		initAudioManager(mContext);

		if(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0){
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
		}else{
			mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
		}
	}

	/**
	 *
	 * @param mContext
	 */
	private static void initAudioManager(Context mContext) {
		if(mAudioManager == null) {
			mAudioManager = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
		}
	}
	/**
	 * 当前是否静音，如果音量为 0 也认为是静音
	 * @return
	 */
	public static boolean isSilence() {
		return !(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) > 0);
	}

	/**
	 * 设置音量
	 * @param isAdd： true(增加音量) false(减少音量)
	 */
	public static void setVolume(Context mContext, boolean isAdd) {
		initAudioManager(mContext);

		int curVolume = getCurVolume(mContext);
		int maxVolume = getMaxVolume(mContext);
		if(isAdd && maxVolume > curVolume){
			if(curVolume ==0)
			{
				mAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, false);
			}
			curVolume += 1;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
			return;
		}
		if(!isAdd && curVolume > 0) {
			curVolume -= 1;
			mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
			return;
		}
	}
	/**
	 * 得到系统当前音量
	 * @param mContext
	 * @return
	 */
	public static int getCurVolume(Context mContext) {
		initAudioManager(mContext);

		return mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
	}
	/**
	 * 得到系统最大音量
	 * @param mContext
	 * @return
	 */
	public static int getMaxVolume(Context mContext) {
		initAudioManager(mContext);

		return mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	}
}
