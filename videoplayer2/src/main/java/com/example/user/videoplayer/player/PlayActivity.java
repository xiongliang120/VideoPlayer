package com.example.user.videoplayer.player;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.example.user.videoplayer.R;
import com.example.user.videoplayer.volume.VolumeFragment;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by User on 2016/4/20.
 */
public class PlayActivity extends Activity implements SurfaceHolder.Callback,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnBufferingUpdateListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {


    private SurfaceView mSurfaceView;
    private MediaPlayer mMediaPlay;
    private final int PLAY_STATE = 0;//播放状态
    private final int PAUSE_STATE = 1;//暂停状态
    private final int FORWARD_STATE = 2;//快进状态
    private final int BACK_STATE = 3;//快退状态
    private final int VOLUME_STATE = 4;//快退状态

    private int state = PLAY_STATE;//四中状态
    private ImageView stateImage;//不同转台对应的图片
    private final int REFRESH_PROGRESS_BAR = 0;//用于handler发送更新UI线程的状态值
    private final int REFRESH_ANIMATION = 1;//用于handler发送更新动画线程的状态值
    private SurfaceHolder holder;//surface管理类
    private int progressDuration;//进度条快进的距离，根据CHANGE_PRE计算出来的
    private final double CHANGE_PRE = 0.03;//每次快进快退的百分比
    private int proSize;//记录播放的位置
    private String URL;//网络资源URL
    private TextView title, playName;
    private ImageView colorProgress;
    private int mMediaPlayDuration;//整个要播放资源的大小
    private TextView currentTimeText;//当前播放时的时间
    private TextView allTimeText;//总的播放时间
    private Context context;
    private RelativeLayout infoBar;
    private boolean isShowAnimation;//是否显示播放信息

    private boolean isFinishSync;

    private VolumeFragment mVolumeFragment;//音量条fragment的使用
    private FragmentManager fm;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("tag", "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.play_ott_layout);

        this.context = PlayActivity.this;

        Intent intent = getIntent();
        URL = intent.getStringExtra("url");

        Bundle extras = intent.getBundleExtra("bundle");
        initView();
        mTimer.schedule(timerTask, 1000, 1000);

    }


