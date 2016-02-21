package com.phodev.andtools.download.impl;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.Utils;
import com.phodev.andtools.download.impl.BlockDownloader.BlockRunnerListener;

import android.content.Context;
import android.util.Log;

/**
 * 管理一个下载任务
 * 
 * @author skg
 */
public class DownloadTask {
	private static final String TAG = "DownloadTask";
	private List<DownloadBlock> blocks = new ArrayList<DownloadBlock>();
	private ExecutorService executor;
	private DownloadFile downloadFile;
	private TaskListener taskListener;
	private TaskRunner currentRunner;
	private Context context;

	public DownloadTask(Context context, ExecutorService executor, DownloadFile df, TaskListener l) {
		this.context = context;
		this.executor = executor;
		downloadFile = df;
		taskListener = l;
	}

	public DownloadFile getDownloadFile() {
		return downloadFile;
	}

	public synchronized boolean start() {
		// 如果已经启动了，则直接返回,否则开启任务
		if (isRunning()) {
			return true;
		}
		if (executor == null || executor.isShutdown()) {
			return false;
		}
		currentRunner = new TaskRunner();
		Future<TaskRunner> f = executor.submit(currentRunner, currentRunner);
		currentRunner.boundFutrue(f);
		return true;
	}

	public synchronized boolean stop() {
		// 停止任务,并取消和线程的绑定,并移除block runner，再次启动的时候创建新的block runner
		if (!isRunning()) {
			return true;
		}
		if (currentRunner != null) {
			currentRunner.stop();
		}
		currentRunner = null;
		return true;
	}

	/**
	 * 判断是否正在运行
	 * 
	 * @return
	 */
	public synchronized boolean isRunning() {
		return currentRunner != null && !currentRunner.interrupt;
	}

	private int blockTotalIncrease = 0;
	private BlockRunnerListener blockListener = new BlockRunnerListener() {

		@Override
		public void onBlockIncrease(BlockDownloader loader, DownloadBlock block, int increase) {
			// 块下载任务进度增加,累加当符合条件的时候同意向外通知进度
			blockTotalIncrease += increase;
		}

		@Override
		public void onBlockLoadDone(BlockDownloader loader, DownloadBlock block) {
			// 块下载完成，通常情况下这个时候不需要做特殊处理
		}

		@Override
		public void onBlockLoadFailed(BlockDownloader loader, DownloadBlock block, int errorCode) {
			// Block下载失败,判断原因看是否需要开启线程继续下载
			// 1,如果是网络部可用，则停止全部下载,并通知外部监听
			// 2,如果是连接超时则尝试重新加载
			int e = TaskListener.TASK_ERROR_UNKOWN;
			switch (errorCode) {
			case ERROR_INVALID_BALOCK:
				break;
			case ERROR_NET_WORK_BAK:
				e = TaskListener.TASK_ERROR_BAD_NETWORK;
				break;
			case ERROR_INVALID_URI:
				break;
			case ERROR_CAN_NOT_FIND_OUT_FILE:
				break;
			case ERROR_SDCARD_IS_REMOVED:
				break;
			case ERROR_UNKONW_IO_EXCEPTION:
			default:
				break;
			}
			onDownloadError(e, true);
		}

		@Override
		public void onUnkonwIOException(BlockDownloader loader, IOException ex) {
			if (currentRunner != null) {
				currentRunner.innerStopAllLoader();
			}
			onDownloadError(TaskListener.TASK_ERROR_UNKOWN, true);
		}

	};

	private List<BlockDownloader> loaders = new ArrayList<BlockDownloader>();
	private DownloadRecorder recorder = DownloadRecorder.getInstance();

	class TaskRunner implements Runnable {
		private boolean interrupt = false;
		private Future<TaskRunner> futrue;

		public void boundFutrue(Future<TaskRunner> futrue) {
			this.futrue = futrue;
		}

		/**
		 * 取消任务
		 */
		public void stop() {
			innerStopAllLoader();
			if (downloadFile != null && downloadFile.getStatus() == DownloadFile.STATUS_DOWNLOAD_LOADING) {
				downloadFile.setStatus(DownloadFile.STATUS_DOWNLOAD_PAUSED);
				recorder.updateFile(context, downloadFile);
				notifyFileStatusChange();
			}
		}

