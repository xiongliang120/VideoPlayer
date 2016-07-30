package com.example.user.videoplayer.player;

import android.app.Fragment;
import android.view.KeyEvent;
import android.view.View;



/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class BaseFragment extends Fragment  {
    private FragmentCallBack fragmentCallBack;
    protected boolean menuState;  //true 表示menu状态, false表示非menu状态
    protected boolean direction;   //true 表示向左， false表示向右

    public void onOkKeyEvent() {

    }

    public boolean onKeyEvent(KeyEvent event) {
        return false;
    }


    public void hiddenBottomIcons() {

    }

    public void showBottomIcons() {

    }

    public void setMenuState(boolean state) {
        this.menuState = state;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }



    public void onFirsFocusViewInit(int pageIndex) {
        fragmentCallBack = (FragmentCallBack) BaseFragment.this.getActivity();
        if (fragmentCallBack != null) {
            fragmentCallBack.onFirstFocuesViewInit();
        }
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isVisibleToUser) {
            return;
        }
        fragmentCallBack = (FragmentCallBack) BaseFragment.this.getActivity();
        if (fragmentCallBack != null) {
            fragmentCallBack.onFragmentIsUserVisible();
        }



    }

    public void initFocusView() {

        //只有在menu状态下进行切换时
        if (menuState) {

        } else {  //非menu状态下切换时实现首尾连接，向左找最右边焦点，向右找最左边的焦点
            if (direction) {

            } else {

            }
        }
    }

    //寻找最左边的控件
    public View findFristFocusView(View view) {
        if (view == null) {
            return null;
        }
        View nextFocus = view.focusSearch(View.FOCUS_LEFT);
        if (nextFocus == null) {
            return view;
        } else {
            return findFristFocusView(nextFocus);
        }
    }

    //寻找最右边的控件
    public View findLastFocusView(View view) {
        if (view == null) {
            return null;
        }
        View nextFocus = view.focusSearch(View.FOCUS_RIGHT);
        if (nextFocus == null) {
            return view;
        } else {
            return findLastFocusView(nextFocus);
        }
    }



    public interface FragmentCallBack {
        void onFragmentIsReady();

        /**
         * 刷新焦点框
         */
        void refreshFocusBoderView();

        /**
         * 当Fragment对用户可见
         */
        void onFragmentIsUserVisible();

        void onFirstFocuesViewInit();
    }



}
