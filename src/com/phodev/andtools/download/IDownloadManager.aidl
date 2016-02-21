package com.phodev.andtools.download;
import com.phodev.andtools.download.ICallback;
import com.phodev.andtools.download.IDeleteResult;
import com.phodev.andtools.download.DownloadFile;

	interface IDownloadManager{
	
		/**
		 * 添加下载任务
		 * 
		 * @param url
		 * @return
		 */
	    boolean createTask(String url, String thumbUri, String title, boolean startAfterCreate);
		/**
		 * 查询下载任务
		 * 
		 * @param url
		 * @return
		 */
		DownloadFile queryTask(String url);
		
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
		 */
		void remove(String url, boolean removeLoadedFile,in IDeleteResult dr);
	
		/**
		 * 移除所有下载任务
		 * 
		 * @param removeLoadedFile
		 */
		void removeAll(boolean removeLoadedFile,in IDeleteResult dr);

	   /**
		 * 根据状态获取文件
		 * 
		 * @param statusMatchOrNote
		 *            true 表示包含statusFilter的数据，false表示不包含的数据
		 * @param statusFilter
		 *            null表示获取所有File
		 * @return
		 */
		List<DownloadFile> getFilesByStatus(boolean statusMatchOrNot, in int[] statusFilter);		
		
		/**
		 * 获取所有的下载任务
		 * 
		 * @return
		 */
		List<DownloadFile> getAllFiles();		
	
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