		// 取消停止loader
		private void innerStopAllLoader() {
			if (interrupt) {
				return;
			}
			interrupt = true;
			synchronized (loaders) {
				for (BlockDownloader b : loaders) {
					if (b != null) {
						b.interrupt();
					}
				}
				loaders.clear();
			}
			if (futrue != null && !futrue.isCancelled()) {
				futrue.cancel(true);
			}
		}

		@Override
		public void run() {
			if (downloadFile.getStatus() != DownloadFile.STATUS_DOWNLOAD_LOADING) {
				downloadFile.setStatus(DownloadFile.STATUS_DOWNLOAD_LOADING);
				recorder.updateFile(context, downloadFile);
				notifyFileStatusChange();
			}
			String sourceUrl = downloadFile.getSourceUrl();
			synchronized (blocks) {
				if (blocks.isEmpty()) {
					// loadBlocksFromCache
					recorder.getBocks(context, blocks, downloadFile);
					//
					if (blocks.size() <= 0) {
						try {
							createBlockFromNetFile(downloadFile, sourceUrl);
						} catch (CreateBlockException e) {
							e.printStackTrace();
						} finally {
							if (blocks.size() <= 0) {
								onDownloadError(TaskListener.TASK_ERROR_INIT_BLOCKS_FAILED, true);
								return;
							}
						}
					}
				}
			}
			//
			synchronized (loaders) {
				for (DownloadBlock b : blocks) {
					if (executor.isShutdown()) {
						onDownloadError(TaskListener.TASK_ERROR_THREAD_POOL_SHUTDOWN, true);
						break;
					}
					if (!b.isCompleteLoad()) {
						BlockDownloader bder = new BlockDownloader(blockListener, b);
						Future<BlockDownloader> f = executor.submit(bder, bder);
						bder.boundFuture(f);
						loaders.add(bder);
					}
				}
			}
			//
			loopUpdateDownloadInfo();
		}

		/**
		 * 从网络加载分段信息
		 * 
		 * @param file
		 * @param url
		 */
		private void createBlockFromNetFile(DownloadFile file, String realUrl) throws CreateBlockException {
			try {
				URL url = null;
				try {
					url = new URL(realUrl);
				} catch (MalformedURLException e) {
					e.printStackTrace();
					onDownloadError(TaskListener.TASK_ERROR_MALFORMED_URL, true);
					return;
				}
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				Utils.configCommonHeader(c, file.getSourceUrl());
				c.setInstanceFollowRedirects(false);
				c.connect();
				//
				final int httpStatusCode = c.getResponseCode();
				if (Constants.DEBUG) {
					Log.d(TAG, "create net file http status code:" + httpStatusCode);
				}
				switch (httpStatusCode) {
				case 200:
					file.setFileSize(c.getContentLength());
					break;
				case 302:
				case 301:
					String redirectUrl = c.getHeaderField("Location");
					if (Constants.DEBUG) {
						Log.d(TAG, "find redirectUrl:" + redirectUrl);
					}
					createBlockFromNetFile(file, redirectUrl);
					return;
				default:
					throw new CreateBlockException();
					// return;
				}
				// 分段
				final int threadCount = Constants.thread_count;
				blocks.clear();
				int perBlockSize = file.getFileSize() / threadCount;
				file.setRealUrl(realUrl);
				String filename = Utils.makeDownloadFileName(c, file);
				file.setFileName(filename);
				recorder.updateFile(context, file);
				//
				final File outFile = new File(file.getFilePath());
				if (outFile != null && !outFile.exists()) {
					outFile.createNewFile();
				}
				// RandomAccessFile randOut = new RandomAccessFile(outFile,
				// "rw");
				// randOut.setLength(file.getFileSize());
				//
				int lastThread = threadCount - 1;
				final String sourceUrl = file.getSourceUrl();
				for (int i = 0; i < threadCount; i++) {
					int start = i * perBlockSize;
					int end;
					if (i == lastThread) {
						end = file.getFileSize();
					} else {
						end = (i + 1) * perBlockSize;
					}
					DownloadBlock b = new DownloadBlock(file, sourceUrl, realUrl, start, end, 0);
					blocks.add(b);
				}
				// 保存分段记录到数据库
				if (!blocks.isEmpty()) {
					recorder.addBlocks(context, blocks, file.getSourceUrl(), realUrl);
				}
			} catch (IOException e) {
				e.printStackTrace();
				blocks.clear();
				throw new CreateBlockException(e);
			}
		}

