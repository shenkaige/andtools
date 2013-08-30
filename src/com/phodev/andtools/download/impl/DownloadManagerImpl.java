package com.phodev.andtools.download.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.DownloadListener;
import com.phodev.andtools.download.DownloadManager;
import com.phodev.andtools.download.ICallback;
import com.phodev.andtools.download.IDownloadManager;
import com.phodev.andtools.download.impl.DownloadTask.TaskListener;

/**
 * 任务下载的具体实现
 * 
 * @author skg
 */
public class DownloadManagerImpl extends IDownloadManager.Stub implements
		DownloadManager {
	static final String TAG = "DownloadManagerImpl";
	private Context mContext;
	private Map<String, DownloadTask> tasks = new ConcurrentHashMap<String, DownloadTask>();
	private DownloadFileMap filesMap = new DownloadFileMap();
	private DownloadRecorder recorder;
	private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(
			Constants.THREAD_POOL_CORE_SIZE, Constants.THREAD_POOL_MAX_SIZE,
			Constants.THREAD_POOL_CHILD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>());
	private Handler handler;

	public DownloadManagerImpl(Context context, Looper looper) {
		mContext = context;
		handler = new Handler(looper, handlerCallback);
		recorder = DownloadRecorder.getInstance();

		recorder.getFiles(context, filesMap.getLoadingFiles(),
				DownloadFile.status_download_loading,
				DownloadFile.status_download_error,
				DownloadFile.status_download_paused);
		recorder.getFiles(context, filesMap.getLoadCompleteFiles(),
				DownloadFile.status_download_complete);

		filesMap.refreshAllFilesMaping();
		//
		createTask(filesMap.getLoadingFiles(), tasks);
	}

	/**
	 * 创建Task
	 * 
	 * @param files
	 * @param out
	 */
	private void createTask(List<DownloadFile> files,
			Map<String, DownloadTask> out) {
		if (files == null || files.isEmpty() || out == null) {
			return;
		}
		for (DownloadFile f : files) {
			if (f != null) {
				String k = f.getSourceUrl();
				DownloadTask v = new DownloadTask(mContext, executor, f,
						taskListener);
				if (k != null && v != null) {
					out.put(k, v);
				}
			}
		}
	}

	@Override
	public boolean start(String url) {
		if (url == null) {
			return false;
		}
		DownloadTask t = tasks.get(url);
		if (t == null) {
			DownloadFile df = new DownloadFile();
			df.setSourceUrl(url);
			t = new DownloadTask(mContext, executor, df, taskListener);
			recorder.addFile(mContext, df);
			tasks.put(url, t);
			notifyDownloadFileAdded(df);
			return t.start();
		}
		if (t.isRunning()) {
			return true;
		}
		return t.start();
	}

	@Override
	public boolean startAll() {
		Iterator<DownloadTask> iter = tasks.values().iterator();
		while (iter.hasNext()) {
			DownloadTask t = iter.next();
			if (t != null && !t.isRunning()) {
				t.start();
			}
		}
		return false;
	}

	@Override
	public boolean stop(String url) {
		if (url == null) {
			return false;
		}
		DownloadTask t = tasks.get(url);
		if (t == null) {
			return false;
		}
		if (t.isRunning()) {
			return t.stop();
		} else {
			return true;
		}
	}

	@Override
	public boolean stopAll() {
		Iterator<DownloadTask> iter = tasks.values().iterator();
		while (iter.hasNext()) {
			DownloadTask t = iter.next();
			if (t != null && t.isRunning()) {
				t.stop();
			}
		}
		return false;
	}

	@Override
	public boolean remove(String url, boolean removeLoadedFile) {
		if (url == null) {
			return false;
		}
		DownloadTask t = tasks.remove(url);
		if (t == null) {
			return true;
		}
		if (t.isRunning()) {
			t.stop();
		}
		if (removeLoadedFile) {
			if (Constants.DEBUG) {
				throw new RuntimeException("暂时不支持 removeLoadedFile");
			}
		}
		recorder.removeFile(mContext, url);
		recorder.removeBlocks(mContext, url);
		DownloadFile file = filesMap.removeFile(url);
		notifyDownloadFileRemoved(file);
		return true;
	}

	@Override
	public boolean removeAll(boolean removeLoadedFile) {
		if (Constants.DEBUG) {
			throw new RuntimeException("暂时不支持 remove all");
		}
		return false;
	}

	@Override
	public List<DownloadFile> getLoadingFiles() {
		return filesMap.getLoadingFiles();
	}

	@Override
	public List<DownloadFile> getLoadedFiles() {
		return filesMap.getLoadCompleteFiles();
	}

	@Override
	public void registerDownloadListener(DownloadListener l) {
		try {
			registerICallback(l);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void unregisterDownloadListener(DownloadListener l) {
		try {
			unregisterICallback(l);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private RemoteCallbackList<ICallback> callbacks = new RemoteCallbackList<ICallback>();

	@Override
	public void registerICallback(ICallback l) throws RemoteException {
		if (l != null) {
			callbacks.register(l);
		}
	}

	@Override
	public void unregisterICallback(ICallback l) throws RemoteException {
		if (l != null) {
			callbacks.unregister(l);
		}
	}

	// 改接口由多线程调用，所以使用handler
	private TaskListener taskListener = new TaskListener() {
		@Override
		public void onProgrees(DownloadFile file, long loadedSize, int speed) {
			Message msg = handler.obtainMessage();
			msg.what = MSG_ACTION_PROGREES;
			Bundle data = msg.getData();
			data.putString(msg_key_source_url, file.getSourceUrl());
			data.putLong(msg_key_file_size, file.getFileSize());
			data.putLong(msg_key_loaded_size, loadedSize);
			data.putInt(msg_key_speed, speed);
			handler.sendMessage(msg);
			if (Constants.DEBUG) {
				int tt = 1024 * 1024;
				long totalSize = file.getFileSize() / tt;
				log((loadedSize / tt) + "/" + totalSize + "Mb---speed:" + speed
						/ 1024 + "kb/s");
			}
		}

		@Override
		public void onDownloadDone(DownloadFile file) {
			Message msg = handler.obtainMessage();
			msg.what = MSG_ACTION_DOWNLOAD_DONE;
			msg.getData().putString(msg_key_source_url, file.getSourceUrl());
			handler.sendMessage(msg);
		}

		@Override
		public void onDownloadFailed(DownloadFile file, int errorCode) {
			Message msg = handler.obtainMessage();
			msg.what = MSG_ACTION_DOWNLOAD_FAILED;
			Bundle data = msg.getData();
			data.putString(msg_key_source_url, file.getSourceUrl());
			data.putInt(msg_key_error_code, errorCode);
			data.putInt(msg_key_file_status, file.getStatus());
			handler.sendMessage(msg);
		}

		@Override
		public void onDownloadFileStatusChanged(DownloadFile file) {
			filesMap.onFileStatusChanged(file);
			notifyDownloadStateChanged(file);
		}

	};
	private static final int MSG_ACTION_PROGREES = 1;
	private static final int MSG_ACTION_DOWNLOAD_DONE = 2;
	private static final int MSG_ACTION_DOWNLOAD_FAILED = 3;
	// private static final int MSG_ACTION_STATUS_CHANGE = 4;
	// value key
	private static final String msg_key_source_url = "source_url";
	private static final String msg_key_file_status = "file_status";
	private static final String msg_key_file_size = "file_size";
	private static final String msg_key_loaded_size = "loaded_size";
	private static final String msg_key_speed = "speed";
	private static final String msg_key_error_code = "error_code";

	private Handler.Callback handlerCallback = new Callback() {
		@Override
		public boolean handleMessage(Message msg) {
			Bundle data = msg.peekData();
			// 通知注册的监听者
			switch (msg.what) {
			case MSG_ACTION_PROGREES:
				if (data != null) {
					String sourceUrl = data.getString(msg_key_source_url);
					long fileSize = data.getLong(msg_key_file_size);
					long loadedSize = data.getLong(msg_key_loaded_size);
					int speed = data.getInt(msg_key_speed);
					notifyProgrees(sourceUrl, fileSize, loadedSize, speed);
				}
				break;
			case MSG_ACTION_DOWNLOAD_DONE:
				if (data != null) {
					notifyDownloadSuccess(data.getString(msg_key_source_url));
				}
				break;
			case MSG_ACTION_DOWNLOAD_FAILED:
				if (data != null) {
					notifyDownloadFailed(data.getString(msg_key_source_url),
							data.getInt(msg_key_file_status),
							data.getInt(msg_key_error_code));
				}
				break;
			// case MSG_ACTION_STATUS_CHANGE:
			// break;
			}
			return true;
		}
	};

	private void notifyProgrees(String url, long total, long loaded, int speed) {
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onProgress(url, total, loaded, speed);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}

	private void notifyDownloadSuccess(String url) {
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onDownloadSuccess(url);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();

	}

	private void notifyDownloadFailed(String url, int curStatus, int errorCode) {
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onDownloadFailed(url, curStatus, errorCode);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}

	private void notifyDownloadStateChanged(DownloadFile file) {
		if (file == null) {
			return;
		}
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onDownloadFileStatusChanged(file);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}

	private void notifyDownloadFileRemoved(DownloadFile file) {
		if (file == null) {
			return;
		}
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onDownloadFileRemove(file);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}

	private void notifyDownloadFileAdded(DownloadFile file) {
		if (file == null) {
			return;
		}
		int i = callbacks.beginBroadcast();
		while (i > 0) {
			i--;
			ICallback cb = callbacks.getBroadcastItem(i);
			try {
				cb.onDownloadFileAdd(file);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		callbacks.finishBroadcast();
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
