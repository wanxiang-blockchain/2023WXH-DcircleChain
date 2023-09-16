package com.base.foundation;

public class SignUtils {
    static {
        System.loadLibrary("rustlib");
    }

    public static native String sign(String hexPrivateKey,byte[] rawDat);
    public static native String recoverPublicKey(String hexMessage,String hexSignature);
    public static native String recoverAddressFromPublicKey(String hexPublicKey);

}
