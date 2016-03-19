package com.xter.pichub.broadcast;

import com.xter.pichub.util.LogUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LifeBlood extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		LogUtils.w(intent.getAction());
	}

}
