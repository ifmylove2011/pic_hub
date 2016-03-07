package com.xter.pichub.util;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by XTER on 2016/3/7.
 */
public class SysUtils {

	/* 获取可用的存储路径 */
	public static String getAvailableStorage() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		else
			return null;
	}

	/**
	 * 获取磁盘缓存路径
	 *
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String getDiskCacheDir(Context context, String fileName) {
		final String cachePath;
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cachePath = context.getExternalCacheDir().getPath();
		} else {
			cachePath = context.getCacheDir().getPath();
		}
		return cachePath + File.separator + fileName;
	}

	/* 将系统长整值转为时间格式 */
	public static String getFormatDate(long date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);
		return sdf.format(new Date(date));
	}

	/**
	 * 字节转十六进制
	 *
	 * @param bytes
	 * @return
	 */
	public String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1)
				sb.append("0");
			sb.append(hex);
		}
		return sb.toString();
	}

	/* px转为dp */
	public static int px2dp(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	/* dp转为px */
	public static int dp2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/* px转为sp */
	public static int px2sp(Context context, float pxValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (pxValue / fontScale + 0.5f);
	}

	/* sp转为px */
	public static int sp2px(Context context, float spValue) {
		final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
		return (int) (spValue * fontScale + 0.5f);
	}
}
