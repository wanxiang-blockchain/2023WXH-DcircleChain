package com.base.foundation;

public class Aes {
    public Aes(String key) {
        this.key = key;
    }
    public Aes(){}

    public byte[] encrypt(byte[] data) throws Exception {
        if (data.length == 0) {
            throw new Exception("aes encrypt data length is zero");
        }
        return aesEncrypt(data, key, "");
    }

    public byte[] decrypt(byte[] data) throws Exception {
        if (data.length == 0) {
            throw new Exception("aes decrypt data length is zero");
        }
        return aesDecrypt(data, key, "");
    }

    public  void decryptWithFilePath(String originPath,String outputPath){
        aesDecryptWithFilePath(originPath,outputPath,key,"");
    }

    public void encryptWithFilePath(String originPath,String outputPath){
        aesEncryptWithFilePath(originPath,outputPath,key,"");
    }

    private static native byte[] aesEncrypt(byte[] raw_data,String hex_key,String hex_iv);

    private static native byte[] aesDecrypt(byte[] raw_data,String hex_key,String hex_iv);

    private static native void aesDecryptWithFilePath(String decrypt_file_path,String out_decrypt_file_path,String hex_key,String hex_iv);

    private static native void aesEncryptWithFilePath(String input_encrypt_path,String output_encrypt_path,String hex_key,String hex_iv);

    public static native byte[] messageDecode(byte[]encrypt_data,String hex_key);

    public static native byte[] messageEncode(int version,byte[]encrypt_data,String hex_key);

    static {
        System.loadLibrary("rustlib");
    }

    public String key;
}
