package com.phodev.andtools.download;
import com.phodev.andtools.download.ICallback;
import com.phodev.andtools.download.DownloadFile;

	interface IDownloadManager{
		/**
		 * 启动下载
		 * 
		 * @param url
		 * @return
		 */
		boolean start(String url);
		/**
		 * 开启所有下载任务
		 * 
		 * @return
		 */
		boolean startAll();
	
		/**
		 * 停止下载
		 * 
		 * @param url
		 * @return
		 */
		boolean stop(String url);
	
		/**
		 * 停止所有下载任务
		 * 
		 * @return
		 */
		boolean stopAll();
	
		/**
		 * 删除下载任务
		 * 
		 * @param url
		 * @param removeLoadedFile
		 *            是否要删除下载好的文件
		 * @return
		 */
		boolean remove(String url, boolean removeLoadedFile);
	
		/**
		 * 移除所有下载任务
		 * 
		 * @param removeLoadedFile
		 * @return
		 */
		boolean removeAll(boolean removeLoadedFile);
	
		/**
		 * 获取正在下载中的文件
		 * 
		 * @return
		 */
		List<DownloadFile> getLoadingFiles();
	
		/**
		 * 获取已经加载好的文件
		 * 
		 * @return
		 */
		List<DownloadFile> getLoadedFiles();
	
		/**
		 * 注册监听器
		 * 
		 * @param l
		 */
		void registerICallback(in ICallback l);
	
		/**
		 * 取消注册监听器
		 * 
		 * @param l
		 */
		void unregisterICallback(in ICallback l);
		
	}