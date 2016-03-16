package com.xter.pichub.binder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.xter.pichub.aidl.ICrypt;

import android.os.RemoteException;

public class ICryptImpl extends ICrypt.Stub {
	@Override
	public String md5Encrypt(String password, int level) throws RemoteException {
		try {
			StringBuffer stb = new StringBuffer();
			MessageDigest digest = MessageDigest.getInstance("md5");
			byte[] result = digest.digest(password.getBytes());
			for (byte b : result) {
				int num = b & 0xff;
				String str = Integer.toHexString(num);
				if (str.length() == 1) {
					stb.append("0");
				}
				stb.append(str);
			}
			//重复加密
			if (level > 0)
				return md5Encrypt(stb.toString(), level - 1);
			else
				return stb.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
}
