package com.phodev.andtools.download.impl;

import com.phodev.andtools.download.DownloadFile;

import android.text.TextUtils;

/**
 * 下载块
 * 
 * @author skg
 */
public class DownloadBlock {
	private String id;
	private DownloadFile downloadFile;
	private String sourceUrl;
	private String realUrl;
	private int start;
	private int end;
	private int loadedSize;

	public DownloadBlock(DownloadFile dFile, String sourceUrl, String realUrl, int start, int end, int loadedSize) {
		this.downloadFile = dFile;
		this.sourceUrl = sourceUrl;
		this.start = start;
		this.end = end;
		this.loadedSize = loadedSize;
		this.realUrl = realUrl;
	}

	protected void updateBlock(int loadedSize) {
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

	/**
	 * 获取真实的下载地址
	 * 
	 * @return
	 */
	public String getRealUrl() {
		return realUrl;
	}

	public void setRealUrl(String realUrl) {
		this.realUrl = realUrl;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public int getLoadedSize() {
		return loadedSize;
	}

	//
	/**
	 * 是否是无效的
	 */
	public boolean isInvalid() {
		return downloadFile == null || TextUtils.isEmpty(sourceUrl) || start < 0 || end < 0 || end < start;
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
		return "DownloadBlock [id=" + id + ", sourceUrl=" + sourceUrl + ", start=" + start + ", end=" + end
				+ ", loadedSize=" + loadedSize + "]";
	}

}
