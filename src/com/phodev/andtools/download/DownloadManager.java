package com.phodev.andtools.download;

import java.util.List;

/**
 * 下载任务管理器
 * 
 * @author skg
 */
public interface DownloadManager {
	/**
	 * 启动下载
	 * 
	 * @param url
	 * @return
	 */
	public boolean start(String url);

	/**
	 * 开启所有下载任务
	 * 
	 * @return
	 */
	public boolean startAll();

	//
	// /**
	// *
	// * 如果任务存在则会删除现有数据，重新加载，如果任务不存在则创建任务并开始加载
	// *
	// * @param url
	// * @return
	// */
	// public boolean reload(String url);

	/**
	 * 停止下载
	 * 
	 * @param url
	 * @return
	 */
	public boolean stop(String url);

	/**
	 * 停止所有下载任务
	 * 
	 * @return
	 */
	public boolean stopAll();

	/**
	 * 删除下载任务
	 * 
	 * @param url
	 * @param removeLoadedFile
	 *            是否要删除下载好的文件
	 * @return
	 */
	public boolean remove(String url, boolean removeLoadedFile);

	/**
	 * 移除所有下载任务
	 * 
	 * @param removeLoadedFile
	 * @return
	 */
	public boolean removeAll(boolean removeLoadedFile);

	/**
	 * 获取正在下载中的文件
	 * 
	 * @return
	 */
	public List<DownloadFile> getLoadingFiles();

	/**
	 * 获取已经加载好的文件
	 * 
	 * @return
	 */
	public List<DownloadFile> getLoadedFiles();

	/**
	 * 注册监听器
	 * 
	 * @param l
	 */
	public void registerDownloadListener(DownloadListener l);

	/**
	 * 取消注册监听器
	 * 
	 * @param l
	 */
	public void unregisterDownloadListener(DownloadListener l);

}