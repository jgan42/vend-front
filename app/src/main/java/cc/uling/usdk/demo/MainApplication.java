package cc.uling.usdk.demo;

import android.app.Application;

import cc.uling.usdk.USDK;


public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        USDK.getInstance().init(this);
    }
}