    private Timer mTimer = new Timer();
    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            Message msg = Message.obtain();
            msg.what = REFRESH_PROGRESS_BAR;
            mHandler.sendMessage(msg);
        }
    };


    /**
     * 播放资源的宣称
     */
    public class PlayResThread extends Thread {

        @Override
        public void run() {
            try {
                //加载资源
                if (URL !=null && URL.isEmpty() ) {
                    URL = "http://192.168.32.66:80/vod/mp4:150631.mp4/playlist.m3u8";//可以播放的地址
                    mMediaPlay.setDataSource(context, Uri.parse(URL));

                mMediaPlay.setDisplay(holder);
                mMediaPlay.prepareAsync();  //准备播放
                isFinishSync = true;
            } else {
                AssetFileDescriptor video2 = getResources().getAssets().openFd("video2.mp4");
                mMediaPlay.setDataSource(video2.getFileDescriptor(),
                        video2.getStartOffset(),
                        video2.getLength());
                    mMediaPlay.setDisplay(holder);
                    mMediaPlay.prepareAsync();  //准备播放
                    isFinishSync = true;
            }


        } catch (FileNotFoundException e) {
            finish();

        } catch (IOException e) {
            e.printStackTrace();
            finish();

        } catch (IllegalArgumentException e) {
            finish();
            e.printStackTrace();
        } catch (IllegalStateException e) {
                finish();
                e.printStackTrace();
            } catch (Exception e) {
                finish();
            }
            super.run();
        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case REFRESH_PROGRESS_BAR:
                    if (mMediaPlay == null) {
                        return;
                    }
                    int position = mMediaPlay.getCurrentPosition();
                    if (mMediaPlay.isPlaying() && position <= mMediaPlay.getDuration()) {
                        setColorProgress(position);
                        currentTimeText.setText(getCurrentProgressTime(position) + "");
                    }

                    String currentTime = getCurrentTime(new Date());
                    Log.i("test", "currentTime  刷新" + currentTime);
                    title.setText("" + currentTime);

                    break;
                case REFRESH_ANIMATION:

                    if (!isShowAnimation) {
                        hidePlayInfo();
                    }
                    break;
                default:
                    break;
            }
        }
    };


    public String getCurrentProgressTime(int currentPosition) {
        String currentTime = null;
        int minutes = (currentPosition / 1000) / 60;
        int seconds = (currentPosition / 1000) % 60;
        String secStr;
        String minStr;
        if (seconds < 10) {
            secStr = "0" + seconds;
        } else {

            secStr = seconds + "";
        }
        if (minutes < 10) {
            minStr = "0" + minutes;
        } else {

            minStr = minutes + "";
        }
        currentTime = minStr + ":" + secStr;
        return currentTime;
    }


    /**
     * 根据当前的点击状态设置播放的图标
     */
    public void refreshViewByState() {
        if (mMediaPlay == null || mMediaPlay.getDuration() <= 0) {
            return;
        }
        switch (state) {
            case PLAY_STATE:
                fm.beginTransaction().remove(mVolumeFragment).commit();
                stateImage.setVisibility(View.VISIBLE);
                stateImage.setBackgroundResource(R.mipmap.media_pause);
                //清空堆栈之前的信息
                mHandler.removeMessages(0);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissAnimation(stateImage);

                    }
                }, 3000);
                break;
            case PAUSE_STATE:
                fm.beginTransaction().remove(mVolumeFragment).commit();
                stateImage.setVisibility(View.VISIBLE);
                stateImage.setBackgroundResource(R.mipmap.media_play);
                mHandler.removeMessages(0);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissAnimation(stateImage);

                    }
                }, 3000);

                break;
            case FORWARD_STATE:
                fm.beginTransaction().remove(mVolumeFragment).commit();
                stateImage.setVisibility(View.VISIBLE);
                stateImage.setBackgroundResource(R.mipmap.media_forword);
                mHandler.removeMessages(0);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissAnimation(stateImage);

                    }
                }, 3000);
                break;
            case BACK_STATE:
                fm.beginTransaction().remove(mVolumeFragment).commit();
                stateImage.setVisibility(View.VISIBLE);
                stateImage.setBackgroundResource(R.mipmap.media_back);
                mHandler.removeMessages(0);

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dismissAnimation(stateImage);

                    }
                }, 3000);
                break;
            case VOLUME_STATE:
                mHandler.removeMessages(0);
                stateImage.setVisibility(View.INVISIBLE);

                FragmentTransaction ft = fm.beginTransaction();

                ft.replace(R.id.volume_bar, mVolumeFragment).commit();
                break;
            default:
                break;
        }
    }

    /**
     * 播放提示图标消失动画
     *
     * @param view
     */
    public void dismissAnimation(View view) {
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(view, "alpha", 1, 0);
        alphaAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                stateImage.setAlpha(1f);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                stateImage.setVisibility(View.INVISIBLE);
                stateImage.setAlpha(1f);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        alphaAnimator.setInterpolator(new LinearInterpolator());
        alphaAnimator.setDuration(500).start();

    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        mHandler.removeMessages(REFRESH_ANIMATION);
        Message msg = Message.obtain();
        msg.what = REFRESH_ANIMATION;
        mHandler.sendMessageDelayed(msg, 5000);

        if (!isShowAnimation && title.getVisibility() == View.INVISIBLE) {
            showPlayInfo();
        }

        //过滤down事件
        int action = event.getAction();
        if (action == KeyEvent.ACTION_DOWN && mMediaPlay != null && isFinishSync) {

            switch (event.getKeyCode()) {

                case KeyEvent.KEYCODE_DPAD_CENTER:
                    //点击左面的中心键
                    play();
                    refreshViewByState();
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    //+音量
                    state = VOLUME_STATE;
                    refreshViewByState();
                    mVolumeFragment.onKeyEvent(event);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    //-音量
                    state = VOLUME_STATE;
                    refreshViewByState();
                    mVolumeFragment.onKeyEvent(event);

                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    //快进
                    forward();
                    refreshViewByState();
                    break;
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    //快退
                    back();
                    refreshViewByState();
                    break;

                case KeyEvent.KEYCODE_BACK:
                    long firstTime = System.currentTimeMillis();
                    if (firstTime - currentTime < 1500) {
                        if (mToast != null) {
                            mToast.cancel();
                        }
                        this.finish();
                    } else {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                        RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.toast_layout, null);

                        TextView text = (TextView) layout.findViewById(R.id.msg_center_add_failed_txt);
                        TextView head = (TextView) layout.findViewById(R.id.toast_head);
                        TextView foot = (TextView) layout.findViewById(R.id.toast_foot);
                        LinearLayout toastLayout = (LinearLayout) layout.findViewById(R.id.toast_with_icon_layout);

                        text.setVisibility(View.GONE);
                    //    String str1 = getResources().getString(R.string.double_click);
                    //    String str2 = getResources().getString(R.string.to_return);

                        head.setText("aaaaa");
                        foot.setText("bbbbb");
                        toastLayout.setVisibility(View.VISIBLE);

                        mToast = new Toast(context);
                        mToast.setView(layout);
                        mToast.setDuration(Toast.LENGTH_SHORT);
                        mToast.show();
                        currentTime = System.currentTimeMillis();
                    }

                    break;
                default:
                    break;


            }


        }

        return super.dispatchKeyEvent(event);
    }

    private Toast mToast;
    private long currentTime = 0;

    @Override
    public void onBackPressed() {

    }

    /**
     * 播放操作
     */
    public void play() {
        if (mMediaPlay != null) {
            if (mMediaPlay.isPlaying()) {
                //正在播放的情况
                mMediaPlay.pause();
                state = PAUSE_STATE;
            } else {
                mMediaPlay.start();
                state = PLAY_STATE;
            }
        }

    }

    //设置自定义进度条的进度
    public void setColorProgress(int progress) {
        if (progress > 0) {
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.channel_bar_progress_bar);

            double pre = (double) progress / mMediaPlayDuration;
            if (pre >= 1) {
                pre = 1;
            }
            double colorPro = pre * bitmap.getWidth();

            if (bitmap != null && bitmap.getWidth() > 0 && (int) colorPro > 0) {
                Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, (int) colorPro, bitmap.getHeight());
                try {
                    colorProgress.setImageBitmap(newBitmap);
                } catch (Exception e) {
                    colorProgress.setImageBitmap(bitmap);
                }
            }
        }
    }


    /**
     * 快进操作
     */
    public void forward() {
        if (mMediaPlay == null) {
            return;
        }

        state = FORWARD_STATE;
        int duration = mMediaPlay.getDuration();

        int currentPosition = mMediaPlay.getCurrentPosition();


        currentPosition = currentPosition + progressDuration;
        if (currentPosition >= duration) {
            //播放完了
            this.finish();
        }
        mMediaPlay.seekTo(currentPosition);
        setColorProgress(currentPosition);
        currentTimeText.setText(getCurrentProgressTime(currentPosition) + "");
    }

    /**
     * 快退操作
     */
    public void back() {
        if (mMediaPlay == null) {
            return;
        }

        state = BACK_STATE;
        int currentPosition = mMediaPlay.getCurrentPosition();

        currentPosition = currentPosition - progressDuration;

        if (currentPosition <= 0) {
            currentPosition = 0;
        }
        mMediaPlay.seekTo(currentPosition);
        setColorProgress(currentPosition);
        currentTimeText.setText(getCurrentProgressTime(currentPosition) + "");
    }


    /**
     * 初始化界面控件
     */
    private void initView() {
        colorProgress = (ImageView) findViewById(R.id.color_progress);
        currentTimeText = (TextView) findViewById(R.id.current_play_time);
        allTimeText = (TextView) findViewById(R.id.all_time);

        String board = Build.BOARD;
        if (board.contains("410") || board.contains("Mstar")) {

            mMediaPlay = new MediaPlayer();
        }

        mMediaPlay = new MediaPlayer();

        mSurfaceView = (SurfaceView) findViewById(R.id.surface_view);
        stateImage = (ImageView) findViewById(R.id.state_img);
        holder = mSurfaceView.getHolder();
        holder.addCallback(this);
        holder.setFixedSize(ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        title = (TextView) findViewById(R.id.time_title);
        playName = (TextView) findViewById(R.id.play_name);
        infoBar = (RelativeLayout) findViewById(R.id.info_bar);
        playName.setText("item");
        mVolumeFragment = new VolumeFragment(context);
        mVolumeFragment.setResponseUpDown();//设置在vod播放中相应上下键调正音量
        fm = getFragmentManager();
    }

    public String getCurrentTime(Date date) {
        String time = "null";
        String[] weekDays = {getString(R.string.date_weekend), getString(R.string.date_monday), getString(R.string.date_tuesday), getString(R.string.date_wednesday), getString(R.string.date_thursday), getString(R.string.date_friday), getString(R.string.date_saturday)};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        String weekDay = weekDays[w];
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String timeFormat = format.format(date);
        time = weekDay + "." + timeFormat;

        return time;
    }

    public String getCurrentTime() {
        Date date2 = new Date();
        String time = "null";

        String[] weekDays = {getString(R.string.date_weekend), getString(R.string.date_monday), getString(R.string.date_tuesday), getString(R.string.date_wednesday), getString(R.string.date_thursday), getString(R.string.date_friday), getString(R.string.date_saturday)};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date2);

        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;

        String weekDay = weekDays[w];


        time = weekDay;

        return time;
    }


    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        mMediaPlay.setOnPreparedListener(this);
        mMediaPlay.setOnBufferingUpdateListener(this);
        mMediaPlay.setOnErrorListener(this);

        new PlayResThread().start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    @Override
    protected void onDestroy() {
        mHandler.removeMessages(0);
        if (mMediaPlay != null) {
            mMediaPlay.release();
            mMediaPlay = null;
        }
        mSurfaceView = null;
        holder = null;
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        super.onDestroy();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        Log.e("leslie", "onCompletion: onCompletion");
        mHandler.removeMessages(0);
        onDestroy();
        this.finish();
    }

    @Override
    public void onBufferingUpdate(MediaPlayer mediaPlayer, int bufferingProgress) {
    }

    //播放器准备就绪
    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        if (mMediaPlay != null) {
            int videoWidth = mMediaPlay.getVideoWidth();
            int videoHeight = mMediaPlay.getVideoHeight();
            if (videoHeight != 0 && videoWidth != 0) {
                mMediaPlay.start();
                if (!isShowAnimation) {
                    showPlayInfo();
                }


                mHandler.removeMessages(REFRESH_ANIMATION);
                Message msg = Message.obtain();
                msg.what = REFRESH_ANIMATION;
                mHandler.sendMessageDelayed(msg, 5000);

                mMediaPlayDuration = mMediaPlay.getDuration();
                progressDuration = (int) (mMediaPlayDuration * CHANGE_PRE);

                allTimeText.setText(getCurrentProgressTime(mMediaPlayDuration) + "");
                mMediaPlay.setOnCompletionListener(this);

            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {


        return false;
    }

    /**
     * 隐藏播放信息的动画
     */
    private void hidePlayInfo() {
        isShowAnimation = true;
        ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(title, "translationY", 0, -80);
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(infoBar, "translationY", 0, 350);

        ObjectAnimator barAlphaAnimator = ObjectAnimator.ofFloat(infoBar, "alpha", 100, 0);
        ObjectAnimator titleAlphaAnimator = ObjectAnimator.ofFloat(title, "alpha", 100, 0);
        AnimatorSet set = new AnimatorSet();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShowAnimation = false;
                title.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set.play(titleAnimator).with(barAnimator).with(barAlphaAnimator).with(titleAlphaAnimator);
        set.setDuration(500);
        set.start();
    }

    /**
     * 显示播放信息的动画
     */
    public void showPlayInfo() {
        title.setVisibility(View.VISIBLE);
        isShowAnimation = true;
        ObjectAnimator titleAnimator = ObjectAnimator.ofFloat(title, "translationY", -80, 0);
        ObjectAnimator barAnimator = ObjectAnimator.ofFloat(infoBar, "translationY", 350, 0);

        ObjectAnimator barAlphaAnimator = ObjectAnimator.ofFloat(infoBar, "alpha", 0, 100);
        ObjectAnimator titleAlphaAnimator = ObjectAnimator.ofFloat(title, "alpha", 0, 100);


        AnimatorSet set2 = new AnimatorSet();
        set2.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                isShowAnimation = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        set2.play(titleAnimator).with(barAnimator).with(barAlphaAnimator).with(titleAlphaAnimator);
        set2.setDuration(500);
        set2.start();

    }


}