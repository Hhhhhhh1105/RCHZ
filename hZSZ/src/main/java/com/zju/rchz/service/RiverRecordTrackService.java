package com.zju.rchz.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.zju.rchz.chief.activity.ChiefEditRecordActivity;

/**
 * Created by ZJTLM4600l on 2017/10/20.
 */

public class RiverRecordTrackService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        new Thread(new ChiefEditRecordActivity.MyRunable(){
//            @Override
//            public void run() {
//                super.run();
//            }
//        }).start();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
