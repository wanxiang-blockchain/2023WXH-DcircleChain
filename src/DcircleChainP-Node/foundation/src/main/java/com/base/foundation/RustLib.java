package com.base.foundation;

public class RustLib {
	static {
		System.loadLibrary("rustlib");
	}

	public static native byte[] sssY0(byte[] y_1,byte[] y1);
	public static native byte[] sssY1(byte[] y0,byte[] y_1);

	public static native String qrAnalysis(byte[] bytes);

	public static native String base64Encode(byte[] in_data);
	public static native byte[] base64Decode(String in_data);

	public static native byte[] getEip1040Address(byte[] address,byte[] salt,byte[] init_code);

	public static native byte[] newKeccak256Cid(byte[] data);


	public static native void ossVersionDecodeFile(String in_data,String out_data);
	public static native void ossVersionEncodeFile(String in_data,String out_data);


	public static native byte[] keccak256ForFile(String file_path);
}
