package com.phodev.andtools.download;

import com.phodev.andtools.download.impl.DownloadManagerImpl;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Looper;

/**
 * Service包装的下载管理
 * 
 * @author skg
 */
public class DownloadService extends Service {
	private IDownloadManager.Stub downloadManager;

	@Override
	public void onCreate() {
		super.onCreate();
		downloadManager = new DownloadManagerImpl(getApplicationContext(), Looper.myLooper());
	}

	@Override
	public IBinder onBind(Intent intent) {
		return downloadManager;
	}

}
