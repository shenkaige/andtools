package com.phodev.andtools.download.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.ICallback;
import com.phodev.andtools.download.IDeleteResult;
import com.phodev.andtools.download.IDownloadManager;
import com.phodev.andtools.download.impl.DownloadTask.TaskListener;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

/**
 * 任务下载的具体实现
 * 
 * @author skg
 */
public class DownloadManagerImpl extends IDownloadManager.Stub {
	static final String TAG = "DownloadManagerImpl";
	private Context mContext;
	private Map<String, DownloadTask> mTasksMap = new ConcurrentHashMap<String, DownloadTask>();
	private DownloadRecorder recorder;
	private final static ThreadPoolExecutor executor = new ThreadPoolExecutor(Constants.THREAD_POOL_CORE_SIZE,
			Constants.THREAD_POOL_MAX_SIZE, Constants.THREAD_POOL_CHILD_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
			new SynchronousQueue<Runnable>());
	private Handler handler;

	public DownloadManagerImpl(Context context, Looper looper) {
		mContext = context;
		handler = new Handler(looper, handlerCallback);
		recorder = DownloadRecorder.getInstance();
		// load running task
		List<DownloadFile> uncompleteFile = new ArrayList<DownloadFile>();
		recorder.getFilesByStatus(mContext, uncompleteFile, true, DownloadFile.STATUS_DOWNLOAD_LOADING,
				DownloadFile.STATUS_DOWNLOAD_PAUSED, DownloadFile.STATUS_DOWNLOAD_WAIT);
		//
		createTask(uncompleteFile, mTasksMap);
		// check restore all task
		for (int i = 0; i < uncompleteFile.size(); i++) {
			DownloadFile df = uncompleteFile.get(i);
			if (df == null || df.getStatus() != DownloadFile.STATUS_DOWNLOAD_LOADING) {
				continue;
			}
			DownloadTask t = mTasksMap.get(df.getSourceUrl());
			if (!t.isRunning()) {
				t.start();
			}
		}
	}

	/**
	 * 创建Task
	 * 
	 * @param files
	 * @param out
	 */
	private void createTask(List<DownloadFile> files, Map<String, DownloadTask> out) {
		if (files == null || files.isEmpty() || out == null) {
			return;
		}
		for (DownloadFile f : files) {
			if (f != null) {
				String k = f.getSourceUrl();
				DownloadTask v = new DownloadTask(mContext, executor, f, taskListener);
				if (k != null && v != null) {
					out.put(k, v);
				}
			}
		}
	}

	@Override
	public synchronized boolean createTask(String url, String thumbUri, String title, boolean startAfterCreate) {
		if (url == null) {
			return false;
		}
		DownloadTask t = mTasksMap.get(url);
		if (t == null) {
			DownloadFile df = new DownloadFile();
			df.setCreatetime(System.currentTimeMillis());
			df.setSourceUrl(url);
			df.setThumbUri(thumbUri);
			df.setTitle(title);
			//
			t = new DownloadTask(mContext, executor, df, taskListener);
			recorder.addFile(mContext, df);
			mTasksMap.put(url, t);
			notifyDownloadFileAdded(df);
		}
		if (startAfterCreate) {
			scheduleStart(t);
			return true;
		} else {
			return true;
		}
	}

	@Override
	public boolean start(String url) {
		return createTask(url, null, null, true);
	}

	@Override
	public synchronized boolean startAll() {
		Iterator<DownloadTask> iter = mTasksMap.values().iterator();
		while (iter.hasNext()) {
			DownloadTask t = iter.next();
			if (t != null && !t.isRunning()) {
				scheduleStart(t);
			}
		}
		return false;
	}

