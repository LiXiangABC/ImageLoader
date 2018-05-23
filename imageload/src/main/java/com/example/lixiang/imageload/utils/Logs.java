package com.example.lixiang.imageload.utils;

import android.util.Log;

import com.example.lixiang.imageload.ImageLoader;

/**
 * Created by lixiang on 2017/9/25.
 */

public class Logs {
    public static void Log(String tagname,String tagMsg){
        if (ImageLoader.isDebug) {
        Log.i("ImageLoader ->"+tagname,tagMsg);
        }
    }
}
