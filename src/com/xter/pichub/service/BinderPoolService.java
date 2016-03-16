package com.xter.pichub.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.xter.pichub.binder.BinderPool;

public class BinderPoolService extends Service {
	public BinderPoolService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	private Binder binderPool = new BinderPool.BinderPoolImpl();

	@Override
	public IBinder onBind(Intent intent) {
		return binderPool;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
