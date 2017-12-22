package com.example.administrator.slimmingapk;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.ViewStub;

import java.lang.ref.WeakReference;

public class MainActivity extends FragmentActivity {

    private Handler mHandler = new Handler();

    private SplashFragment splashFragment;

    private ViewStub viewStub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        splashFragment = new SplashFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, splashFragment);
        fragmentTransaction.commit();
        viewStub = (ViewStub) findViewById(R.id.content_viewstub);
        //1.当窗体加载完毕的时候,立马再加载真正的布局进来
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                //将viewstub加载进来
                viewStub.inflate();
            }
        });

//        2.判断当窗体加载完毕的时候执行,一些延时操作(如:动画)
        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                //开启延时加载
                mHandler.postDelayed(new DelayRunnable(MainActivity.this, splashFragment), 2000);
            }
        });


    }


    static class DelayRunnable implements Runnable {

        //强大的弱引用,防侧漏,防止内存泄露好东西.
        // 当 a=null ，这个时候A只被弱引用依赖，那么GC会立刻回收A这个对象，这就是弱引用的好处！
        // 他可以在你对对象结构和拓扑不是很清晰的情况下，帮助你合理的释放对象，造成不必要的内存泄漏！！
        private WeakReference<Context> contextRef;
        private WeakReference<SplashFragment> nextRef;

        public DelayRunnable(Context context, SplashFragment f) {
            contextRef = new WeakReference<Context>(context);
            nextRef = new WeakReference<SplashFragment>(f);
        }

        @Override
        public void run() {
            //移除fragment
            if (contextRef != null) {
                SplashFragment splashFragment = nextRef.get();
                if (splashFragment == null) {
                    return;
                }
                FragmentActivity activity = (FragmentActivity) contextRef.get();
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.remove(splashFragment);
                transaction.commit();
                String a="a";
                String test="can i fuck you?";
                String test1="aaaaa";




                



                //rrrrrrrrrrrrrrrrrr
            }
        }
    }
}
