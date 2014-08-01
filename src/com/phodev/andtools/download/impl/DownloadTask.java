package com.phodev.andtools.download.impl;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.DownloadFile;
import com.phodev.andtools.download.Utils;
import com.phodev.andtools.download.impl.BlockDownloader.BlockRunnerListener;

/**
 * 管理一个下载任务
 * 
 * @author skg
 */
public class DownloadTask {
	private List<DownloadBlock> blocks = new ArrayList<DownloadBlock>();
	private ExecutorService executor;
	private DownloadFile downloadFile;
	private TaskListener taskListener;
	private boolean isRunning = false;
	private TaskRunner currentRunner;
	private Context context;

	public DownloadTask(Context context, ExecutorService executor,
			DownloadFile df, TaskListener l) {
		this.context = context;
		this.executor = executor;
		downloadFile = df;
		taskListener = l;
	}

	public synchronized boolean start() {
		// 如果已经启动了，则直接返回,否则开启任务
		if (isRunning) {
			return true;
		}
		if (executor == null || executor.isShutdown()) {
			return false;
		}
		isRunning = true;
		currentRunner = new TaskRunner();
		Future<TaskRunner> f = executor.submit(currentRunner, currentRunner);
		currentRunner.boundFutrue(f);
		return true;
	}

	public synchronized boolean stop() {
		// 停止任务,并取消和线程的绑定,并移除block runner，再次启动的时候创建新的block runner
		if (!isRunning) {
			return true;
		}
		if (currentRunner != null) {
			currentRunner.stop();
		}
		currentRunner = null;
		isRunning = false;
		return true;
	}

	/**
	 * 判断是否正在运行
	 * 
	 * @return
	 */
	public synchronized boolean isRunning() {
		return isRunning;
	}

