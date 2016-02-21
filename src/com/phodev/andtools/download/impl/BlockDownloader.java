package com.phodev.andtools.download.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Future;

import com.phodev.andtools.download.Constants;
import com.phodev.andtools.download.Utils;

import android.util.Log;

/**
 * 文件分段加载器
 * 
 * @author skg
 */
public class BlockDownloader implements Runnable {
	private String TAG = "BlockDownloader";
	private final static int buffer_size = Constants.block_read_buffer_size;
	private final static String out_file_mode = Constants.download_file_mode;
	private DownloadBlock mBlock;
	private BlockRunnerListener mListener;
	private boolean interrupt = false;
	private Future<BlockDownloader> future;

	public BlockDownloader(BlockRunnerListener listener, DownloadBlock block) {
		mBlock = block;
		mListener = listener;
	}

	public void boundFuture(Future<BlockDownloader> future) {
		this.future = future;
	}

	public void interrupt() {
		interrupt = true;
		if (future != null && !future.isCancelled()) {
			future.cancel(true);
		}
	}

	public boolean isInterrupt() {
		return interrupt;
	}

	@Override
	public void run() {
		if (Constants.DEBUG) {
			log("block start load");
		}
		// check--------------
		if (mBlock == null || mBlock.isInvalid()) {
			makeTerminationOnError(BlockRunnerListener.ERROR_INVALID_BALOCK);
			if (Constants.DEBUG) {
				log("invalid Block:" + mBlock);
			}
			return;
		}
		int loaded = mBlock.getLoadedSize();
		if (loaded < 0) {
			loaded = 0;
		}
		int rangeStart = mBlock.getStart() + loaded;
		int rangeEnd = mBlock.getEnd();
		if (rangeStart >= rangeEnd) {
			notifyWorkDone();
			interrupt();
			return;
		}
		//
		final String sourceUrl = mBlock.getSourceUrl();
		//
		final File file = new File(mBlock.getDownloadFile().getFilePath());
		synchronized (BlockDownloader.class) {// FIXME maybe use other sync lock
			// 避免同一个Task中的多个block同时创建
			if (!file.exists()) {
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
					makeTerminationOnError(BlockRunnerListener.ERROR_INVALID_BALOCK);
					return;
				}
			}
		}
		URL url;
		try {
			url = new URL(sourceUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
			makeTerminationOnError(BlockRunnerListener.ERROR_INVALID_URI);
			return;
		}
		// check end--------------
		//
		try {
			HttpURLConnection http = (HttpURLConnection) url.openConnection();
			Utils.configBlockHeader(http, sourceUrl, rangeStart, rangeEnd);
			if (Constants.DEBUG) {
				log("set block range:" + rangeStart + "-" + rangeEnd + mBlock);
			}
			RandomAccessFile outfile = new RandomAccessFile(file, out_file_mode);
			outfile.seek(rangeStart);
			//
			int read = 0;
			byte[] buffer = new byte[buffer_size];
			InputStream is = http.getInputStream();
			// Utils.debugPrintResponseHeader(http, TAG);
			// Log.e(TAG, "content encoding:" + http.getContentEncoding());
			while (!interrupt && (read = is.read(buffer, 0, buffer_size)) != -1) {
				if (interrupt) {
					break;
				}
				outfile.write(buffer, 0, read);
				mBlock.updateBlock(mBlock.getLoadedSize() + read);
				// if (Constants.DEBUG) {
				// log("block Increase current:" + mBlock.getCurrent());
				// }
				// 通知下载进度
				if (mListener != null) {
					mListener.onBlockIncrease(this, mBlock, read);
				}
			}
			outfile.close();
			is.close();
			if (Constants.DEBUG) {
				log("block load finish interrupt:" + interrupt);
			}
			notifyWorkDone();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			makeTerminationOnError(BlockRunnerListener.ERROR_CAN_NOT_FIND_OUT_FILE);
		} catch (IOException e) {
			e.printStackTrace();
			makeTerminationOnError(BlockRunnerListener.ERROR_NET_WORK_BAK);
		} catch (Exception e) {
			e.printStackTrace();
			makeTerminationOnError(BlockRunnerListener.ERROR_NET_WORK_BAK);
		}
	}

	private void notifyWorkDone() {
		if (!interrupt && mListener != null) {
			mListener.onBlockLoadDone(this, mBlock);
		}
	}

	private void makeTerminationOnError(int errorCode) {
		if (mListener != null) {
			mListener.onBlockLoadFailed(this, mBlock, errorCode);
		}
		interrupt();
	}

	public interface BlockRunnerListener {
		// TODO
		/** 数据段无效 */
		public final static int ERROR_INVALID_BALOCK = 1;
		/** 网络异常 */
		public final static int ERROR_NET_WORK_BAK = 2;
		/** 无效的资源文件 */
		public final static int ERROR_INVALID_URI = 3;
		/** 找不到要缓存的目标文件 */
		public final static int ERROR_CAN_NOT_FIND_OUT_FILE = 4;
		/** SDcard被移除,导致文件下载 */
		public final static int ERROR_SDCARD_IS_REMOVED = 5;
		/** 未知IO Exception */
		public final static int ERROR_UNKONW_IO_EXCEPTION = 6;

		public void onBlockIncrease(BlockDownloader loader, DownloadBlock block, int increase);

		public void onBlockLoadDone(BlockDownloader loader, DownloadBlock block);

		public void onBlockLoadFailed(BlockDownloader loader, DownloadBlock block, int errorCode);

		public void onUnkonwIOException(BlockDownloader loader, IOException ex);
	}

	void log(Object msg) {
		String blockId = null;
		String fName = null;
		String url = null;
		if (mBlock != null) {
			blockId = mBlock.getId();
			url = mBlock.getSourceUrl();
			mBlock.getDownloadFile().getFileName();
		}
		Log.d(TAG, "block--" + blockId + "-fname-" + fName + "-url:" + url);
		Log.d(TAG, "block--" + blockId + "-fname-" + fName + "-msg:" + msg);
	}
}
