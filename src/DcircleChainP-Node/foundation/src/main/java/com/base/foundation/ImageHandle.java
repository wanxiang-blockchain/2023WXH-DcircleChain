package com.base.foundation;

public class ImageHandle {
    static {
        System.loadLibrary("rustlib");
    }

    public void getThumb(String fromPath,String outPath,int x,int y,int w,int h){
        imageGetThumb(fromPath, outPath, x, y, w, h);
    }

    public void getLarge(String fromPath,String outPath){
        imageGetLarge(fromPath, outPath);
    }

    public native void imageGetThumb(String fromPath,String outPath,int x,int y,int a,int c);

    public native void imageGetLarge(String fromPath,String outPath);

}
