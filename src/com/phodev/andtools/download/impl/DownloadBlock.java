package com.phodev.andtools.download.impl;

import android.text.TextUtils;

import com.phodev.andtools.download.DownloadFile;

/**
 * 下载块
 * 
 * @author skg
 */
public class DownloadBlock {
	private String id;
	private DownloadFile downloadFile;
	private String sourceUrl;
	private long start;
	private long end;
	private long loadedSize;

	public DownloadBlock(DownloadFile dFile, String sourceUrl, long start,
			long end, long loadedSize) {
		this.downloadFile = dFile;
		this.sourceUrl = sourceUrl;
		this.start = start;
		this.end = end;
		this.loadedSize = loadedSize;
	}

	protected void updateBlock(long loadedSize) {
		// 更新自己的进度
		this.loadedSize = loadedSize;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public DownloadFile getDownloadFile() {
		return downloadFile;
	}

	public String getSourceUrl() {
		return sourceUrl;
	}

	public long getStart() {
		return start;
	}

	public long getEnd() {
		return end;
	}

	public long getLoadedSize() {
		return loadedSize;
	}

	//
	/**
	 * 是否是无效的
	 */
	public boolean isInvalid() {
		return downloadFile == null || TextUtils.isEmpty(sourceUrl)
				|| start < 0 || end < 0 || end < start;
	}

	/**
	 * 是否已经完成加载
	 * 
	 * @return
	 */
	public boolean isCompleteLoad() {
		return loadedSize > end - start;
	}

	@Override
	public String toString() {
		return "DownloadBlock [id=" + id + ", sourceUrl=" + sourceUrl
				+ ", start=" + start + ", end=" + end + ", loadedSize="
				+ loadedSize + "]";
	}

}
