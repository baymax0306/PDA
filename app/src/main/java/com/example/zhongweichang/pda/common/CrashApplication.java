package com.example.zhongweichang.pda.common;

import android.app.Application;

/**
 * Created by zhongweichang on 2018/7/11.
 */

public class CrashApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CrashHandler crashHandler = CrashHandler.getInstance();
        crashHandler.init(getApplicationContext());
    }

}
