package com.byids.hy.testpro.fragment;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.byids.hy.testpro.PullDownMenuListener;
import com.byids.hy.testpro.PullUpMenuListener;
import com.byids.hy.testpro.R;
import com.byids.hy.testpro.ScrollViewListener;
import com.byids.hy.testpro.View.MyCustomScrollView;
import com.byids.hy.testpro.View.MyPullUpScrollView;
import com.byids.hy.testpro.activity.MyMainActivity;

/**
 * Created by hy on 2016/8/15.
 */
@SuppressLint({"ValidFragment", "NewApi"})
public class MyFragment extends Fragment implements PullUpMenuListener{
    private LinearLayout linearMenu;  //下拉菜单
    private TextView tvSet;
    private TextView tvMonitoring; //监控
    private TextView tvLock;
    private TextView tvSecurity;

    int btHeight;
    private ImageView imageView;
    private MyCustomScrollView scrollView;
    private String btName;
    private String roomName;
    private GestureDetector gestureDetector;
    private MyMainActivity.MyOntouchListener listener;
    private Activity activity;
    private Button btPullMenu;
    private RelativeLayout linear;

    //上拉菜单
    MyPullUpScrollView svPullUpMenu;
    PullDownMenuListener pullDownMenuListener;//自定义的下拉监听

    private int scrollY; //下滑的距离
    private static final int size = 4;
    private float y;



    public MyFragment(String btName, String roomName) {
        this.btName = btName;
        this.roomName = roomName;
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_layout,null);
        linear = (RelativeLayout) view.findViewById(R.id.linear);
        linearMenu = (LinearLayout) view.findViewById(R.id.linear_menu);
        imageView = (ImageView) view.findViewById(R.id.id_iv);
        btPullMenu = (Button) view.findViewById(R.id.button);
        tvSet = (TextView) view.findViewById(R.id.tv_set);
        tvMonitoring = (TextView) view.findViewById(R.id.tv_monitoring);
        tvLock = (TextView) view.findViewById(R.id.tv_lock);
        tvSecurity = (TextView) view.findViewById(R.id.tv_security);

        tvSet.setOnClickListener(pullDownClick);
        tvMonitoring.setOnClickListener(pullDownClick);
        tvLock.setOnClickListener(pullDownClick);
        tvSecurity.setOnClickListener(pullDownClick);

        btPullMenu.setOnClickListener(pullMenuListener);
        //上拉菜单   子scrollview
        svPullUpMenu = (MyPullUpScrollView) view.findViewById(R.id.scroll_pull_up);
        svPullUpMenu.setOnConnectionListener(this);
        svPullUpMenu.setScrollViewListener(pullDownListener);

        svPullUpMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                svPullUpMenu.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });

        /*svPullUpMenu.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View view, int i, int i1, int i2, int i3) {
                Log.i(, "onScrollChange: ");
            }
        });*/

        //获取控件高度
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        linearMenu.measure(w, h);
        btHeight = linearMenu.getMeasuredHeight();         //头菜单的高度
        //Log.i("result", "onCreateView: ----kongjian-----"+btHeight);

        scrollView = (MyCustomScrollView) view.findViewById(R.id.id_scroll);
        //scrollView.setDownOnConnectionListener(this);
        /*scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                view.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });*/



        //获取屏幕宽高，设置图片大小一致
        WindowManager wm = this.getActivity().getWindowManager();
        int width = wm.getDefaultDisplay().getWidth();
        int height = wm.getDefaultDisplay().getHeight();
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        int statusHeight = getStatusBar();
        params.height = height - statusHeight+500;       //设置图片长度，使上拉时有图片上升的一个效果
        params.width = width;
        Log.i("result", "onCreateView:------------高度 "+(height - statusHeight)+"------宽度"+width);
        imageView.setLayoutParams(params);
        svPullUpMenu.setLayoutParams(params);   //设置上拉菜单全屏

        scrollToBottom();

        return view;

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //手势
        activity = getActivity();
        gestureDetector = new GestureDetector(activity,new MyCustomGesture());
        listener = new MyMainActivity.MyOntouchListener(){

            @Override
            public void onTouchEvent(MotionEvent event) {
                gestureDetector.onTouchEvent(event);
            }
        };
        ((MyMainActivity) activity).registerListener(listener);
        super.onActivityCreated(savedInstanceState);
    }

    //自定义设置的下拉菜单监听
    public void pullDown(boolean f){
        pullDownMenuListener.pullDown(f);
    }

    public void setPullDownMenuListener(PullDownMenuListener pullDownMenuListener){
        this.pullDownMenuListener = pullDownMenuListener;
    }

    //上拉菜单滑动监听
    ScrollViewListener pullDownListener = new ScrollViewListener() {
        @Override
        public void onScrollChanged(MyPullUpScrollView scrollView, int x, int y, int oldx, int oldy) {
            //加入手势，松开的时候 判断距离，选择菜单栏出现还是隐藏的动画
            scrollY = y;
            Log.i("result", "onScrollChanged:-----------x-----------"+x+"----------y---------"+y);



            //判断向下滑的时候(y<控件的高度)    button消失
            if (scrollY==(btHeight*3)) {           //乘以三是因为手指移动和滑动是三倍率
                btPullMenu.setVisibility(View.VISIBLE);
            }else {
                btPullMenu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onCommOnTouchEvent(MyPullUpScrollView scrollView, MotionEvent ev) {
            int action = ev.getAction();
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    y = ev.getY();
                    break;
                case MotionEvent.ACTION_UP:
                    if (scrollY>((btHeight*3)/2)&&scrollY<(btHeight*3)){
                        scrollToBottom();        //滑动到隐藏头
                        pullDown(true);

                    }else if(scrollY<=((btHeight*3)/2)){
                        scrollToTop();           //滑动到显示头
                        pullDown(false);
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    final float preY = y;
                    float nowY = ev.getY();
                    int deltaY = (int) (preY - nowY) / size;

                    int yy = linear.getTop() - deltaY;

                    break;
                default:
                    break;
            }
        }
    };


    //获取状态栏statusBar的高度
    private int getStatusBar(){
        Resources resources = getActivity().getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen","android");
        int height = resources.getDimensionPixelSize(resourceId);
        return height;
    }

    //点击事件
    boolean flag = true;
    View.OnClickListener pullMenuListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //隐藏button
            btPullMenu.setVisibility(View.GONE);

            //两种方式弹出菜单：1.smoothScroll    2.属性动画
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (flag==true){
                        svPullUpMenu.smoothScrollTo(0,-btHeight);
                        flag = false;
                        pullDown(flag);
                    }else if (flag==false){
                        svPullUpMenu.smoothScrollTo(0,btHeight);
                        flag = true;
                    }
                }
            });
        }
    };

    //隐藏控件


    //滑动到隐藏头
    private void scrollToBottom(){
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                svPullUpMenu.smoothScrollTo(0,btHeight*3);
                flag = true;
                btPullMenu.setVisibility(View.VISIBLE);
            }
        });

    }
    private void scrollToTop(){
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                svPullUpMenu.smoothScrollTo(0,-(btHeight*3));
                flag = false;
            }
        });
    }

    View.OnClickListener pullDownClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()){
                case R.id.tv_set:
                    Toast.makeText(getActivity(), "设置", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.tv_monitoring:
                    Toast.makeText(getActivity(), "监控", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.tv_lock:
                    Toast.makeText(getActivity(), "门锁", Toast.LENGTH_SHORT).show();
                    break;
                case R.id.tv_security:
                    Toast.makeText(getActivity(), "安防", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    //两个ScrollView联动
    @Override
    public void onScrollConnection(MyPullUpScrollView scrollView0, int x, int y, int oldx, int oldy) {
        scrollView.scrollTo(x,y);
    }

    @Override
    public void onScrollConnectionDown(MyCustomScrollView scrollView, int x, int y, int oldx, int oldy) {
        svPullUpMenu.scrollTo(x,y);
    }



    //手势
    private class MyCustomGesture extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            Log.i("result", "onSingleTapUp: ---------songshou----------");
            return false;
        }
    }



}