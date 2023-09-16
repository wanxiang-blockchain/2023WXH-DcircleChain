package com.base.foundation.utils;

public class HexUtils {
    public static int hex2Int(char ch){
        char c = Character.toLowerCase(ch);
        int ret = 0;
        if('0'<= c && '9' >= c){
            ret = c - '0';
        } else if ('a'<= c && 'f' >= c) {
            ret = c - 'a' + 10;
        }
        return ret;
    }

    public static byte[] fromHex(String hex){
        int len  = hex.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            int high = hex2Int(hex.charAt(2*i));
            int low = hex2Int(hex.charAt(2*i+1));
            int ret = high*16 +low;
            result[i] = (byte)ret;
        }
        return result;
    }


    public static String toHex(byte[] b) {
        String hs = "";
        String stmp = "";
        for (int i : b) {
            int value = i;
            if (i < 0) {
                value = i + 256;
            }
            stmp = (Integer.toHexString(value & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toLowerCase();
    }
}
