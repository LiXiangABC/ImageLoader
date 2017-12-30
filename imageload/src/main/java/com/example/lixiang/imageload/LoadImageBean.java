package com.example.lixiang.imageload;

import android.widget.ImageView;

import java.io.File;

/**
 * Created by lixiang on 2017/11/25.
 */

public class LoadImageBean {
    /**
     * @param imageName 图片的名称
     * @param imageView 图片的视图对象
     * @param cacheFile 指定获取存储图片的File
     */
    String imageName;
    ImageView imageView;
    boolean isFromNet = true;
    File cacheFile = null;

    public File getCacheFile() {
        return cacheFile;
    }

    public LoadImageBean setCacheFile(File cacheFile) {
        this.cacheFile = cacheFile;
        return this;
    }

    public String getImageName() {
        return imageName;
    }

    public LoadImageBean setImageName(String imageName) {
        this.imageName = imageName;return this;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public LoadImageBean setImageView(ImageView imageView) {
        this.imageView = imageView;return this;
    }

    public boolean isFromNet() {
        return isFromNet;
    }

    public LoadImageBean setFromNet(boolean fromNet) {
        isFromNet = fromNet;return this;
    }
}