	private int blockTotalIncrease = 0;
	private BlockRunnerListener blockListener = new BlockRunnerListener() {

		@Override
		public void onBlockIncrease(BlockDownloader loader,
				DownloadBlock block, int increase) {
			// 块下载任务进度增加,累加当符合条件的时候同意向外通知进度
			blockTotalIncrease += increase;
		}

		@Override
		public void onBlockLoadDone(BlockDownloader loader, DownloadBlock block) {
			// 块下载完成，通常情况下这个时候不需要做特殊处理
		}

		@Override
		public void onBlockLoadFailed(BlockDownloader loader,
				DownloadBlock block, int errorCode) {
			// Block下载失败,判断原因看是否需要开启线程继续下载
			// 1,如果是网络部可用，则停止全部下载,并通知外部监听
			// 2,如果是连接超时则尝试重新加载
			if (currentRunner != null) {
				currentRunner.innerStopAllLoader();
			}
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
			onDownloadError(e);
		}

		@Override
		public void onUnkonwIOException(BlockDownloader loader, IOException ex) {
			if (currentRunner != null) {
				currentRunner.innerStopAllLoader();
			}
			onDownloadError(TaskListener.TASK_ERROR_UNKOWN);
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
			if (downloadFile != null
					&& downloadFile.getStatus() == DownloadFile.status_download_loading) {
				downloadFile.setStatus(DownloadFile.status_download_paused);
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
			if (downloadFile.getStatus() != DownloadFile.status_download_loading) {
				downloadFile.setStatus(DownloadFile.status_download_loading);
				recorder.updateFile(context, downloadFile);
				notifyFileStatusChange();
			}
			String sourceUrl = downloadFile.getSourceUrl();
			URL url = null;
			try {
				url = new URL(sourceUrl);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				onDownloadError(TaskListener.TASK_ERROR_MALFORMED_URL);
				return;
			}
			synchronized (blocks) {
				if (blocks.isEmpty()) {
					// loadBlocksFromCache
					recorder.getBocks(context, blocks, downloadFile);
					//
					if (blocks.size() <= 0) {
						try {
							createBlockFromNetFile(downloadFile, url);
						} catch (CreateBlockException e) {
							e.printStackTrace();
						} finally {
							if (blocks.size() <= 0) {
								onDownloadError(TaskListener.TASK_ERROR_INIT_BLOCKS_FAILED);
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
						onDownloadError(TaskListener.TASK_ERROR_THREAD_POOL_SHUTDOWN);
						break;
					}
					if (!b.isCompleteLoad()) {
						BlockDownloader bder = new BlockDownloader(
								blockListener, b);
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
		private void createBlockFromNetFile(DownloadFile file, URL url)
				throws CreateBlockException {
			if (url == null) {
				throw new CreateBlockException();
				// return;
			}
			try {
				// 分段
				final int threadCount = Constants.thread_count;
				blocks.clear();
				//
				HttpURLConnection c = (HttpURLConnection) url.openConnection();
				Utils.configCommonHeader(c, file.getSourceUrl());
				c.connect();
				//
				if (c.getResponseCode() == 200) {
					file.setFileSize(c.getContentLength());
				} else {
					throw new CreateBlockException();
					// return;
				}
				long perBlockSize = file.getFileSize() / threadCount;
				String filename = getFileName(c, file.getSourceUrl());
				file.setFileName(filename);
				recorder.updateFile(context, file);
				//
				File outFile = file.getFile();
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
					long start = i * perBlockSize;
					long end;
					if (i == lastThread) {
						end = file.getFileSize();
					} else {
						end = (i + 1) * perBlockSize;
					}
					DownloadBlock b = new DownloadBlock(file, sourceUrl, start,
							end, 0);
					blocks.add(b);
				}
				// 保存分段记录到数据库
				if (!blocks.isEmpty()) {
					recorder.addBlocks(context, blocks, file.getSourceUrl());
				}
			} catch (IOException e) {
				e.printStackTrace();
				blocks.clear();
				throw new CreateBlockException();
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
				long loadedSize = 0;
				for (DownloadBlock block : blocks) {
					loadedSize += block.getLoadedSize();
				}
				downloadFile.setLoadedSize(loadedSize);
				recorder.updateFile(context, downloadFile);
				// DEBUG-start
				// int tt = 1024 * 1024;
				// long totalSize = downloadFile.getFileSize() / tt;
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
					downloadFile
							.setStatus(DownloadFile.status_download_complete);
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

	private void onDownloadError(int taskErrorCode) {
		int oldStatus = downloadFile.getStatus();
		downloadFile.setStatus(DownloadFile.status_download_error);
		if (oldStatus != DownloadFile.status_download_error) {
			recorder.updateFile(context, downloadFile);
			notifyFileStatusChange();
		}
		if (taskListener != null) {
			taskListener.onDownloadFailed(downloadFile, taskErrorCode);
		}
	}

	private String getFileName(HttpURLConnection conn, String url) {
		String filename = null;
		if (url != null) {
			filename = url.substring(url.lastIndexOf('/') + 1);
		}
		if (filename == null || "".equals(filename.trim())) {
			for (int i = 0;; i++) {
				String mine = conn.getHeaderField(i);
				if (mine == null)
					break;
				if ("content-disposition".equals(conn.getHeaderFieldKey(i)
						.toLowerCase(Locale.getDefault()))) {
					Matcher m = Pattern.compile(".*filename=(.*)").matcher(
							mine.toLowerCase(Locale.getDefault()));
					if (m.find())
						return m.group(1);
				}
			}
			filename = UUID.randomUUID() + ".tmp";
		}
		return filename;
	}

	public interface TaskListener {
		/** 无效的URL*/
		public static final int TASK_ERROR_MALFORMED_URL = 0;
		public static final int TASK_ERROR_INIT_BLOCKS_FAILED = 1;
		public static final int TASK_ERROR_THREAD_POOL_SHUTDOWN = 2;
		public static final int TASK_ERROR_BAD_NETWORK = 3;
		public static final int TASK_ERROR_UNKOWN = 4;

		public void onProgrees(DownloadFile file, long loadedSize, int speed);

		public void onDownloadDone(DownloadFile file);

		public void onDownloadFailed(DownloadFile file, int errorCode);

		public void onDownloadFileStatusChanged(DownloadFile file);
	}

	class CreateBlockException extends Exception {
		private static final long serialVersionUID = 3459920816008528412L;

		@Override
		public String toString() {
			return "CreateBlockException";
		}

	}

}