		private void loopUpdateDownloadInfo() {
			while (!interrupt) {
				// 更新进度和速度//block进度的缓存
				try {
					Thread.sleep(Constants.update_speed_interval_time);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				final int speed = blockTotalIncrease;
				blockTotalIncrease = 0;
				int loadedSize = 0;
				for (DownloadBlock block : blocks) {
					loadedSize += block.getLoadedSize();
				}
				downloadFile.setLoadedSize(loadedSize);
				recorder.updateFile(context, downloadFile);
				// DEBUG-start
				// int tt = 1024 * 1024;
				// int totalSize = downloadFile.getFileSize() / tt;
				// Log.e("ttt", (loadedSize / tt) + "/" + totalSize + " speed:"
				// + speed / 1024 + "kb/s");
				// DEBUG-end

				// TODO 控制优化
				recorder.updateBlockProgress(context, blocks);
				if (taskListener != null) {
					taskListener.onProgrees(downloadFile, loadedSize, speed);
				}
				// 判断是否下载完毕
				if (loadedSize >= downloadFile.getFileSize()) {
					recorder.removeBlocks(context, downloadFile.getSourceUrl());
					downloadFile.setStatus(DownloadFile.STATUS_DOWNLOAD_COMPLETE);
					recorder.updateFile(context, downloadFile);
					notifyFileStatusChange();
					if (taskListener != null) {
						taskListener.onDownloadDone(downloadFile);
					}
					innerStopAllLoader();
					break;
				}
			}
		}
	}

	private void notifyFileStatusChange() {
		if (taskListener != null) {
			taskListener.onDownloadFileStatusChanged(downloadFile);
		}
	}

	private void onDownloadError(int taskErrorCode, boolean checkCloseTaskRunner) {
		if (checkCloseTaskRunner) {
			synchronized (currentRunner) {
				if (currentRunner != null) {
					currentRunner.innerStopAllLoader();
				}
			}
		}
		int oldStatus = downloadFile.getStatus();
		downloadFile.setStatus(DownloadFile.STATUS_DOWNLOAD_ERROR);
		if (oldStatus != DownloadFile.STATUS_DOWNLOAD_ERROR) {
			recorder.updateFile(context, downloadFile);
			notifyFileStatusChange();
		}
		if (taskListener != null) {
			taskListener.onDownloadFailed(downloadFile, taskErrorCode);
		}
	}

	public interface TaskListener {
		/** 无效的URL */
		public static final int TASK_ERROR_MALFORMED_URL = 0;
		public static final int TASK_ERROR_INIT_BLOCKS_FAILED = 1;
		public static final int TASK_ERROR_THREAD_POOL_SHUTDOWN = 2;
		public static final int TASK_ERROR_BAD_NETWORK = 3;
		public static final int TASK_ERROR_UNKOWN = 4;

		public void onProgrees(DownloadFile file, int loadedSize, int speed);

		public void onDownloadDone(DownloadFile file);

		public void onDownloadFailed(DownloadFile file, int errorCode);

		public void onDownloadFileStatusChanged(DownloadFile file);
	}

	class CreateBlockException extends Exception {
		private static final long serialVersionUID = 3459920816008528412L;

		public CreateBlockException() {
			super();
		}

		public CreateBlockException(String detailMessage, Throwable throwable) {
			super(detailMessage, throwable);
		}

		public CreateBlockException(String detailMessage) {
			super(detailMessage);
		}

		public CreateBlockException(Throwable throwable) {
			super(throwable);
		}

		@Override
		public String toString() {
			return "CreateBlockException";
		}
	}

}
