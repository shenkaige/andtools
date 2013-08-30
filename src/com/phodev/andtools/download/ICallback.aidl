package com.phodev.andtools.download;
import com.phodev.andtools.download.DownloadFile;
	interface ICallback{
		/**
		 * 下载任务状态发生改变
		 * 
		 * @param file
		 */
		 void onDownloadFileStatusChanged(out DownloadFile file);
	
		/**
		 * 添加新的下载任务
		 * 
		 * @param file
		 */
		 void onDownloadFileAdd(out DownloadFile file);
	
		/**
		 * 下载任务被移除
		 * 
		 * @param file
		 */
		 void onDownloadFileRemove(out DownloadFile file);
	
		/**
		 * 下载任务进度的变化(单位都是byte)
		 * 
		 * @param url
		 * @param total
		 *            文件总大小
		 * @param loaded
		 *            已经加载到的大小
		 * @param speed
		 *            当前的速度
		 */
		 void onProgress(String url, long total, long loaded, int speed);
	
		/**
		 * 下载成功
		 * 
		 * @param url
		 */
		 void onDownloadSuccess(String url);
	
		/**
		 * 下载失败
		 * 
		 * @param url
		 * @param curStatus
		 * @param errorCode
		 */
		 void onDownloadFailed(String url, int curStatus, int errorCode);
	}