	@Override
	public synchronized boolean stop(String url) {
		if (url == null) {
			return false;
		}
		DownloadTask t = mTasksMap.get(url);
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
	public synchronized boolean stopAll() {
		Iterator<DownloadTask> iter = mTasksMap.values().iterator();
		while (iter.hasNext()) {
			DownloadTask t = iter.next();
			if (t != null && t.isRunning()) {
				t.stop();
			}
		}
		return false;
	}

	@Override
	public synchronized void remove(String url, boolean removeLoadedFile, IDeleteResult dr) {
		DownloadTask t = mTasksMap.remove(url);
		DownloadFile df = null;
		if (t != null) {
			df = t.getDownloadFile();
			if (t.isRunning()) {
				t.stop();
			}
		}
		if (df == null) {
			df = recorder.getFile(mContext, url);
		}
		recorder.removeFile(mContext, url);
		recorder.removeBlocks(mContext, url);
		new CheckRemoveFileThread(df, dr, removeLoadedFile).start();
	}

	@Override
	public synchronized void removeAll(boolean removeLoadedFile, IDeleteResult dr) {
		stopAll();
		final List<DownloadFile> dfList = new ArrayList<DownloadFile>();
		recorder.getAllFiles(mContext, dfList);
		//
		recorder.removeAllBlocks(mContext);
		recorder.removeAllFile(mContext);
		mTasksMap.clear();
		new CheckRemoveFileThread(dfList, dr, removeLoadedFile).start();
	}

	/**
	 * remove loaded and download temp file
	 * 
	 * @author kaige.shen
	 */
	private class CheckRemoveFileThread extends Thread {
		private final List<DownloadFile> mDownloadList = new ArrayList<DownloadFile>();
		private final IDeleteResult mIDeleteResult;
		private final boolean mRemoveFile;

		public CheckRemoveFileThread(DownloadFile df, IDeleteResult dc, boolean removeFile) {
			mDownloadList.add(df);
			mIDeleteResult = dc;
			mRemoveFile = removeFile;
		}

		public CheckRemoveFileThread(List<DownloadFile> dfList, IDeleteResult dc, boolean removeFile) {
			mDownloadList.addAll(dfList);
			mIDeleteResult = dc;
			mRemoveFile = removeFile;
		}

		@Override
		public void run() {
			final int listSize = mDownloadList.size();
			for (int i = 0; i < listSize; i++) {
				DownloadFile df = mDownloadList.get(i);
				if (mRemoveFile) {
					if (df != null && df.getStatus() != DownloadFile.STATUS_DOWNLOAD_COMPLETE) {
						// 1,删除下载文件
						// 2,删除临时文件
						String filePath = df.getFilePath();
						if (filePath != null && filePath.length() > 0) {
							File file = new File(filePath);
							if (file.exists()) {
								file.delete();
							}
						}
					}
				}
				notifyDownloadFileRemoved(df, mRemoveFile);
				if (mIDeleteResult != null) {
					try {
						mIDeleteResult.onDeleteResult(df, true, listSize, i);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	private final int max_task_runing_count = Constants.max_loading_task_count;
	private int runingTaskCount;

	private synchronized void scheduleStart(DownloadTask task) {
		if (task.isRunning()) {
			return;
		}
		synchronized (mTasksMap) {
			if (runingTaskCount < max_task_runing_count) {
				task.start();
				runingTaskCount++;
			} else {// make wait state and notify state changed
				DownloadFile df = task.getDownloadFile();
				if (df.getStatus() != DownloadFile.STATUS_DOWNLOAD_WAIT) {
					df.setStatus(DownloadFile.STATUS_DOWNLOAD_WAIT);
					notifyDownloadStateChanged(df);
					if (Constants.DEBUG) {
						log("scheduleStart hang-up,runingTaskCount:" + runingTaskCount);
					}
				}
			}
		}
	}

	private synchronized void tryPopTask(int findedTaskCount) {
		synchronized (mTasksMap) {
			runingTaskCount -= findedTaskCount;
			if (runingTaskCount >= max_task_runing_count) {
				return;
			}
			Collection<DownloadTask> tasks = mTasksMap.values();
			for (DownloadTask t : tasks) {
				if (t.isRunning()) {
					continue;
				} else if (t.getDownloadFile().getStatus() == DownloadFile.STATUS_DOWNLOAD_WAIT) {
					t.start();
					runingTaskCount++;
					if (Constants.DEBUG) {
						log("pop download task,current running count:" + runingTaskCount);
					}
					break;
				}
			}
		}
	}

	@Override
	public synchronized DownloadFile queryTask(String url) {
		return recorder.getFile(mContext, url);
	}

	@Override
	public synchronized List<DownloadFile> getAllFiles() {
		List<DownloadFile> out = new ArrayList<DownloadFile>();
		recorder.getAllFiles(mContext, out);
		return out;
	}

	@Override
	public synchronized List<DownloadFile> getFilesByStatus(boolean statusMatchOrNot, int[] statusFilter) {
		List<DownloadFile> out = new ArrayList<DownloadFile>();
		recorder.getFilesByStatus(mContext, out, statusMatchOrNot, statusFilter);
		return out;
	}

	private RemoteCallbackList<ICallback> callbacks = new RemoteCallbackList<ICallback>();

	@Override
	public synchronized void registerICallback(ICallback l) throws RemoteException {
		if (l != null) {
			callbacks.register(l);
		}
	}

	@Override
	public synchronized void unregisterICallback(ICallback l) throws RemoteException {
		if (l != null) {
			callbacks.unregister(l);
		}
	}

	// 改接口由多线程调用，所以使用handler
	private TaskListener taskListener = new TaskListener() {
		@Override
		public void onProgrees(DownloadFile file, int loadedSize, int speed) {
			Message msg = handler.obtainMessage();
			msg.what = MSG_ACTION_PROGREES;
			Bundle data = msg.getData();
			data.putString(msg_key_source_url, file.getSourceUrl());
			data.putInt(msg_key_file_size, file.getFileSize());
			data.putInt(msg_key_loaded_size, loadedSize);
			data.putInt(msg_key_speed, speed);
			handler.sendMessage(msg);
			if (Constants.DEBUG) {
				int tt = 1024 * 1024;
				int totalSize = file.getFileSize() / tt;
				log((loadedSize / tt) + "/" + totalSize + "Mb---speed:" + speed / 1024 + "kb/s");
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
			notifyDownloadStateChanged(file);
			if (file != null) {
				switch (file.getStatus()) {
				case DownloadFile.STATUS_DOWNLOAD_COMPLETE:
				case DownloadFile.STATUS_DOWNLOAD_ERROR:
				case DownloadFile.STATUS_DOWNLOAD_PAUSED:
					tryPopTask(1);
					break;
				}
			}
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
					int fileSize = data.getInt(msg_key_file_size);
					int loadedSize = data.getInt(msg_key_loaded_size);
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
					notifyDownloadFailed(data.getString(msg_key_source_url), data.getInt(msg_key_file_status),
							data.getInt(msg_key_error_code));
				}
				break;
			// case MSG_ACTION_STATUS_CHANGE:
			// break;
			}
			return true;
		}
	};

	private void notifyProgrees(String url, int total, int loaded, int speed) {
		synchronized (callbacks) {
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
	}

	private void notifyDownloadSuccess(String url) {
		synchronized (callbacks) {
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
	}

	private void notifyDownloadFailed(String url, int curStatus, int errorCode) {
		synchronized (callbacks) {
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
	}

	private void notifyDownloadStateChanged(DownloadFile file) {
		if (file == null) {
			return;
		}
		synchronized (callbacks) {
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
	}

	private void notifyDownloadFileRemoved(DownloadFile file, boolean removedFromDisk) {
		if (file == null) {
			return;
		}
		synchronized (callbacks) {
			int i = callbacks.beginBroadcast();
			while (i > 0) {
				i--;
				ICallback cb = callbacks.getBroadcastItem(i);
				try {
					cb.onDownloadFileRemove(file, removedFromDisk);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
			callbacks.finishBroadcast();
		}
	}

	private void notifyDownloadFileAdded(DownloadFile file) {
		if (file == null) {
			return;
		}
		synchronized (callbacks) {
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
	}

	void log(String msg) {
		Log.d(TAG, "" + msg);
	}
}
