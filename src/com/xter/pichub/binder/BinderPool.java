package com.xter.pichub.binder;

import java.util.concurrent.CountDownLatch;

import com.xter.pichub.aidl.IBinderPool;
import com.xter.pichub.service.BinderPoolService;
import com.xter.pichub.util.LogUtils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

/**
 * Created by XTER on 2015/12/24.
 */
public class BinderPool {
	public static final int BINDER_NONE = -1;
	public static final int BINDER_CRYPT = 0;
	public static final int BINDER_WELCOME = 1;

	private Context mContext;
	private IBinderPool iBinderPool;
	// 多线程并发
	private static volatile BinderPool instance;
	private CountDownLatch connectBinderPoolCountDownLatch;

	private BinderPool(Context context) {
		mContext = context.getApplicationContext();
		connectBinderPoolService();
	}

	/* 异常退出时释放资源 */
	private IBinder.DeathRecipient iBinderPoolDeathRecipient = new IBinder.DeathRecipient() {
		@Override
		public void binderDied() {
			LogUtils.w("binder died");
			// 不用“保险”了
			iBinderPool.asBinder().unlinkToDeath(iBinderPoolDeathRecipient, 0);
			iBinderPool = null;
			// 挂掉之后重连
			connectBinderPoolService();
		}
	};

	private ServiceConnection binderPoolConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtils.w(name + " connected");
			iBinderPool = IBinderPool.Stub.asInterface(service);
			try {
				// 连接“保险”，异常退出后调用binderDied
				iBinderPool.asBinder().linkToDeath(iBinderPoolDeathRecipient, 0);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			// 一个线程完成，减一(线程池中又少一)
			connectBinderPoolCountDownLatch.countDown();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			LogUtils.w(name + " disconnected");
		}
	};

	/* 获取类单例 */
	public static BinderPool getInstance(Context context) {
		if (instance == null) {
			synchronized (BinderPool.class) {
				if (instance == null) {
					instance = new BinderPool(context);
				}
			}
		}
		return instance;
	}

	/* 连接binder线程池服务 */
	private synchronized void connectBinderPoolService() {
		connectBinderPoolCountDownLatch = new CountDownLatch(1);
		Intent service = new Intent(mContext, BinderPoolService.class);
		mContext.bindService(service, binderPoolConnection, Context.BIND_AUTO_CREATE);
		try {
			// 等待所有线程工作完毕
			connectBinderPoolCountDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public IBinder queryBinder(int binderCode) {
		IBinder iBinder = null;
		try {
			if (iBinderPool != null) {
				iBinder = iBinderPool.queryBinder(binderCode);
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return iBinder;
	}

	/**
	 * binder线程池实现类
	 */
	public static class BinderPoolImpl extends IBinderPool.Stub {
		public BinderPoolImpl() {
			super();
		}

		@Override
		public IBinder queryBinder(int binderCode) throws RemoteException {
			IBinder iBinder = null;
			switch (binderCode) {
			case BINDER_CRYPT:
				iBinder = new ICryptImpl();
				break;
			default:
				break;
			}
			return iBinder;
		}
	}
}
