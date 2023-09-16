package com.base.foundation;

public class EccLoadUtils {

    static {
        System.loadLibrary("rustlib");
    }

    public native String eccEncrypt(String pub_key,String raw_data);

    public native String eccDecrypt(String pri_key,String raw_data);



}
